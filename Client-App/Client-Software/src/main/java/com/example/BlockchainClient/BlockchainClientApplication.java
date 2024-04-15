package com.example.BlockchainClient;

import com.example.BlockchainClient.wsclient.client.ClientServicesImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BlockchainClientApplication {
	public static void main(String[] args) {
		SpringApplication.run(BlockchainClientApplication.class, args);
		ClientServicesImpl.start();
	}
}
