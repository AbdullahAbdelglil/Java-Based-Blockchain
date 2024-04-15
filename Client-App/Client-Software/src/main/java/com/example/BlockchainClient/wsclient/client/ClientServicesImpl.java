package com.example.BlockchainClient.wsclient.client;

import com.example.BlockchainClient.account.Account;


import com.example.BlockchainClient.transaction.InvaidTransaction;
import com.example.BlockchainClient.wsclient.message.Message;
import com.example.BlockchainClient.constants.Constants;
import com.example.BlockchainClient.wsclient.message.MessageType;
import com.example.BlockchainClient.transaction.Transaction;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class ClientServicesImpl implements ClientService {
    public static Client client;
    private static Environment environment;
    private static Scanner in = new Scanner(System.in);
    private static String CLIENT_ID ;

    //-------------------------------------------------Constructor------------------------------------------------------

    public ClientServicesImpl(Environment environment) {
        ClientServicesImpl.environment = environment;
        CLIENT_ID = environment.getProperty("username");
    }

    //----------------------------------------------------Init----------------------------------------------------------

    public static void start() {
        client = new Client(CLIENT_ID);
    }

    private static void register() {
        String name;
        double balance;

        System.out.print("Enter you name: "); name = in.nextLine();
        System.out.print("Enter initial balance: "); balance = in.nextDouble();

        in.nextLine();
        in.close();

        client = new Client(CLIENT_ID);
        addNewAccount(name, balance);
    }

    //----------------------------------------------Account Services----------------------------------------------------


    public static Account addNewAccount(String name, Double balance) {
        Account account = new Account(name, balance);
        while (client.getLedger().get(account.getAccountNumber()) != null) {
            account.setAccountNumber();
        }
        account.setClientID(CLIENT_ID);
        client.setAccount(account);

        Transaction transaction = buildTransaction(Constants.ACCOUNT_CREATED, account, null, account.getBalance());
        sendTransactionRequest(transaction);

        return account;
    }

    public Transaction addNewAccount(Account newAccount) {
        Account account = new Account(newAccount.getUserName(), newAccount.getBalance());
        while (client.getLedger().get(account.getAccountNumber()) != null) {
            account.setAccountNumber();
        }
        client.setAccount(account);

        Transaction transaction = buildTransaction(Constants.ACCOUNT_CREATED, account, null, account.getBalance());
        sendTransactionRequest(transaction);
        return transaction;
    }

    public Transaction withdraw(Double amount) {
        Account account = client.getAccount();
        Transaction transaction = buildTransaction(Constants.WITHDRAW, account, null, amount);
        sendTransactionRequest(transaction);
        return transaction;
    }

    public Transaction deposit(Double amount) {
        Account account = client.getAccount();
        Transaction transaction = buildTransaction(Constants.DEPOSIT, account, null, amount);
        sendTransactionRequest(transaction);
        return transaction;
    }

    public Transaction transfer(String receiverAccountNumber, Double amount) {
        Account sender = client.getAccount();
        Account receiver = client.getLedger().get(receiverAccountNumber);
        if(receiver==null) {
            return new InvaidTransaction("Account not found");
        }

        Transaction transaction = buildTransaction(Constants.TRANSFER, sender, receiver, amount);
        sendTransactionRequest(transaction);
        return transaction;
    }

    @Override
    public Account getAccountInfo() {
        return client.getAccount();
    }

    private static void sendTransactionRequest(Transaction transaction) {
        client.print("Waiting for transaction validation result ");

        Message message = new Message();
        message.setType(MessageType.BROADCAST_TRANSACTION);
        message.setTransaction(transaction);
        client.sendMessage(message);
    }


    //---------------------------------------------Helper Methods-------------------------------------------------------

    private static Transaction buildTransaction(String transactionType, Account sender, Account receiver, Double amount) {
        Transaction transaction = new Transaction();

        transaction.setTransactionType(transactionType);
        transaction.setSender(sender);
        transaction.setReceiver(receiver);
        transaction.setAmount(amount);

        client.getLedger().put(sender.getAccountNumber(), sender);
        if(receiver!=null) {
            client.getLedger().put(receiver.getAccountNumber(), receiver);
        }
        transaction.setLedger(new ArrayList<>(client.getLedger().values()));

        return transaction;
    }

}
