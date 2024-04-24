/*
package ch.uzh.ifi.hase.soprafs24.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class ProfileCheck {

    private final Environment environment;

  //  @Value("${translate.api-key}")
    private String apiKey;

    public ProfileCheck(Environment environment) {
        this.environment = environment;
        displayActiveProfiles();
        displayApiKey();
    }

    private void displayActiveProfiles() {
        String[] activeProfiles = environment.getActiveProfiles();
        if (activeProfiles.length > 0) {
            System.out.println("Active Profiles: " + String.join(", ", activeProfiles));
        } else {
            System.out.println("No active profiles.");
        }
    }

    private void displayApiKey() {
        System.out.println("Current API Key: " + apiKey);
    }
}

*/
