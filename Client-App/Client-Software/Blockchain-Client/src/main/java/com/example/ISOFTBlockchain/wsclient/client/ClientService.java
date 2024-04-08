package com.example.ISOFTBlockchain.wsclient.client;

import com.example.ISOFTBlockchain.account.Account;
import com.example.ISOFTBlockchain.transaction.Transaction;

public interface ClientService {
    Transaction addNewAccount(Account newAccount);
    Transaction withdraw(Double amount);
    Transaction deposit(Double amount);
    Transaction transfer(String receiverAccountNumber, Double amount);

}
