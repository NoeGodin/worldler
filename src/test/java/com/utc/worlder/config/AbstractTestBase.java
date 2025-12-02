package com.utc.worlder.config;

import com.utc.worlder.entity.Country;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.TestPropertySource;

import java.time.Duration;
import java.time.Instant;

@ExtendWith(MockitoExtension.class)
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.sql.init.mode=never",
    "logging.level.org.hibernate.SQL=DEBUG"
})
public abstract class AbstractTestBase {
    
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected Instant testStartTime;

    @BeforeEach
    void setUpBase(TestInfo testInfo) {
        testStartTime = Instant.now();
        logger.info("Starting test: {} in class {}",
            testInfo.getDisplayName(), 
            testInfo.getTestClass().map(Class::getSimpleName).orElse("Unknown"));
    }

    @AfterEach
    protected void logTestDuration(TestInfo testInfo) {
        Duration duration = Duration.between(testStartTime, Instant.now());
        logger.info("✅ Test {} completed in {} ms",
            testInfo.getDisplayName(), 
            duration.toMillis());
    }

    protected Country createTestCountry(String name, String isoCode) {
        return new Country(
            name,
            isoCode,
            "Capital of " + name,
            "Europe",
            5000000L,
            100000.0,
            "Euro",
            "English"
        );
    }
}