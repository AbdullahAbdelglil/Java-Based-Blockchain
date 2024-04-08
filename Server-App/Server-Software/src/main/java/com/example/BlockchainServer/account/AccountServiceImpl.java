package com.example.BlockchainServer.account;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    @Autowired
    public AccountServiceImpl(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public void save(Account account) {
        accountRepository.save(account);
    }

    public void save(List<Account> accounts) {
        for(Account account:accounts) {
            save(account);
        }
    }

    public Account getAccountByAccountNumber(String accountNumber) {
        return accountRepository.getAccountByAccountNumber(accountNumber);
    }

    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
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

    public void deleteAll() {
        accountRepository.deleteAll();
    }
}
