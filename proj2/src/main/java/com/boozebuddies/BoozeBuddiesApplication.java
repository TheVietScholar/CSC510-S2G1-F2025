package com.boozebuddies;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BoozeBuddiesApplication {
	private BoozeBuddiesApplication() {
		// Prevent instantiation
	}

	public static void main(String[] args) {
		SpringApplication.run(BoozeBuddiesApplication.class, args);
	}
}
