package com.alice.project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class AliceDiaryApplication {

	public static void main(String[] args) {
		System.setProperty("server.servlet.context-path", "/AliceDiary");
		SpringApplication.run(AliceDiaryApplication.class, args);
	}
}
