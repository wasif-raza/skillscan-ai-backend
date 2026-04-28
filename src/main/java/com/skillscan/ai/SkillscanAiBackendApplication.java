package com.skillscan.ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.TimeZone;

@EnableCaching
@SpringBootApplication
@EnableScheduling
public class SkillscanAiBackendApplication {

	public static void main(String[] args) {

		// Set timezone at JVM level
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));
		System.out.println(new BCryptPasswordEncoder().encode("Wasifraza1"));
		SpringApplication.run(SkillscanAiBackendApplication.class, args);
	}

}
