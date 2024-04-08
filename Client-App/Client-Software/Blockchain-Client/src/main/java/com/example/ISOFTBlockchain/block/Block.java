package com.example.ISOFTBlockchain.block;

import com.example.ISOFTBlockchain.transaction.Transaction;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.data.mongodb.core.mapping.Document;
import java.security.MessageDigest;

public class Block {
    private String hash;
    private String previousHash;
    private int nonce;
    private Long timestamp;
    private Transaction transaction;


    public Block() {
    }

    public Block(Transaction transaction, String previousHash) {
        this.transaction = transaction;
        this.timestamp = System.currentTimeMillis();
        this.previousHash = previousHash;
        this.nonce = 0;
        setHash();
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getPreviousHash() {
        return previousHash;
    }

    public void setPreviousHash(String previousHash) {
        this.previousHash = previousHash;
    }

    public int getNonce() {
        return nonce;
    }

    public void setNonce(int nonce) {
        this.nonce = nonce;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public void setHash() {
        this.hash = calculateHash();
    }

    public String calculateHash() {
        String data = transaction.toString() + timestamp + previousHash + nonce;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] hash = digest.digest(data.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hexadecimal = Integer.toHexString(0xff & b);
                if (hexadecimal.length() == 1) hexString.append('0');
                hexString.append(hexadecimal);
            }
            return hexString.toString();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void incrementNonce() {
        this.nonce++;
    }


    @Override
    public String toString() {
        return "{" +
                " timestamp=" + timestamp +
                ", blockHash='" + hash + '\'' +
                ", previousHash='" + previousHash + '\'' +
                ", nonce=" + nonce +
                ", transaction=" + transaction +
                '}';
    }
}