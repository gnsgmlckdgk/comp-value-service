package com.finance.dart;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
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
