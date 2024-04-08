package com.example.BlockchainServer.account;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends MongoRepository<Account, String> {
    Account getAccountByAccountNumber(String accountNumber);
}
