package com.webkorps.sync_db;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SyncDbApplication {

	public static void main(String[] args) {
		SpringApplication.run(SyncDbApplication.class, args);
	}

}
