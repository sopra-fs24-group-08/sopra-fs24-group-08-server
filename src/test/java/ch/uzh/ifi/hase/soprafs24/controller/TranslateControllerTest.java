package ch.uzh.ifi.hase.soprafs24.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.eq;
import ch.uzh.ifi.hase.soprafs24.service.TranslateService;
import org.junit.jupiter.api.Test;

@WebMvcTest(TranslateController.class)
public class TranslateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TranslateService translateService;

    @Test
    public void testTranslate() throws Exception {
        String inputText = "Original Text";
        String targetLang = "de";
        String expectedTranslatedText = "Übersetzter Text";
        when(translateService.translateText(inputText, targetLang)).thenReturn(expectedTranslatedText);

        mockMvc.perform(get("/api/translate")
                        .param("text", inputText)
                        .param("targetLang", targetLang))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedTranslatedText));

        verify(translateService).translateText(eq(inputText), eq(targetLang));
    }
}
