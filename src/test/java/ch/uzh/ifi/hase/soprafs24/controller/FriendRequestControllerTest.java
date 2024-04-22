package ch.uzh.ifi.hase.soprafs24.controller;


import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.constant.RequestStatus;
import ch.uzh.ifi.hase.soprafs24.constant.RequestType;
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
import org.springframework.web.context.request.async.DeferredResult;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.*;

import java.beans.Transient;

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
                .header("Authorization", "1")) 
                .andExpect(status().isCreated()) 
                .andExpect(jsonPath("$.id", is(friend.getId().intValue()))) 
                .andExpect(jsonPath("$.username", is(friend.getUsername())));
    }

    @Test
    public void inviteFriendtoGame_validInput_ReturnFriendRequest() throws Exception {
        // given
        Long userId = 1L;
        String token = "Bearer someAuthToken";
        FriendRequestDTO friendRequestDTO = new FriendRequestDTO();
        friendRequestDTO.setSenderId(1L);
        friendRequestDTO.setReceiverId(2L);
        friendRequestDTO.setRequestType(RequestType.GAMEINVITATION);
        FriendRequest gameInvitation = DTOMapper.INSTANCE.convertFriendRequestDTOtoEntity(friendRequestDTO);
        Mockito.doNothing().when(userService).authenticateUser(Mockito.any(), Mockito.any());
        given(friendService.inviteFriendToGame(eq(userId), Mockito.any(FriendRequest.class))).willReturn(gameInvitation);
        given(friendService.convertEntityToFriendRequestDTO(Mockito.any(FriendRequest.class))).willReturn(friendRequestDTO);
    
        mockMvc.perform(post("/game/invite/{userId}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(friendRequestDTO)) 
                .header("Authorization", token)) 
                .andExpect(status().isCreated()) 
                .andExpect(jsonPath("$.senderId", is(friendRequestDTO.getSenderId().intValue())))
                .andExpect(jsonPath("$.receiverId", is(friendRequestDTO.getReceiverId().intValue())))
                .andExpect(jsonPath("$.senderName", is(friendRequestDTO.getSenderName())))
                .andExpect(jsonPath("$.requestType", is(friendRequestDTO.getRequestType().toString())));
    }
    
    @Test
    public void longPolling_validInput_ReturnResult() throws Exception {
        Long userId = 1L;
        String token = "Bearer someAuthToken";
        doNothing().when(friendService).pollUpdates(any(DeferredResult.class), eq(userId));
    
        mockMvc.perform(get("/users/{userId}/polling", 1L)
                .header("Authorization", token)) 
                .andExpect(status().isOk());
    }

    @Test
    public void responseFriendRequest_validInput_returnFriendRequest() throws Exception {
      FriendRequestDTO friendRequestDTO = new FriendRequestDTO();
      friendRequestDTO.setSenderId(1L);
      friendRequestDTO.setReceiverId(2L);
      friendRequestDTO.setStatus(RequestStatus.ACCEPTED);
      friendRequestDTO.setRequestType(RequestType.FRIENDADDING);
      Long userId = 2L;
      String token = "Bearer someAuthToken";
      FriendRequest receivedFriendRequest = DTOMapper.INSTANCE.convertFriendRequestDTOtoEntity(friendRequestDTO);
      given(friendService.handleFriendRequest(eq(userId), Mockito.any(FriendRequest.class))).willReturn(receivedFriendRequest);
      given(friendService.convertEntityToFriendRequestDTO(Mockito.any(FriendRequest.class))).willReturn(friendRequestDTO);

  
      mockMvc.perform(post("/users/{userId}/friendresponse", userId)
              .contentType(MediaType.APPLICATION_JSON)
              .content(asJsonString(friendRequestDTO))
              .header("Authorization", token)) 
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.senderId", is(friendRequestDTO.getSenderId().intValue())))
              .andExpect(jsonPath("$.receiverId", is(friendRequestDTO.getReceiverId().intValue())))
              .andExpect(jsonPath("$.status", is(friendRequestDTO.getStatus().toString())))
              .andExpect(jsonPath("$.senderName", is(friendRequestDTO.getSenderName())))
              .andExpect(jsonPath("$.requestType", is(friendRequestDTO.getRequestType().toString())))
              .andReturn();
  }  

  @Test
  public void responseGameInvitation_validInput_returnFriendRequest() throws Exception {
    FriendRequestDTO friendRequestDTO = new FriendRequestDTO();
    friendRequestDTO.setSenderId(1L);
    friendRequestDTO.setReceiverId(2L);
    friendRequestDTO.setStatus(RequestStatus.ACCEPTED);
    friendRequestDTO.setRequestType(RequestType.GAMEINVITATION);
    Long userId = 2L;
    String token = "Bearer someAuthToken";
    FriendRequest receivedFriendRequest = DTOMapper.INSTANCE.convertFriendRequestDTOtoEntity(friendRequestDTO);
    given(friendService.handleGameInvitation(eq(userId), Mockito.any(FriendRequest.class))).willReturn(receivedFriendRequest);
    given(friendService.convertEntityToFriendRequestDTO(Mockito.any(FriendRequest.class))).willReturn(friendRequestDTO);


    mockMvc.perform(post("/game/{userId}/invitationresponse", userId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(asJsonString(friendRequestDTO))
            .header("Authorization", token)) 
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.senderId", is(friendRequestDTO.getSenderId().intValue())))
            .andExpect(jsonPath("$.receiverId", is(friendRequestDTO.getReceiverId().intValue())))
            .andExpect(jsonPath("$.status", is(friendRequestDTO.getStatus().toString())))
            .andExpect(jsonPath("$.senderName", is(friendRequestDTO.getSenderName())))
            .andExpect(jsonPath("$.requestType", is(friendRequestDTO.getRequestType().toString())))
            .andReturn();
  }  

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

