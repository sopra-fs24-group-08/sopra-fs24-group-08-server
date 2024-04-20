package ch.uzh.ifi.hase.soprafs24.controller;


import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.entity.FriendRequest;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs24.rest.dto.*;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import ch.uzh.ifi.hase.soprafs24.service.FriendService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.server.ResponseStatusException;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FriendRequestController.class)
public class FriendRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private FriendService friendService;

    @Test
    public void addFriend_validInput_ReturnFriend() throws Exception {
        // given
        User friend = new User();
        friend.setId(2L);
        friend.setUsername("testName");
        FriendGetDTO friendGetDTO = new FriendGetDTO();
        friendGetDTO.setId(2L);
        friendGetDTO.setUsername("testName");

        Mockito.doNothing().when(userService).authenticateUser(Mockito.any(), Mockito.any());
        given(friendService.addFriendRequest(Mockito.any(), Mockito.any())).willReturn(friend);

        mockMvc.perform(post("/users/{userId}/friends/add", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(friendGetDTO))
                        .header("token", "1"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(friend.getId().intValue())))
                .andExpect(jsonPath("$.username", is(friend.getUsername())));
    }

    /**
     * Helper Method to convert userPostDTO into a JSON string such that the input
     * can be processed
     * Input will look like this: {"name": "Test User", "username": "testUsername"}
     *
     * @param object
     * @return string
     */
    private String asJsonString(final Object object) {
        try {
            return new ObjectMapper().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("The request body could not be created.%s", e.toString()));
        }
    }
}