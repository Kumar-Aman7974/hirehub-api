

package com.hirehub.hirehubapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling  // Add this for scheduled job expiration
public class HirehubApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(HirehubApiApplication.class, args);
		System.out.println("========================================");
		System.out.println("  HIREHUB API STARTED SUCCESSFULLY!    ");
		System.out.println("   Job Management System Ready       ");
		System.out.println("   JWT Authentication Active         ");
		System.out.println("   API Base: http://localhost:8080   ");
		System.out.println("========================================");
	}
}