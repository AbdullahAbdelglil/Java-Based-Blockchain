package com.example.BlockchainClient.wsclient.client;

import com.example.BlockchainClient.account.Account;
import com.example.BlockchainClient.block.Block;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;


@RestController
@RequestMapping("/client")
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class ClientController {

    private final ClientService clientServices;

    public ClientController(ClientServicesImpl clientServices) {
        this.clientServices = clientServices;
    }

    @GetMapping("/account-info")
    public Account getAccountInfo() {
        return clientServices.getAccountInfo();
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
    public List<Account> getLedger() {
        return new ArrayList<>(ClientServicesImpl.client.getLedger().values());
    }

}
