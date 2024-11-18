package com.example.pytainer;

import com.example.pytainer.component.ProcessManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

// TODO: The exclude can be removed, but some dependency is fucking with me, and I don't know which one
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
@EnableMongoRepositories
public class PyTainerApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(PyTainerApplication.class, args);

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			ProcessManager processManager = context.getBean(ProcessManager.class);
			processManager.shutdownAllProcesses();
		}));
	}
}
