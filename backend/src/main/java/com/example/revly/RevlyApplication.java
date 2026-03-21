package com.example.revly;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RevlyApplication {

	public static void main(String[] args) {
		SpringApplication.run(RevlyApplication.class, args);
	}

}
