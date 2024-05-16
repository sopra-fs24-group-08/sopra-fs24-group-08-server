package ch.uzh.ifi.hase.soprafs24.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class RestTemplateConfigTest {

    @Autowired
    private ApplicationContext context;

    @Test
    public void restTemplateBeanExists() {
        // Assert that a RestTemplate bean is indeed configured
        assertTrue(context.containsBean("restTemplate"), "RestTemplate bean should exist in the application context.");

        // Assert that the bean can be retrieved and is of the correct type
        assertNotNull(context.getBean(RestTemplate.class), "RestTemplate bean should not be null.");
    }
}