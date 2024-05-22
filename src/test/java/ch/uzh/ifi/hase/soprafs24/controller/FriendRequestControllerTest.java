package ch.uzh.ifi.hase.soprafs24.controller;


import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import ch.uzh.ifi.hase.soprafs24.service.FriendService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.*;

@WebMvcTest(FriendRequestController.class)
public class FriendRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private FriendService friendService;

    @Test
    public void deleteFriend_validUser_ReturnFriends() throws Exception {
      Long userId = 1L;
      Long friendId = 2L;
      String token = "Bearer someAuthToken";
      User friend = new User();
      friend.setId(3L);
      friend.setUsername("testName");
      List<User> friends = new ArrayList<>();
      friends.add(friend);
      given(friendService.getFriendsQuery(eq(userId))).willReturn(friends);

      mockMvc.perform(put("/users/{userId}/friends/delete", userId)
              .header("Authorization", token) 
              .param("FriendId", friendId.toString()))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$", hasSize(1)))
              .andExpect(jsonPath("$[0].id", is(friend.getId().intValue())))
              .andExpect(jsonPath("$[0].username", is(friend.getUsername())));
    } 

    @Test
    public void getAllUsers_validInput_ReturnFriends() throws Exception {
      Long userId = 1L;
      Long friendId = 2L;
      String token = "Bearer someAuthToken";
      User friend = new User();
      friend.setId(3L);
      friend.setUsername("testName");
      List<User> friends = new ArrayList<>();
      friends.add(friend);
      given(friendService.getFriendsQuery(eq(userId))).willReturn(friends);

      mockMvc.perform(get("/users/{userId}/friends", userId)
              .header("Authorization", token) 
              .param("FriendId", friendId.toString()))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$", hasSize(1)))
              .andExpect(jsonPath("$[0].id", is(friend.getId().intValue())))
              .andExpect(jsonPath("$[0].username", is(friend.getUsername())));
    } 
}

