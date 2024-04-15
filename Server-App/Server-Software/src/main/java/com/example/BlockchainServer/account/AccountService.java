package com.example.BlockchainServer.account;

import java.util.List;

public interface AccountService {
    void save(Account account);
    void save(List<Account> accounts);
    Account getAccountByAccountNumber(String accountNumber);
    Account getAccountByClientId(String clientId);
    List<Account> getAllAccounts();
    void deleteAll();
}
