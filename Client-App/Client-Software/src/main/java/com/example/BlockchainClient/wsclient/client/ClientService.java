package com.example.BlockchainClient.wsclient.client;

import com.example.BlockchainClient.account.Account;
import com.example.BlockchainClient.transaction.Transaction;

public interface ClientService {
    Transaction addNewAccount(Account newAccount);
    Transaction withdraw(Double amount);
    Transaction deposit(Double amount);
    Transaction transfer(String receiverAccountNumber, Double amount);
    Account getAccountInfo();
}
