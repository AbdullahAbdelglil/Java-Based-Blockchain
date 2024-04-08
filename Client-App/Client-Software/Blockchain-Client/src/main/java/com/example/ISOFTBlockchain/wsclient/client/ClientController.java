package com.example.ISOFTBlockchain.wsclient.client;

import com.example.ISOFTBlockchain.account.Account;
import com.example.ISOFTBlockchain.block.Block;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;


@RestController
@RequestMapping("/clients/account")
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class ClientController {

    private final ClientService clientServices;

    public ClientController(ClientServicesImpl clientServices) {
        this.clientServices = clientServices;
    }

    @PostMapping("/register")
    public Object addAccount(@RequestBody Account account) {
        return clientServices.addNewAccount(account);
    }

    @GetMapping("/login/{accountNumber}")
    public void loginToAccount(@PathVariable("accountNumber") String accountNumber) {
        //clientServices.login(accountNumber);
    }

    @PutMapping("/deposit/{amount}")
    public Object deposit(@PathVariable("amount") Double amount) {

        return clientServices.deposit(amount);

    }

    @PutMapping("/withdraw/{amount}")
    public Object withdraw(@PathVariable("amount") Double amount) {
        return clientServices.withdraw(amount);
    }

    @PutMapping("/transfer/{amount}/to/{receiver}")
    public Object transfer(@PathVariable("receiver") String receiverAccountNumber,
                           @PathVariable("amount") Double amount) {

        return clientServices.transfer(receiverAccountNumber, amount);
    }

    @GetMapping("/blockchain")
    public List<Block> getBlockchain() {
        return ClientServicesImpl.client.getBlockchain();
    }

    @GetMapping("/ledger")
    public List<Account> getLedger () {
        return new ArrayList<>(ClientServicesImpl.client.getLedger().values());
    }

}
