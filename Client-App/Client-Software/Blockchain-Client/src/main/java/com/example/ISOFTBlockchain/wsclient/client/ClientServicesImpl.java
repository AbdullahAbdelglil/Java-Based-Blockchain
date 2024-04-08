package com.example.ISOFTBlockchain.wsclient.client;

import com.example.ISOFTBlockchain.account.Account;


import com.example.ISOFTBlockchain.wsclient.message.Message;
import com.example.ISOFTBlockchain.constants.Constants;
import com.example.ISOFTBlockchain.wsclient.message.MessageType;
import com.example.ISOFTBlockchain.transaction.Transaction;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class ClientServicesImpl implements ClientService{
    public static Client client;
    private static Environment environment;

    //-------------------------------------------------Constructor------------------------------------------------------

    public ClientServicesImpl(Environment environment) {
        ClientServicesImpl.environment = environment;
    }

    //----------------------------------------------------Init----------------------------------------------------------

    public static void init() {
        String username = environment.getProperty("user");
        client = new Client(username);
    }

    //----------------------------------------------Account Services----------------------------------------------------

    public Transaction addNewAccount(Account newAccount) {
        Account account = new Account(newAccount.getOwnerName(), newAccount.getBalance());
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

        Transaction transaction = buildTransaction(Constants.TRANSFER, sender, receiver, amount);
        sendTransactionRequest(transaction);
        return transaction;
    }

    private void sendTransactionRequest(Transaction transaction) {
        client.print("Waiting for transaction validation result ");

        Message message = new Message();
        message.setType(MessageType.BROADCAST_TRANSACTION);
        message.setTransaction(transaction);
        client.sendMessage(message);
    }


    //---------------------------------------------Helper Methods-------------------------------------------------------

    private Transaction buildTransaction(String transactionType, Account sender, Account receiver, Double amount) {
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
