/*
package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.service.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/translate")
public class TranslationController {


    @Autowired
    private TranslationService translationService;

    @PostMapping("/{targetLang}")
    public String translateText(@RequestBody TranslationRequest request, @PathVariable String targetLang) {
        // Überprüfen, ob die Nachricht leer ist
        if (request.getMessage() == null || request.getMessage().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Message cannot be empty");
        }

        // Überprüfen, ob die Ausgangssprache angegeben ist, andernfalls Standardausgangssprache verwenden
        String sourceLang = request.getSourceLang() != null ? request.getSourceLang() : "en"; // Beispiel: Standardausgangssprache ist Englisch

        // Übersetzen und zurückgeben
        return translationService.translateText(request.getMessage(), sourceLang, targetLang);
    }
}
class TranslationRequest {
    private String message;
    private String sourceLang;

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
*/
