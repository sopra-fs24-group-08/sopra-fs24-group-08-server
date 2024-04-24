package ch.uzh.ifi.hase.soprafs24;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
//Run it all the time without profile while in dev
@Configuration
@EnableScheduling
public class MemoryLoggerConfig {

    @Scheduled(fixedRate = 60000)
    public void logMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long allocatedMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = allocatedMemory - freeMemory;

        System.out.println("Used memory in MB: " + usedMemory / 1024 / 1024);
        System.out.println("Free memory in MB: " + freeMemory / 1024 / 1024);
        System.out.println("Total memory in MB: " + allocatedMemory / 1024 / 1024);
        System.out.println("Max memory in MB: " + maxMemory / 1024 / 1024);
    }
}
