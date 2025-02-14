package com.finance.dart;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class SomeRepositoryTest {

    // 1) DB 이미지를 불러와 테스트 컨테이너를 실행
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14.4")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    // 2) Spring Boot가 사용할 DB 접속 정보를 동적으로 설정
    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Test
    void contextLoads() {
        // 스프링 컨텍스트와 DB가 정상적으로 뜨는지 확인하는 기본 테스트
    }

    // 그 외 Repository나 Service, Controller 테스트 등...
}
