package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.controller.TranslationController;
import ch.uzh.ifi.hase.soprafs24.service.TranslationService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Nested
@WebMvcTest(TranslationController.class)
class TranslationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TranslationService translationService;

    @Test
    public void translateText_validInput_ReturnTranslatedText() throws Exception {
        // Mocking translation service response
        String translatedText = "Hello";
        when(translationService.translateText(anyString(), anyString(), anyString())).thenReturn(translatedText);

        // Sending POST request to translate text
        MvcResult result = mockMvc.perform(post("/api/translate/en")
                        .content("{\"message\": \"Hallo\", \"sourceLang\": \"de\"}")
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Check the status code
        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());

    }
    @Test
    public void translateText_invalidInput_ReturnsBadRequest() throws Exception {
        // given
        TranslationRequest request = new TranslationRequest();
        request.setMessage(""); // leere Nachricht
        request.setSourceLang("en");
        String targetLang = "de";

        // when/then
        mockMvc.perform(post("/api/translate/{targetLang}", targetLang)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request))
                        .header("Authorization", "Bearer yourAccessToken"))
                .andExpect(status().isBadRequest());
    }
    @Test
    public void translateText_validatedInput_ReturnTranslatedText() throws Exception {
        // Mocking translation service response
        String expectedTranslatedText = "Hola, mundo!";
        when(translationService.translateText(anyString(), anyString(), anyString())).thenReturn(expectedTranslatedText);

        // Sending POST request to translate text
        MvcResult result = mockMvc.perform(post("/api/translate/es")
                        .content("{\"message\": \"Hello, world!\", \"sourceLang\": \"en\"}")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // Check the translated text in the response content
        String responseContent = result.getResponse().getContentAsString();
        assertEquals(expectedTranslatedText, responseContent);
    }
    @Test
    public void translateText_robustnessTest_ReturnsTranslatedText() throws Exception {
        // given
        TranslationRequest request = new TranslationRequest();
        request.setMessage("Hello");
        request.setSourceLang("en");
        String targetLang = "de"; // Zielsprache

        // Simulate a delay or failure in the external translation service
        // For simulation purpose, you can introduce a delay here.

        // when/then
        mockMvc.perform(post("/api/translate/{targetLang}", targetLang)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request))
                        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(isEmptyOrNullString())));
    }

    public static String asJsonString(final Object object) {
        try {
            return new ObjectMapper().writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}