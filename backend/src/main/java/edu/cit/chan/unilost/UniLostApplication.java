package edu.cit.chan.unilost;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class UniLostApplication {

	public static void main(String[] args) {
		SpringApplication.run(UniLostApplication.class, args);
	}

}
