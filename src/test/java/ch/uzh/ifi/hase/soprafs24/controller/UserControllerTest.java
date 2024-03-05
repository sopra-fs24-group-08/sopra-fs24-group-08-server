package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.EditUserPutDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
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

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * UserControllerTest
 * This is a WebMvcTest which allows to test the UserController i.e. GET/POST
 * request without actually sending them over the network.
 * This tests if the UserController works.
 */
@WebMvcTest(UserController.class)
public class UserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private UserService userService;

  @Test
  public void givenUsers_whenGetUsers_thenReturnJsonArray() throws Exception {
    // given
    User user = new User();
    user.setName("Firstname Lastname");
    user.setUsername("firstname@lastname");
    user.setStatus(UserStatus.OFFLINE);

    List<User> allUsers = Collections.singletonList(user);

    // this mocks the UserService -> we define above what the userService should
    // return when getUsers() is called
    given(userService.getUsers()).willReturn(allUsers);

    // when
    MockHttpServletRequestBuilder getRequest = get("/users").contentType(MediaType.APPLICATION_JSON);

    // then
    mockMvc.perform(getRequest).andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].name", is(user.getName())))
        .andExpect(jsonPath("$[0].username", is(user.getUsername())))
        .andExpect(jsonPath("$[0].status", is(user.getStatus().toString())));
  }

  @Test
  //POST success
  public void createUser_validInput_userCreated() throws Exception {
    // given
    User user = new User();
    user.setId(1L);
    user.setName("Test User");
    user.setUsername("testUsername");
    user.setToken("1");
    user.setPassword("testPassword");
    user.setStatus(UserStatus.OFFLINE);

    UserPostDTO userPostDTO = new UserPostDTO();
    userPostDTO.setName("Test User");
    userPostDTO.setUsername("testUsername");
    userPostDTO.setPassword("testPassword");

    given(userService.createUser(Mockito.any())).willReturn(user);

    // when/then -> do the request + validate the result
    MockHttpServletRequestBuilder postRequest = post("/users")
        .contentType(MediaType.APPLICATION_JSON)
        .content(asJsonString(userPostDTO));

    // then
    mockMvc.perform(postRequest)
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", is(user.getId().intValue())))
        .andExpect(jsonPath("$.name", is(user.getName())))
        .andExpect(jsonPath("$.username", is(user.getUsername())))
        .andExpect(jsonPath("$.status", is(user.getStatus().toString())));
  }
  //POST：fail
  @Test
  public void doubledUser_validInput_throwexceptions() throws Exception {
      // given
      UserPostDTO userPostDTO = new UserPostDTO();
      userPostDTO.setName("Test User");
      userPostDTO.setUsername("testUsername");
      userPostDTO.setPassword("testPassword");
      given(userService.createUser(Mockito.any())).willThrow(new ResponseStatusException(HttpStatus.CONFLICT));

      // when/then -> do the request + validate the result
      MockHttpServletRequestBuilder postRequest = post("/users")
              .contentType(MediaType.APPLICATION_JSON)
              .content(asJsonString(userPostDTO));

      // then
      mockMvc.perform(postRequest)
              .andExpect(status().isConflict());
  }

  //GET: success
  @Test
  public void givenUser_whenUserId_ReturnFullUserInfo() throws Exception {

      User user = new User();
      LocalDate date = LocalDate.now();

      // given user
      user.setId(1L);
      user.setName("Dennis");
      user.setUsername("Dennis");
      user.setPassword("Dennis");
      user.setCreation_date(date);
      user.setStatus(UserStatus.ONLINE);

      given(userService.getUserbyUserID(Mockito.any())).willReturn(user);

      // when
      MockHttpServletRequestBuilder getRequest = get("/users/1")
              .contentType(MediaType.APPLICATION_JSON);

      // then
      mockMvc.perform(getRequest)
              .andExpect(jsonPath("$.id", is(user.getId().intValue())))
              .andExpect(jsonPath("$.username", is(user.getUsername())))
              .andExpect(jsonPath("$.status", is(user.getStatus().toString())));
      //.andExpect(jsonPath("$.creation_date", is(user.getCreation_date())));
      //.andExpect(jsonPath("$.birthday", is(user.getBirthday().toString())));
  }
  //GET:fail
  @Test
  public void givenId_IdNotFound_throwexception() throws Exception {
      //given
      given(userService.getUserbyUserID(Mockito.any())).willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

      // when
      MockHttpServletRequestBuilder getRequest = get("/users/1")
              .contentType(MediaType.APPLICATION_JSON);

      // then
      mockMvc.perform(getRequest)
              .andExpect(status().isNotFound());
  }



    //PUT: success
    @Test
    public void givenUser_whenEdit_ReturnEdited() throws Exception {
        User user = new User();
        LocalDate date = LocalDate.now();

        // given user
        user.setId(189L);
        user.setName("SoPra555");
        user.setUsername("SoPra555");
        user.setPassword("SoPra555");
        user.setCreation_date(date);
        user.setStatus(UserStatus.ONLINE);


        EditUserPutDTO editUserPutDTO = new EditUserPutDTO();



        given(userService.editUserbyUserID(Mockito.any())).willReturn(user);

        // when
        MockHttpServletRequestBuilder putRequest = put("/users/189")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(editUserPutDTO));

        // then
        mockMvc.perform(putRequest)
                .andExpect(status().isNoContent());
    }
    // PUT: fail
    @Test
    public void givenId_attemptedit_IdNotFound() throws Exception {
        // given$
        LocalDate date = LocalDate.now();
        EditUserPutDTO editUserPutDTO = new EditUserPutDTO();

        given(userService.editUserbyUserID(Mockito.any())).willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));


        // when
        MockHttpServletRequestBuilder putRequest = put("/users/100000")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(editUserPutDTO));

        // then
        mockMvc.perform(putRequest)
                .andExpect(status().isNotFound());
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