package ch.uzh.ifi.hase.soprafs24.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class TranslateService {

    @Value("${TRANSLATE_KEY}")
    private String apiKey;

    private final RestTemplate restTemplate;

    public TranslateService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String translateText(String text, String targetLang) {
        try {
            // Manually construct the URL
            String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8);
            String url = "https://translation.googleapis.com/language/translate/v2?key=" + apiKey +
                    "&q=" + encodedText +
                    "&target=" + targetLang;

            System.out.println("Request URL: " + url);
            String rawResponse = restTemplate.getForObject(url, String.class);
            System.out.println("Raw JSON response: " + rawResponse);

            ObjectMapper mapper = new ObjectMapper();
            TranslationResponse response = mapper.readValue(rawResponse, TranslationResponse.class);
            return response.getData().getTranslations().get(0).getTranslatedText();
        } catch (Exception e) {
            System.out.println("Error during translation API call: " + e.getMessage());
            return "Translation API call failed";
        }
    }

    private static class TranslationResponse {
        private TranslationData data;

        public TranslationData getData() {
            return data;
        }

        public void setData(TranslationData data) {
            this.data = data;
        }
    }

    private static class TranslationData {
        private List<Translation> translations;

        public List<Translation> getTranslations() {
            return translations;
        }

        public void setTranslations(List<Translation> translations) {
            this.translations = translations;
        }
    }

    private static class Translation {
        private String translatedText;
        private String detectedSourceLanguage;

        public String getTranslatedText() {
            return translatedText;
        }

        public void setTranslatedText(String translatedText) {
            this.translatedText = translatedText;
        }

        public String getDetectedSourceLanguage() {
            return detectedSourceLanguage;
        }

        public void setDetectedSourceLanguage(String detectedSourceLanguage) {
            this.detectedSourceLanguage = detectedSourceLanguage;
        }
    }
}
