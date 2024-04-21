package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.service.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/translate")
public class TranslationController {

    @Autowired
    private TranslationService translationService;
    @PostMapping("/{targetLang}")
    public ResponseEntity<String> translateText(@RequestBody TranslationRequest request, @PathVariable String targetLang) {
        try {
            String translatedText = translationService.translateText(request.getMessage(), request.getSourceLang(), targetLang);
            return ResponseEntity.ok(translatedText);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Translation failed: " + e.getMessage());
        }
    }
}

class TranslationRequest {
    private String message;
    private String sourceLang; // Can be null for auto-detection

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSourceLang() {
        return sourceLang;
    }

    public void setSourceLang(String sourceLang) {
        this.sourceLang = sourceLang;
    }
}
