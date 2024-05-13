/*
package ch.uzh.ifi.hase.soprafs24.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

@Service
public class TranslationService {

    @Value("AIzaSyBa6V3OcgeYaX-r1w8ilrrN3HqZ6JKXZZY")
    private String apiKey;

    public String translateText(String text, String sourceLang, String targetLang) {
        String url = "https://translation.googleapis.com/language/translate/v2?key=" + apiKey;
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
       // headers.add("Authorization", "Bearer " + apiKey);  // API key usage

        String requestJson = String.format("{\"q\": \"%s\", \"source\": \"%s\", \"target\": \"%s\", \"format\": \"text\"}", text, sourceLang, targetLang);
        HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
        return response.getBody();
    }
}

*/
