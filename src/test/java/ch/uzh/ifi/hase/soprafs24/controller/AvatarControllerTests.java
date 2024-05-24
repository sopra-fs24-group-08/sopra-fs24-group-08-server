package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.rest.dto.AvatarDTO;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.client.RestTemplate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import java.util.Optional;



@ExtendWith(SpringExtension.class)
@WebMvcTest(AvatarController.class)
public class AvatarControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private RestTemplate restTemplate;

    @Test
    public void testGetCatAvatar() throws Exception {
        String catName = "carrot";
        byte[] imageContent = new byte[10]; // Assume this is the image content
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);
        ResponseEntity<byte[]> responseEntity = new ResponseEntity<>(imageContent, headers, HttpStatus.OK);

        Mockito.when(restTemplate.getForEntity(
                ArgumentMatchers.eq("https://cat-avatars.vercel.app/api/cat?name=" + catName),
                ArgumentMatchers.eq(byte[].class)
        )).thenReturn(responseEntity);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/proxy/cat-avatar")
                        .param("name", catName))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.IMAGE_JPEG))
                .andExpect(MockMvcResultMatchers.content().bytes(imageContent));
    }

    @Test
    public void testUpdateAvatar_Success() throws Exception {
        Long userId = 1L;
        String avatarUrl = "http://example.com/new-avatar.jpg";
        String payload = "{\"imageUrl\":\"" + avatarUrl + "\"}";

        Mockito.doNothing().when(userService).updateUserAvatar(userId, avatarUrl);

        mockMvc.perform(MockMvcRequestBuilders.put("/users/{id}/updateIcon", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("Avatar updated successfully"));

        Mockito.verify(userService).updateUserAvatar(userId, avatarUrl);
    }

    @Test
    public void testUpdateAvatar_Failure() throws Exception {
        Long userId = 1L;
        String avatarUrl = "http://example.com/new-avatar.jpg";
        String payload = "{\"imageUrl\":\"" + avatarUrl + "\"}";

        Mockito.doThrow(new RuntimeException("Failed to update avatar")).when(userService).updateUserAvatar(userId, avatarUrl);

        mockMvc.perform(MockMvcRequestBuilders.put("/users/{id}/updateIcon", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().string("Error updating avatar: Failed to update avatar"));

        Mockito.verify(userService).updateUserAvatar(userId, avatarUrl);
    }


}

