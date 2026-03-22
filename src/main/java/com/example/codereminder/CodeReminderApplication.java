package com.example.codereminder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class CodeReminderApplication {

	public static void main(String[] args) {
		SpringApplication.run(CodeReminderApplication.class, args);
	}

}
