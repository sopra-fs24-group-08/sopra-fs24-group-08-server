package ch.uzh.ifi.hase.soprafs24.controller;
import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserEditDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;
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
import static java.lang.Math.toIntExact;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
    public void getUsers() throws Exception {
        // given
        User user = new User();
        user.setPassword("Password");
        user.setUsername("Username");
        user.setCreation_date(LocalDate.now());
        user.setBirthday(null);
        user.setStatus(UserStatus.OFFLINE);

        List<User> allUsers = Collections.singletonList(user);

        given(userService.getUsers()).willReturn(allUsers);

        // when
        MockHttpServletRequestBuilder getRequest = get("/users").contentType(MediaType.APPLICATION_JSON);

        // then
        mockMvc.perform(getRequest).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                //Not giving back password so no need to test.
                .andExpect(jsonPath("$[0].creation_date",is(user.getCreation_date().toString())))
                .andExpect(jsonPath("$[0].birthday", is(user.getBirthday())))
                .andExpect(jsonPath("$[0].username", is(user.getUsername())))
                .andExpect(jsonPath("$[0].status", is(user.getStatus().toString())));
    }

    @Test
    public void createUser_validInput_userCreated() throws Exception {
        // given
        User user = new User();
        user.setId(1L);
        user.setUsername("TestUsername");
        user.setPassword("TestPassword");
        user.setToken("1");
        user.setBirthday(null);
        user.setCreation_date(LocalDate.now());
        //ask after doing it, maybe online implementation wanted
        user.setStatus(UserStatus.ONLINE);
        UserPostDTO userPostDTO = new UserPostDTO();
        userPostDTO.setPassword("TestPassword");
        userPostDTO.setUsername("TestUsername");
        given(userService.createUser(Mockito.any())).willReturn(user);
        MockHttpServletRequestBuilder postRequest = post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userPostDTO));

        // then
        mockMvc.perform(postRequest)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(user.getId().intValue())))
                .andExpect(jsonPath("$.username", is(user.getUsername())))
                .andExpect(jsonPath("$.birthday",is(user.getBirthday())))
                .andExpect(jsonPath("$.creation_date",is(user.getCreation_date().toString())))
                .andExpect(jsonPath("$.status", is(user.getStatus().toString())));


    }

    @Test
    public void createUser_Duplicate_Invalid() throws Exception {
        UserPostDTO userPostDTO = new UserPostDTO();
        userPostDTO.setPassword("testPassword");
        userPostDTO.setUsername("testUsername");
        //TA said we really are only testing the endpoints and the HTTP contents
        given(userService.createUser(Mockito.any(User.class)))
                .willThrow(new ResponseStatusException(HttpStatus.CONFLICT));

        MockHttpServletRequestBuilder postRequest = post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userPostDTO));

        mockMvc.perform(postRequest)
                .andExpect(status().isConflict());
    }



    @Test
    public void getProfile_Valid() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setPassword("Password");
        user.setUsername("Username");
        user.setCreation_date(LocalDate.now());
        LocalDate now =  user.getCreation_date();
        user.setBirthday(null);
        user.setStatus(UserStatus.ONLINE);


        User user2 = new User();
        user2.setId(1L);
        user2.setPassword("Password");
        user2.setUsername("Username");
        user2.setCreation_date(LocalDate.now());
        user2.setBirthday(null);
        user2.setStatus(UserStatus.ONLINE);
        UserGetDTO userGetDTO = DTOMapper.INSTANCE.convertEntityToUserGetDTO(user2);
        given(userService.getUserById(Mockito.any())).willReturn(user);
        MockHttpServletRequestBuilder getRequest = get("/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization","HiddenToken");

        mockMvc.perform(getRequest).andExpect(jsonPath("$.username",is (userGetDTO.getUsername())))
                .andExpect(jsonPath("$.id",is(toIntExact(userGetDTO.getId()))))
                .andExpect(jsonPath("$.creation_date",is (now.toString())))
                .andExpect(jsonPath("$.status",is (userGetDTO.getStatus().toString())))
                .andExpect(jsonPath("$.birthday",is (userGetDTO.getBirthday())))
                .andExpect(jsonPath("$.creation_date", is(now.toString())))
                .andExpect(status().isOk());
    }

    @Test
    public void getProfile_notFound() throws Exception {
        MockHttpServletRequestBuilder getRequest = get("/users/2")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization","HiddenToken");
        given(userService.getUserById(2L))
                .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));
        mockMvc.perform(getRequest).andExpect(status().isNotFound());
    }

    @Test
    public void updateUser_validId() throws Exception{
        User user = new User();
        user.setId(1L);
        user.setPassword("Password");
        user.setUsername("Username");
        user.setCreation_date(LocalDate.now());
        user.setBirthday(null);
        user.setStatus(UserStatus.OFFLINE);
        user.setToken("HiddenToken");
        given(userService.createUser(Mockito.any())).willReturn(user);


        UserEditDTO userEditDTO = new UserEditDTO();
        userEditDTO.setUsername("CoolerUsername");
        long ID = user.getId();

        MockHttpServletRequestBuilder putRequest = put("/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization","HiddenToken")
                .content(asJsonString(userEditDTO));
        mockMvc.perform(putRequest).andExpect(status().isNoContent());


        //User freshUser = userService.getUserById(ID);
        //given(userService.getUserById(user.getId())).willReturn(freshUser);

    }
    @Test
    public void updateUser_notFound() throws Exception {
        UserEditDTO userEditDTO = new UserEditDTO();
        userEditDTO.setUsername("CoolerUsername");

        MockHttpServletRequestBuilder getRequest = get("/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization","HiddenToken");
        given(userService.getUserById(1L))
                .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));
        mockMvc.perform(getRequest).andExpect(status().isNotFound());
    }
    @Test
    public void updateUser_InvalidToken() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setPassword("Password");
        user.setUsername("Username");
        user.setCreation_date(LocalDate.now());
        user.setBirthday(null);
        user.setStatus(UserStatus.OFFLINE);
        user.setToken("HiddenToken");
        given(userService.createUser(Mockito.any())).willReturn(user);
        UserEditDTO userEditDTO = new UserEditDTO();
        userEditDTO.setUsername("CoolerUsername");

        MockHttpServletRequestBuilder getRequest = get("/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization","WrongToken");
        given(userService.getUserById(1L))
                .willThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        mockMvc.perform(getRequest).andExpect(status().isUnauthorized());
    }

    private String asJsonString(final Object object) {
        try {
            return new ObjectMapper().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("The request body could not be created.%s", e.toString()));
        }
    }
}