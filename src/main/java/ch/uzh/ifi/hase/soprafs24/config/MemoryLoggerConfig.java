package ch.uzh.ifi.hase.soprafs24.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@Profile("memoryLog")
public class MemoryLoggerConfig {

    @Scheduled(fixedRate = 60000)
    public void logMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        System.out.println("Free memory (bytes): " + runtime.freeMemory());
        System.out.println("Total memory (bytes): " + runtime.totalMemory());
        System.out.println("Max memory (bytes): " + runtime.maxMemory());
    }
}
