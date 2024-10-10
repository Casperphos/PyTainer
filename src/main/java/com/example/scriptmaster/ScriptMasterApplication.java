package com.example.scriptmaster;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

// TODO: The exclude can be removed, but some dependency is fucking with me, and I don't know which one
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
@EnableMongoRepositories
public class ScriptMasterApplication {

	public static void main(String[] args) {
		SpringApplication.run(ScriptMasterApplication.class, args);
	}
}
