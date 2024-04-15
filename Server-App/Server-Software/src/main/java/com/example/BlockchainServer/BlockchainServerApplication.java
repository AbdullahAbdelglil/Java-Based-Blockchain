package com.example.BlockchainServer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BlockchainServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(BlockchainServerApplication.class, args);
		BlockchainServer.start();
	}

}
