package com.example.BlockchainServer.block;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface BlockRepository extends MongoRepository<Block, String> {
//    List<Block> getBlocksByTimestampBetweenAndTransactionSenderAccountNumberEqualsOrTransactionReceiverAccountNumberEquals(Long start, Long end, String transaction_sender_accountNumber, String transaction_receiver_accountNumber);
//
//    List<Block> getBlocksByTransactionSenderAccountNumberEqualsOrTransactionReceiverAccountNumberEquals(String accountNumber, String account_Number);
//
//    Block getBlockByTimestampEqualsAndTransactionSenderAccountNumberEqualsOrTransactionReceiverAccountNumberEquals(Long timestamp, String transaction_sender_accountNumber, String transaction_receiver_accountNumber);
//
//    List<Block> getBlocksByTransactionSenderAccountNumberEqualsOrTransactionReceiverAccountNumberEqualsOrderByTimestampDesc(String accountNumber, String account_Number);

    void deleteByHash(String hash);

}
