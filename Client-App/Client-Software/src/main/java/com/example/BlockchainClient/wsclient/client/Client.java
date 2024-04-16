package com.example.BlockchainClient.wsclient.client;

import com.example.BlockchainClient.account.Account;
import com.example.BlockchainClient.block.Block;
import com.example.BlockchainClient.constants.Constants;
import com.example.BlockchainClient.transaction.Transaction;
import com.example.BlockchainClient.wsclient.message.Message;
import com.example.BlockchainClient.wsclient.message.MessageDecoder;
import com.example.BlockchainClient.wsclient.message.MessageEncoder;
import com.example.BlockchainClient.wsclient.message.MessageType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.*;
import org.json.JSONObject;
import org.springframework.core.env.Environment;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

@ClientEndpoint(decoders = {MessageDecoder.class},
                encoders = {MessageEncoder.class})

public class Client {

    private static Session session;
    private Account account;
    private final Map<String, Account> ledger = new HashMap<>();
    private List<Block> blockchain;
    private final String userName;
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static Scanner in = new Scanner(System.in);
    public static Environment environment;

    public Client(String username) {
        this.userName = username;
        try {
            String websocketURL = environment.getProperty("WEBSOCKET_URL");
            String URL = websocketURL + userName;

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, new URI(URL));
        } catch (DeploymentException | IOException | RuntimeException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @OnOpen
    public void onOpen(Session session) {
        printDashedLine();
        print("Connection established!");
        Client.session = session;
    }

    @OnMessage
    public void onMessage(Session session, Object message) throws IOException, InterruptedException {
        String json = objectMapper.writeValueAsString(message);
        Message msgFromServer = objectMapper.readValue(json, Message.class);

        if (msgFromServer != null) {
            if (msgFromServer.getContent() != null) {
                print("Server: " + msgFromServer.getContent());
                if(msgFromServer.getContent().equals("❌ Invalid transaction")) {
                    handleInvalidTransactionMessage();
                }
                else if(msgFromServer.getContent().equals("✔ Valid transaction")) {
                    handleValidTransactionMessage(msgFromServer);
                }
            } else {
                print("Server: " + msgFromServer.getType());
                if (msgFromServer.getType().equals(MessageType.SET_INITIAL_DATA)) {
                    handleSetBlockchainRequest(msgFromServer);
                } else if (msgFromServer.getType().equals(MessageType.VALIDATE_TRANSACTION)) {
                    handleValidateTransactionRequest(msgFromServer);
                } else if (msgFromServer.getType().equals(MessageType.MAKE_BLOCK)) {
                    handleMakeBlockRequest(msgFromServer);
                } else if (msgFromServer.getType().equals(MessageType.VALIDATE_BLOCK)) {
                    handleValidateBlockRequest(msgFromServer);
                } else if (msgFromServer.getType().equals(MessageType.RECORD_BLOCK)) {
                    handleRecordBlockRequest(msgFromServer);
                }

            }
        }
    }

    private void handleValidTransactionMessage(Message msgFromServer) throws IOException {
        String str = objectMapper.writeValueAsString(msgFromServer.getTransaction());
        JSONObject transactionJson = new JSONObject(str);
        Transaction transaction = buildTransaction(transactionJson);
        Account sender = transaction.getSender();
        Account receiver = transaction.getReceiver();

        switch (transaction.getTransactionType()) {
            case Constants.DEPOSIT -> {
                sender.deposit(transaction.getAmount());
                ledger.get(sender.getAccountNumber()).deposit(transaction.getAmount());
            }
            case Constants.WITHDRAW -> {
                sender.withdraw(transaction.getAmount());
                ledger.get(sender.getAccountNumber()).withdraw(transaction.getAmount());
            }
            case Constants.TRANSFER -> {
                sender.withdraw(transaction.getAmount());
                receiver.deposit(transaction.getAmount());
                ledger.get(sender.getAccountNumber()).withdraw(transaction.getAmount());
                ledger.get(receiver.getAccountNumber()).deposit(transaction.getAmount());
            }
        }
        transaction.setLedger(new ArrayList<>(ledger.values()));
        sendFinalTransaction(transaction);
    }
    private void handleInvalidTransactionMessage() {
        print("Cause: sufficient funds, or account not found");
        print("stop");
        printDashedLine();
    }
    @OnError
    public void onError(Throwable throwable) {
        System.out.println("there is something wrong :(");
        System.out.println("Error: "+ Arrays.toString(throwable.getStackTrace()));
        printDashedLine();
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        System.out.println("Session closed :(");
        System.out.println("Reason: "+closeReason.getReasonPhrase());
        printDashedLine();

    }


    public void handleSetBlockchainRequest(Message message) throws JsonProcessingException, InterruptedException {
        String accountStr = objectMapper.writeValueAsString(message.getAccount());
        if (accountStr.equals("null")) {
            Account account1 = register();
            print("Account: " + account1);
        }
        printInitialData(message);
    }

    private Account register() {
        printDashedLine();
        System.out.println("You must have an account to access our services");

        System.out.print("Enter you name: ");
        String name = in.nextLine();
        System.out.println();

        System.out.print("Enter initial balance: ");
        double balance = in.nextDouble();
        in.close();
        printDashedLine();
        return ClientServicesImpl.addNewAccount(name, balance);

    }

    private void printInitialData(Message message) throws JsonProcessingException {
        List<Block> IBlockchain = new ArrayList<>();
        String IBlockchainJson = objectMapper.writeValueAsString(message.getBlockchain());
        String accountStr = objectMapper.writeValueAsString(message.getAccount());


        print("Latest Blockchain: " + IBlockchainJson);
        if (!IBlockchainJson.equals("[]")) {
            IBlockchain = objectMapper.readValue(IBlockchainJson, new TypeReference<List<Block>>() {
            });
        }
        setBlockchain(IBlockchain);
        if (!accountStr.equals("null")) {
            print("Account: " + accountStr);
            account = objectMapper.readValue(accountStr, Account.class);
            print("Latest ledger: " + Arrays.toString(ledger.values().toArray()));
        }
    }


    public void handleValidateTransactionRequest(Message message) throws JsonProcessingException {
        String transactionJson = objectMapper.writeValueAsString(message.getTransaction());
        print("Transaction: " + transactionJson);

        JSONObject json = new JSONObject(transactionJson);
        Transaction transaction = buildTransaction(json);
        boolean isValidTransaction = validateTransaction(transaction);
        print("Transaction validation result: " + ((isValidTransaction) ? "✔ Valid Transaction" : "❌ Invalid Transaction"));
        if(!isValidTransaction) {
            print("stop");
            printDashedLine();
        }
        Message msg = new Message();
        msg.setType(MessageType.TRANSACTION_VALIDATION_RESULT);
        msg.setUserName(userName);
        msg.setValidTransaction(isValidTransaction);
        msg.setBalance(account.getBalance());
        msg.setTransaction(transaction);

        sendMessage(msg);
    }

    private Transaction buildTransaction(JSONObject transactionJson) throws JsonProcessingException {
        Transaction transaction = new Transaction();

        String transactionType = transactionJson.get("transactionType").toString();
        Account sender = objectMapper.readValue(transactionJson.get("sender").toString(), Account.class);
        Account receiver = null;
        if(transactionType.equals(Constants.TRANSFER)) {
            receiver = objectMapper.readValue(transactionJson.get("receiver").toString(), Account.class);
        }
        Double amount = objectMapper.readValue(transactionJson.get("amount").toString(), Double.class);
        List<Account> ledger = objectMapper.readValue(transactionJson.get("ledger").toString(), new TypeReference<List<Account>>() {
        });

        transaction.setTransactionType(transactionType);
        transaction.setSender(sender);
        transaction.setReceiver(receiver);
        transaction.setAmount(amount);
        transaction.setLedger(ledger);

        return transaction;
    }


    public void handleMakeBlockRequest(Message message) throws JsonProcessingException {
        String transactionJson = objectMapper.writeValueAsString(message.getTransaction());
        Transaction transaction = objectMapper.readValue(transactionJson, Transaction.class);

        Block block = makeBlock(transaction);
        block = mine(block);

        Message msg = new Message();
        msg.setType(MessageType.BROADCAST_BLOCK);
        msg.setBlock(block);
        print("The block: " + block);

        sendMessage(msg);
    }

    public void handleValidateBlockRequest(Message message) throws JsonProcessingException {
        String blockJson = objectMapper.writeValueAsString(message.getBlock());
        Block block = objectMapper.readValue(blockJson, Block.class);

        print("Block to validate: " + block);
        boolean isValidBlock = validateBlock(block);
        print("Block validation result: " + ((isValidBlock) ? "✔ Valid Block" : "❌ Invalid Block"));

        Message msg = new Message();
        msg.setType(MessageType.BLOCK_VALIDATION_RESULT);
        msg.setUserName(userName);
        msg.setValidBlock(isValidBlock);
        msg.setBlock(block);

        sendMessage(msg);
    }

    public void handleRecordBlockRequest(Message message) throws JsonProcessingException {
        String blockJson = objectMapper.writeValueAsString(message.getBlock());
        Block block = objectMapper.readValue(blockJson, Block.class);

        blockchain.add(block);
        updateLedger(block);
        print("Updated Blockchain: " + Arrays.toString(blockchain.toArray()));
        print("Updated blockchain size: " + blockchain.size());
        print("Updated ledger: "+ledger.values());
        print("stop");

        printDashedLine();
    }


    private void sendFinalTransaction(Transaction transaction) {
        Message finalTransaction = new Message();
        finalTransaction.setType(MessageType.FINAL_TRANSACTION);
        finalTransaction.setTransaction(transaction);
        sendMessage(finalTransaction);
    }

    public void sendMessage(Object message) {
        try {
            session.getBasicRemote().sendObject(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean validateTransaction(Transaction transaction) {
        if (ledger == null || ledger.isEmpty()) return true;

        Account sender = transaction.getSender();
        Double amount = transaction.getAmount();
        String transactionType = transaction.getTransactionType();

        return switch (transactionType) {
            case Constants.DEPOSIT -> ((ledger.get(sender.getAccountNumber()) != null) && (amount > 0));

            case Constants.WITHDRAW ->
                    ((ledger.get(sender.getAccountNumber()) != null)
                            && (ledger.get(sender.getAccountNumber()).getBalance() >= amount)
                            && amount > 0);

            case Constants.TRANSFER ->
                    ((ledger.get(sender.getAccountNumber()) != null)
                            && (ledger.get(transaction.getSender().getAccountNumber())!=null)
                            && (ledger.get(sender.getAccountNumber()).getBalance() >= amount)
                            && amount > 0);

            case Constants.ACCOUNT_CREATED -> true;

            default -> false;
        };
    }

    public Block makeBlock(Transaction transaction) {
        String previousHash = getPreviousHash();
        return new Block(transaction, previousHash);
    }

    public String getPreviousHash() {
        if (blockchain!=null && !blockchain.isEmpty()) {
            Block lastBlock = blockchain.get(blockchain.size() - 1);
            return lastBlock.getHash();
        }
        return Constants.GENESIS_PREV_HASH;
    }

    public Block mine(Block block) {
        String hash = block.getHash();

        while (!AcceptedHash(hash)) {
            block.setHash();
            block.incrementNonce();
            hash = block.getHash();
        }

        return block;
    }

    private boolean AcceptedHash(String hash) {
        char[] difficulty = new char[Constants.DIFFICULTY];
        String leadingZeros = new String(difficulty).replace('\0', '0');

        return (hash.substring(0, Constants.DIFFICULTY).equals(leadingZeros));
    }

    public boolean validateBlock(Block block) {
        Block referenceBlock = null;
        if (blockchain!=null && !blockchain.isEmpty()) {
            referenceBlock = blockchain.get(blockchain.size() - 1);
        }

        String blockHash = block.getHash();
        String blockPreviousHash = block.getPreviousHash();

        String previousHash = ((referenceBlock != null) ? referenceBlock.getHash() : Constants.GENESIS_PREV_HASH);
        Block blockTmp = new Block(block.getTransaction(), previousHash);
        blockTmp.setTimestamp(block.getTimestamp());

        blockTmp = mine(blockTmp);

        return (blockHash.equals(blockTmp.getHash()) && blockPreviousHash.equals(blockTmp.getPreviousHash()));
    }

    public void updateLedger(Block block) {
        List<Account> accounts = block.getTransaction().getLedger();
        for (Account account : accounts) {
            ledger.put(account.getAccountNumber(), account);
        }
    }

    public Account getAccount() {
        account = ledger.get(account.getAccountNumber());
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public List<Block> getBlockchain() {
        return blockchain;
    }

    public void setBlockchain(List<Block> blockchain) {
        this.blockchain = blockchain;
        if (!blockchain.isEmpty()) {
            updateLedger(blockchain.get(blockchain.size() - 1));
        }
    }

    public Map<String, Account> getLedger() {
        return ledger;
    }

    private int printCounter = 1;
    public void print(String message) {
        if(message.equals("stop")) {
            printCounter = 1;
        }
        else {
            System.out.println(printCounter + ") " + message);
            printCounter++;
        }
    }

    private void printDashedLine(){
        System.out.println("\n--------------------------------\n");
    }
}
