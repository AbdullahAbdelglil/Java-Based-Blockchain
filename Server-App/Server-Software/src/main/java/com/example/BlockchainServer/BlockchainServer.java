package com.example.BlockchainServer;

import com.example.BlockchainServer.account.Account;
import com.example.BlockchainServer.account.AccountService;
import com.example.BlockchainServer.account.AccountServiceImpl;
import com.example.BlockchainServer.block.Block;
import com.example.BlockchainServer.block.BlockService;
import com.example.BlockchainServer.block.BlockServiceImpl;
import com.example.BlockchainServer.message.MessageDecoder;
import com.example.BlockchainServer.message.MessageEncoder;

import com.example.BlockchainServer.message.Message;
import com.example.BlockchainServer.message.MessageType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.collection.IList;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

@ServerEndpoint(
        value = "/blockchainServer/{username}",
        decoders = {MessageDecoder.class},
        encoders = {MessageEncoder.class})
@Component
public class BlockchainServer {
    private Session session;
    private static final Set<BlockchainServer> chatEndpoints = new CopyOnWriteArraySet<>();
    private static final Map<String, Session> onlineSessions = new HashMap<>();
    private static final HashMap<String, String> users = new HashMap<>();
    private static final Map<String, Account> ledger = new HashMap<>();

    private static final List<Message> transactionValidationResults = new ArrayList<>();
    private static int transactionValidationCounter = 0;
    private static String transactionSender;

    private static final List<Message> blockValidationResults = new ArrayList<>();
    private static int blockValidationCounter = 0;
    private static String blockSender;

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final HazelcastInstance hazelcast = Hazelcast.newHazelcastInstance();
    private static final IList<Block> IBlockchain = hazelcast.getList("blockchain");

    private static BlockService blockService;
    private static AccountService accountService;


    public BlockchainServer() {
    }

    @Autowired
    public BlockchainServer(BlockService blockService, AccountService accountService) {
        BlockchainServer.blockService = blockService;
        BlockchainServer.accountService = accountService;

    }

    public static void init() {
        Block lastBlock = blockService.getLastBlock();
        if (lastBlock != null) {
            IBlockchain.add(lastBlock);
            updateLedger(lastBlock);
        }

        printDashedLine();
        System.out.println("Initial Blockchain: " + Arrays.toString(IBlockchain.toArray()));
        System.out.println("Initial ledger: " + Arrays.toString(ledger.values().toArray()));
        printDashedLine();
    }

    @OnOpen
    public void onOpen(
            Session session,
            @PathParam("username") String userName) throws IOException, EncodeException {

        printDashedLine();
        print(userName + " Connected!");

        this.session = session;
        chatEndpoints.add(this);
        onlineSessions.put(userName, session);
        users.put(session.getId(), userName);

        Message msgToBroadcast = new Message();
        msgToBroadcast.setContent(userName + " Active now!");
        broadcast(msgToBroadcast, session);

        Message msgToSend = new Message();
        msgToSend.setType(MessageType.SET_BLOCKCHAIN);
        msgToSend.setBlockchain(IBlockchain);
        sendObject(userName, msgToSend);
    }

    @OnMessage
    public void onMessage(Session session, Object message) throws IOException, EncodeException {
        String json = objectMapper.writeValueAsString(message);
        Message msgFromClient = objectMapper.readValue(json, Message.class);
        print("Message from " + users.get(session.getId()) + ": " + msgFromClient.getType());

        if (msgFromClient.getType().equals(MessageType.BROADCAST_TRANSACTION)) {
            handleBroadcastTransactionRequest(session, msgFromClient);
        }else if (msgFromClient.getType().equals(MessageType.TRANSACTION_VALIDATION_RESULT)) {
            handleValidateTransactionRequest(session, msgFromClient);
        }else if(msgFromClient.getType().equals(MessageType.FINAL_TRANSACTION)) {
            handleMakeBlockEvent(msgFromClient);
        } else if (msgFromClient.getType().equals(MessageType.BROADCAST_BLOCK)) {
            handleBroadcastBlockRequest(session, msgFromClient);
        } else if (msgFromClient.getType().equals(MessageType.BLOCK_VALIDATION_RESULT)) {
            handleValidateBlockRequest(session, msgFromClient);
        }
    }
    @OnError
    public void onError(Session session, Throwable throwable) throws EncodeException, IOException {
        String msg = "There is an error in "+users.get(session.getId())+"'s session";
        print(msg);
        printDashedLine();
    }

    @OnClose
    public void onClose(Session session) throws IOException, EncodeException {
        chatEndpoints.remove(this);
        onlineSessions.remove(users.get(session.getId()));
        String msg = users.get(session.getId())+" Disconnected";
        Message message  = new Message();
        message.setContent(msg);
        print(msg);
        broadcast(message, session);
    }

    private void handleBroadcastTransactionRequest(Session session, Message msgFromClient) throws EncodeException, IOException {
        if (!IBlockchain.isEmpty()) {
            if (notValidBlockchain()) {
                fixTheDB(IBlockchain.get(IBlockchain.size() - 1));
            }
            if (notValidAccounts()) {
                fixTheDB(getLatestLedger(IBlockchain.get(IBlockchain.size() - 1)));
            }
        }

        print("Transaction: " + msgFromClient.getTransaction());
        Message msgToSend = new Message();
        msgToSend.setType(MessageType.VALIDATE_TRANSACTION);
        msgToSend.setTransaction(msgFromClient.getTransaction());

        transactionSender = users.get(session.getId());
        broadcast(msgToSend);
    }

    int invalidCounter = 0;

    private void handleValidateTransactionRequest(Session session, Message msgFromClient) throws EncodeException, IOException {
        transactionValidationResults.add(msgFromClient);

        if (msgFromClient.isValidTransaction()) {
            transactionValidationCounter++;
            print(users.get(session.getId()) + ": ✔ Valid Transaction");
        } else {
            print(users.get(session.getId()) + ": ❌ Invalid Transaction");
            invalidCounter++;
            if(invalidCounter == onlineSessions.size()) {
                invalidCounter=0;
            }
        }

        if (transactionValidationResults.size() == onlineSessions.size()) {
            if (transactionValidationCounter == onlineSessions.size()) {
                Message msgToSender = new Message();
                msgToSender.setTransaction(msgFromClient.getTransaction());
                msgToSender.setContent("✔ Valid transaction");
                sendObject(transactionSender, msgToSender);
            } else {
                Message msgToSend = new Message();
                msgToSend.setContent("❌ Invalid transaction");
                print("stop");
                printDashedLine();
                sendObject(transactionSender, msgToSend);
                transactionValidationResults.clear();
            }

            transactionValidationCounter = 0;
        }
    }

    private void handleMakeBlockEvent(Message msgFromClient) throws EncodeException, IOException {
        print("Transaction: "+msgFromClient.getTransaction());

        Message msgToSend = new Message();
        msgToSend.setType(MessageType.MAKE_BLOCK);
        msgToSend.setTransaction(msgFromClient.getTransaction());

        String blockMaker = chooseTheBlockMaker(transactionValidationResults);
        sendObject(blockMaker, msgToSend);
        transactionValidationResults.clear();
    }

    private void handleBroadcastBlockRequest(Session session, Message msgFromClient) throws EncodeException, IOException {
        print("Block to broadcast: " + msgFromClient.getBlock());

        Message msgToSend = new Message();
        msgToSend.setType(MessageType.VALIDATE_BLOCK);
        msgToSend.setBlock(msgFromClient.getBlock());
        broadcast(msgToSend);

        blockSender = users.get(session.getId());
    }


    private void handleValidateBlockRequest(Session session, Message msgFromClient) throws EncodeException, IOException {
        blockValidationResults.add(msgFromClient);
        if (msgFromClient.isValidBlock()) {
            blockValidationCounter++;
            print(users.get(session.getId()) + ": ✔ Valid Block");
        } else {
            print(users.get(session.getId()) + ": ❌ Invalid Block");
            invalidCounter++;
            if (invalidCounter == onlineSessions.size()) {
                invalidCounter = 0;
            }
        }

        if (blockValidationResults.size() == onlineSessions.size()) {
            if (blockValidationCounter == onlineSessions.size()) {
                Message msgToSend = new Message();
                msgToSend.setType(MessageType.RECORD_BLOCK);
                msgToSend.setBlock(msgFromClient.getBlock());
                broadcast(msgToSend);

                recordBlock(msgFromClient.getBlock());

                print("Updated blockchain: " + Arrays.toString(IBlockchain.toArray()));
                print("Updated blockchain size: " + IBlockchain.size());
                print("stop");
                printDashedLine();
            } else {
                Message messageToSend = new Message();
                messageToSend.setContent("❌ Invalid Block");
                sendObject(blockSender, messageToSend);
                print("Block didnt recorded");
                print("stop");
                printDashedLine();
            }

            blockValidationResults.clear();
            blockValidationCounter = 0;
        }
    }

    private static void broadcast(Object message, Session session)
            throws IOException, EncodeException {
        chatEndpoints.forEach(endpoint -> {
            synchronized (endpoint) {
                if (endpoint.session != session) {
                    try {
                        endpoint.session.getBasicRemote().sendObject(message);
                    } catch (IOException | EncodeException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private static void broadcast(Object message)
            throws IOException, EncodeException {
        chatEndpoints.forEach(endpoint -> {
            synchronized (endpoint) {

                try {
                    endpoint.session.getBasicRemote().sendObject(message);
                } catch (IOException | EncodeException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private static void sendObject(String senderUserName, Object message)
            throws IOException, EncodeException {
        Session receiverSession = onlineSessions.get(senderUserName);
        synchronized (receiverSession) {
            try {
                receiverSession.getBasicRemote().sendObject(message);
            } catch (IOException | EncodeException e) {
                e.printStackTrace();
            }

        }
    }

    public String chooseTheBlockMaker(List<Message> messages) {
        String blockMaker = messages.get(0).getUserName();
        for (int i = 1; i < messages.size(); i++) {
            Message currentUser = messages.get(i);
            Message previousUser = messages.get(i - 1);
            blockMaker = ((currentUser.getBalance() > previousUser.getBalance()) ? currentUser.getUserName() : previousUser.getUserName());
            System.out.println("Block maker: "+blockMaker);
        }
        return blockMaker;
    }

    private void recordBlock(Object block) throws JsonProcessingException {
        String blockJson = objectMapper.writeValueAsString(block);
        Block blockToSave = objectMapper.readValue(blockJson, Block.class);

        blockService.save(blockToSave);
        IBlockchain.add(blockToSave);
        updateDBAccounts(blockToSave);
        updateLedger(blockToSave);
    }

    private void updateDBAccounts(Block block) throws JsonProcessingException {
        String transactionJson = objectMapper.writeValueAsString(block.getTransaction());
        JSONObject transaction = new JSONObject(transactionJson);

        Account sender = objectMapper.readValue(transaction.get("sender").toString(), Account.class);
        accountService.save(sender);

        String transactionType = transaction.get("transactionType").toString();
        Account receiver;
        if (transactionType.equals("Transfer Transaction")) {
            receiver = objectMapper.readValue(transaction.get("receiver").toString(), Account.class);
            accountService.save(receiver);
        }
    }

    public static void updateLedger(Block block) {
        List<Account> accountList = getLatestLedger(block);
        for (Account account : accountList) {
            ledger.put(account.getAccountNumber(), account);
        }
    }

    private boolean notValidBlockchain() {
        Block lastBlockInMemory = IBlockchain.get(IBlockchain.size() - 1);
        Block lastBlockInDB = blockService.getLastBlock();

        String memoHash = lastBlockInMemory.calculateHash();
        String dbHash = lastBlockInDB.calculateHash();

        boolean dbHacked = !(dbHash.equals(memoHash));
        if(dbHacked) {
            printDashedLine();
            print("Hacked Block: "+lastBlockInDB);
            print("Correct Block: "+lastBlockInMemory);
            printDashedLine();
        }
        return dbHacked;
    }

    private boolean notValidAccounts() {
        boolean dbHacked = false;
        List<Account> dbAccounts = accountService.getAllAccounts();
        if (dbAccounts!=null && !dbAccounts.isEmpty()) {
            List<Account> memoryAccounts = getLatestLedger(IBlockchain.get(IBlockchain.size()-1));

            Collections.sort(dbAccounts);
            Collections.sort(memoryAccounts);

            for (int i = 0; i < dbAccounts.size(); i++) {
                Account dbAccount = (dbAccounts.get(i)), memoryAccount = memoryAccounts.get(i);
                if (!(dbAccount.toString().equals(memoryAccount.toString()))) {
                    printDashedLine();
                    print("Hacked Account: " + dbAccount);
                    print("Correct Account: " + memoryAccount);
                    dbHacked = true;
                }
                if(dbHacked) printDashedLine();
            }
        }
        return dbHacked;
    }

    private static List<Account> getLatestLedger(Block block) {
        try {
            String transactionJson = objectMapper.writeValueAsString(block.getTransaction());
            JSONObject json = new JSONObject(transactionJson);
            String ledgerJson = json.get("ledger").toString();
            return objectMapper.readValue(ledgerJson, new TypeReference<List<Account>>(){});
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void fixTheDB(Block block) {
        blockService.removeLastBlock();
        blockService.save(block);
    }

    private void fixTheDB(List<Account> memoryAccounts) {
        accountService.deleteAll();
        accountService.save(memoryAccounts);
    }


    private static int printCounter = 1;
    public void print(String message) {
        if(message.equals("stop")) {
            printCounter=1;
        }
        else {
            System.out.println(printCounter +") "+message);
            printCounter++;
        }
    }

    private static void printDashedLine() {
        System.out.println("\n--------------------------------\n");
    }

}
