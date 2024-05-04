package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.rest.dto.TranslationRequest;
import ch.uzh.ifi.hase.soprafs24.service.TranslationService;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class TranslationController {

    private final TranslationService translationService;

    TranslationController(TranslationService translationService) {
        this.translationService = translationService;
    }


    @PostMapping("/api/translate/{targetLang}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResponseEntity<String> translateText(@RequestBody TranslationRequest request, @PathVariable String targetLang) {
        System.out.println(request.getMessage());
        try {
            String translatedText = translationService.translateText(request.getMessage(), request.getSourceLang(), targetLang);
            System.out.println(translatedText);
            return ResponseEntity.ok(translatedText);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Translation failed: " + e.getMessage());
        }
    }
}

