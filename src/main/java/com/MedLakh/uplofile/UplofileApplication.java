package com.MedLakh.uplofile;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class UplofileApplication {

	public static void main(String[] args) {
		SpringApplication.run(UplofileApplication.class, args);
	}

}
