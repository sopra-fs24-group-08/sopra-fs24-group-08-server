package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.service.TranslateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/translate")
public class TranslateController {

    @Autowired
    private TranslateService translateService;

    @GetMapping
    public String translate(@RequestParam String text, @RequestParam String targetLang) {
        System.out.println("Received text: " + text);
        String translatedText = translateService.translateText(text, targetLang);
        System.out.println("Translated text: " + translatedText);
        return translatedText;
    }
}
