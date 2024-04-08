package com.example.ISOFTBlockchain.account;

import org.springframework.data.annotation.LastModifiedBy;

import java.io.Serializable;
import java.util.Random;

public class Account implements Comparable<Account>  ,Serializable {

    private static final long serialVersionUID = 129348938L;

    private String accountNumber;
    private String ownerName;
    private Double balance;
    private Long creationDate;

    public Account() {
    }

    public Account(String ownerName, Double balance) {
        this.ownerName = ownerName;
        this.balance = balance;
        this.accountNumber = generateAccountNumber(ownerName);
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
        this.accountNumber = generateAccountNumber(ownerName);
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
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


    @Override
    public String toString() {
        return "Account{" +
                "accountNumber='" + accountNumber + '\'' +
                ", ownerName='" + ownerName + '\'' +
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
