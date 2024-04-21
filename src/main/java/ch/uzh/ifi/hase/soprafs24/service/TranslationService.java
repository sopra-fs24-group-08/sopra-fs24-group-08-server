package ch.uzh.ifi.hase.soprafs24.service;

import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;

public class TranslationService {

    private static final String API_KEY = "{key here}";
    private static final String TRANSLATE_URL = "https://translation.googleapis.com/language/translate/v2?key=" + API_KEY;

    public String translateText(String text, String targetLang) {
        RestTemplate restTemplate = new RestTemplate();
        String requestJson = buildRequestJson(text, targetLang);

        ResponseEntity<String> response = restTemplate.postForEntity(TRANSLATE_URL, requestJson, String.class);

        // Parse the response to extract the translated text
        return parseTranslatedText(response.getBody());
    }

    private String buildRequestJson(String text, String targetLang) {
        // Build JSON request string
        return "{ 'q': '" + text + "', 'target': '" + targetLang + "' }";
    }

    private String parseTranslatedText(String jsonResponse) {
        // Implement JSON parsing logic
        // Example: Use a JSON library to parse and return the translated text
        return jsonResponse; // Simplified return for example purposes
    }
}
