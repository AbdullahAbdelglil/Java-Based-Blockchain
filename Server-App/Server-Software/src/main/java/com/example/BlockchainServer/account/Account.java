package com.example.BlockchainServer.account;


import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

@Document(collection ="accounts")
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class Account implements Comparable<Account>  ,Serializable {

    private static final long serialVersionUID = 129348938L;

    @Id
    private String accountNumber;
    private String userName;
    private String clientID;
    private Double balance;
    private Long creationDate;
    public Account() {
    }

    public Account(String userName, Double balance) {
        this.userName = userName;
        this.balance = balance;
        this.accountNumber = AccountServiceImpl.generateAccountNumber(userName);
        this.creationDate = System.currentTimeMillis();
    }

    public void withdraw(Double amount) {
        this.balance -= amount;
    }

    public void deposit(Double amount) {
        this.balance += amount;
    }

    public void transfer(Double amount, Account receiver) {
        this.balance -= amount;
        receiver.balance += amount;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber() {
        this.accountNumber = AccountServiceImpl.generateAccountNumber(this.userName);
    }
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public Long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Long creationDate) {
        this.creationDate = creationDate;
    }

    public String getClientID() {
        return clientID;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    @Override
    public String toString() {
        return "Account{" +
                "accountNumber='" + accountNumber + '\'' +
                ", ownerName='" + userName + '\'' +
                ", balance=" + balance +
                ", creationDate=" + creationDate +
                '}';
    }

    @Override
    public int compareTo(Account account) {
        return this.creationDate.compareTo(account.creationDate);
    }
}
