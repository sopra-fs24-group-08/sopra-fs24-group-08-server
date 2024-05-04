package ch.uzh.ifi.hase.soprafs24.rest.dto;

public class TranslationRequest {
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
