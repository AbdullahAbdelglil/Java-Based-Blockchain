package com.example.BlockchainServer.block;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.security.MessageDigest;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@Document(collection = "blockchain")

public class Block implements Serializable {

    private static final Long serialNumber = 12345678911L;
    private String hash;
    private String previousHash;
    private int nonce;
    private Long timestamp;
    private Object transaction;


    public Block() {
    }

    public Block(Object transaction, String previousHash) {
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

    public Object getTransaction() {
        return transaction;
    }

    public void setTransaction(Object transaction) {
        this.transaction = transaction;
    }

    public void setHash() {
        this.hash = calculateHash();
    }

    public String calculateHash() {
        String data =
                this.transaction.toString() +
                this.timestamp +
                this.previousHash +
                this.nonce;
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