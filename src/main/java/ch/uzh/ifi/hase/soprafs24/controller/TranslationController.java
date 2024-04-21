package ch.uzh.ifi.hase.soprafs24.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;

@RestController
public class TranslationController {

    private final Translate translate = TranslateOptions.getDefaultInstance().getService();

    @PostMapping("/translate/{language}")
    public String translateText(@RequestBody TranslationRequest request, @PathVariable String language) {
        Translation translation = translate.translate(
                request.getText(),
                Translate.TranslateOption.sourceLanguage(language),
                Translate.TranslateOption.targetLanguage(request.getTargetLang())
        );
        return translation.getTranslatedText();
    }

    public static class TranslationRequest {
        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getTargetLang() {
            return targetLang;
        }

        public void setTargetLang(String targetLang) {
            this.targetLang = targetLang;
        }

        private String text;
        private String targetLang;

        // Getters and setters
    }
}
