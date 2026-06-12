package com.hirehub.hirehubapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class HirehubApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(HirehubApiApplication.class, args);

		System.out.println("========================================");
		System.out.println("  HIREHUB API STARTED SUCCESSFULLY!    ");
		System.out.println("  Test endpoint: http://localhost:8080/api/test/hello");
		System.out.println("========================================");
	}

}
