package com.finance.dart;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@Slf4j
@SpringBootApplication
@EnableCaching
@EnableScheduling
public class FinReportApplication {

	public static void main(String[] args) {
		startupLogging();
		SpringApplication.run(FinReportApplication.class, args);
	}

	public static void startupLogging() {
		log.info("📝 JVM file.encoding = {}", System.getProperty("file.encoding"));
		log.info("📝 JVM defaultCharset = {}", java.nio.charset.Charset.defaultCharset());
	}
}
