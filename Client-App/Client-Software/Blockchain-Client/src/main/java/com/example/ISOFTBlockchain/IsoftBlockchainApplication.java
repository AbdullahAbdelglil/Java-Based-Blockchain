package com.example.ISOFTBlockchain;

import com.example.ISOFTBlockchain.wsclient.client.ClientServicesImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class IsoftBlockchainApplication {
	public static void main(String[] args) {
		SpringApplication.run(IsoftBlockchainApplication.class, args);
		ClientServicesImpl.init();
	}
}
