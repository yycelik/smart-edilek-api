package com.smart.edilek;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class EdilekApplication {

	public static void main(String[] args) {
		SpringApplication.run(EdilekApplication.class, args);
	}

}
