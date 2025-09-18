package com.saravanatimbers.palletbuilderbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;

@EnableAsync
@EnableGlobalMethodSecurity(prePostEnabled = true)
@SpringBootApplication
public class PalletBuilderBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(PalletBuilderBackendApplication.class, args);
	}

} 