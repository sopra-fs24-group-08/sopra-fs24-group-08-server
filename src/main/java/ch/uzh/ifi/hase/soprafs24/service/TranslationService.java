package ch.uzh.ifi.hase.soprafs24.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class TranslationService {

   // @Value("${TRANSLATE_API_KEY}")
    private String translationApiKey;
    //Testing for myself, will make new key once I share this code.
    private final String Apikey = "AIzaSyD3GQtfEI_r1PmsOt5csasJXPDCyq3Z280ey";

    public String translateText(String text, String sourceLang, String targetLang) {
        String url = "https://translation.googleapis.com/language/translate/v2";
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", "Bearer " +Apikey);

        // Construct the request JSON, handle auto language detection
        String requestJson = "{\"q\": \"" + text + "\", \"target\": \"" + targetLang + "\"";
        if (sourceLang != null && !sourceLang.isEmpty()) {
            requestJson += ", \"source\": \"" + sourceLang + "\"";
        }
        requestJson += ", \"format\": \"text\"}";

        HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        return response.getBody();
    }
}
