package com.example.BlockchainClient.transaction;

public class InvaidTransaction extends Transaction {
    private String errorMessage;

    public InvaidTransaction(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
