package com.example.BlockchainClient.account;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Random;

public class Account implements Comparable<Account>  ,Serializable {

    private static final long serialVersionUID = 129348938L;

    private String accountNumber;
    private String userName;
    private String clientID;
    private String clientIP;
    private Double balance;
    private Long creationDate;
    public Account() {
    }

    public Account(String userName, Double balance) {
        this.userName = userName;
        this.balance = balance;
        this.accountNumber = generateAccountNumber(userName);
        this.creationDate = System.currentTimeMillis();
        try {
            this.clientIP = InetAddress.getLocalHost().getHostAddress();
        }catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
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
        this.accountNumber =generateAccountNumber(this.userName);
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

    public String getClientIP() {
        return clientIP;
    }

    @Override
    public String toString() {
        return "Account{" +
                "accountNumber='" + accountNumber + '\'' +
                ", userName='" + userName + '\'' +
                ", clientID='" + clientID + '\'' +
                ", clientIP='" + clientIP + '\'' +
                ", balance=" + balance +
                ", creationDate=" + creationDate +
                '}';
    }

    @Override
    public int compareTo(Account account) {
        return this.creationDate.compareTo(account.creationDate);
    }
    public static String generateAccountNumber(String owner){
        StringBuilder accountNumber = new StringBuilder();

        accountNumber.append(owner.substring(0, 2).toUpperCase());
        accountNumber.append("00");

        Random random = new Random();
        int randomNumber = random.nextInt(900000) + 100000;

        accountNumber.append(randomNumber);
        accountNumber.append("0001");

        return accountNumber.toString();
    }
}
