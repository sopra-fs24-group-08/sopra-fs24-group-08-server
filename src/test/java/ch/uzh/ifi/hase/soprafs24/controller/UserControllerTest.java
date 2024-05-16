package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.Achievement;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.EditUserPutDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.OtherUserGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
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

    /*
      1. getAllUsers
       */

    //The API correctly returns the list of users when user data exists.
    @Test
    public void getAllUsers_success_shouldReturnNonEmptyUserList() throws Exception {
        // Given
        List<User> allUsers = new ArrayList<>();
        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("userOne");
        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("userTwo");
        allUsers.add(user1);
        allUsers.add(user2);

        List<OtherUserGetDTO> dtos = allUsers.stream()
                .map(user -> DTOMapper.INSTANCE.convertEntityToOtherUserGetDTO(user))
                .collect(Collectors.toList());

        given(userService.getUsers()).willReturn(allUsers);

        // When
        MockHttpServletRequestBuilder getRequest = get("/users")
                .header("Authorization", "valid_token");

        // Then
        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].username", is("userOne")))
                .andExpect(jsonPath("$[1].username", is("userTwo")));
    }

    //Whether the API returns an empty list in the absence of user data.
    @Test
    public void getAllUsers_noUsers_shouldReturnEmptyList() throws Exception {
        // Given
        given(userService.getUsers()).willReturn(new ArrayList<>());

        // When
        MockHttpServletRequestBuilder getRequest = get("/users")
                .header("Authorization", "valid_token");

        // Then
        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    //Ensure that the API returns a 401 unauthorized status when the user is unauthorized.
    @Test
    public void getAllUsers_unauthorizedUser_shouldReturnUnauthorized() throws Exception {
        // Given
        doThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED))
                .when(userService).authorizeUser(anyString());

        // When
        MockHttpServletRequestBuilder getRequest = get("/users")
                .header("Authorization", "invalid_token");

        // Then
        mockMvc.perform(getRequest)
                .andExpect(status().isUnauthorized());
    }


    /*
      2. createUser
       */

    //Input validation: when registering without entering password or username, or both (failed)
    static Stream<Arguments> invalidUserInputProvider() {
        return Stream.of(
                Arguments.of("", "password123"),  // empty username
                Arguments.of("username", ""),     // empty password
                Arguments.of("", "")              // Username and password are empty
        );
    }


    //Duplicate user name: return 409 error
    @Test
    public void createUser_duplicateUsername_shouldReturnConflict() throws Exception {
        // Given
        UserPostDTO userPostDTO = new UserPostDTO();
        userPostDTO.setUsername("testUsername");
        userPostDTO.setPassword("testPassword");

        // When attempting to create a user, expect a conflict exception to be thrown if the user name already exists
        given(userService.createUser(any())).willThrow(new ResponseStatusException(HttpStatus.CONFLICT));

        // When
        MockHttpServletRequestBuilder postRequest = post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userPostDTO)); // 将DTO转换为JSON

        // Then
        mockMvc.perform(postRequest)
                .andExpect(status().isConflict()); // 预期返回409冲突错误
    }

    /*
      3. loginUser
       */

    // Success story: correct credentials return user information
    @Test
    public void loginUser_withValidCredentials_shouldReturnUserInfo() throws Exception {
        // Create login information DTO
        UserPostDTO loginUser = new UserPostDTO();
        loginUser.setUsername("validUsername");
        loginUser.setPassword("validPassword");

        // Assuming this is the user data returned from the service layer
        User user = new User();
        user.setId(1L);
        user.setUsername("validUsername");
        user.setStatus(UserStatus.ONLINE);

        // DTO conversion
        OtherUserGetDTO userGetDTO = DTOMapper.INSTANCE.convertEntityToOtherUserGetDTO(user);

        // Setting the behavior of the service layer
        given(userService.loginCredentials(any(User.class))).willReturn(user);

        // Simulating HTTP requests
        MockHttpServletRequestBuilder postRequest = post("/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(loginUser));

        // Implementation and validation
        mockMvc.perform(postRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is(user.getUsername())))
                .andExpect(jsonPath("$.status", is(user.getStatus().toString())));
    }

    // Wrong credentials: return 401 unauthorized
    @Test
    public void loginUser_withInvalidCredentials_shouldReturnUnauthorized() throws Exception {
        // 创建登录信息DTO
        UserPostDTO loginUser = new UserPostDTO();
        loginUser.setUsername("invalidUsername");
        loginUser.setPassword("invalidPassword");

        // 设置服务层抛出未授权异常
        given(userService.loginCredentials(any(User.class))).willThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        // 模拟HTTP请求
        MockHttpServletRequestBuilder postRequest = post("/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(loginUser));

        // 执行和验证
        mockMvc.perform(postRequest)
                .andExpect(status().isUnauthorized());
    }


    /*
      4. getUserById
       */
    //Success Story: Returning User Information
    @Test
    public void getUserById_withValidIdAndAuthorization_shouldReturnUserInfo() throws Exception {
        // 给定
        Long userId = 1L; // 假设这是一个有效的用户ID
        User user = new User();
        user.setId(userId);
        user.setUsername("testUser");
        user.setStatus(UserStatus.ONLINE);

        OtherUserGetDTO userGetDTO = DTOMapper.INSTANCE.convertEntityToOtherUserGetDTO(user);

        // 模拟服务层返回特定用户信息
        given(userService.getUserbyUserID(userId)).willReturn(user);

        // 模拟授权成功
        doNothing().when(userService).authenticateUser(anyString(), eq(userId));

        // 当
        MockHttpServletRequestBuilder getRequest = get("/users/" + userId)
                .header("Authorization", "valid_token");

        // 那么
        mockMvc.perform(getRequest)
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id", is(userId.intValue())))
                .andExpect(jsonPath("$.username", is(user.getUsername())))
                .andExpect(jsonPath("$.status", is(user.getStatus().toString())));
    }

    //User does not exist: return 404 error
    @Test
    public void getUserById_withNonExistentId_shouldReturnNotFound() throws Exception {
        // 给定
        Long userId = 100L; // 假设这是一个不存在的用户ID

        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND))
                .when(userService).getUserbyUserID(userId);

        // 还需要授权成功才能执行查询
        doNothing().when(userService).authenticateUser(anyString(), eq(userId));

        // 当
        MockHttpServletRequestBuilder getRequest = get("/users/" + userId)
                .header("Authorization", "valid_token");

        // 那么
        mockMvc.perform(getRequest)
                .andExpect(status().isNotFound());
    }

    //Authorization Failure: Returns 403
    @Test
    public void getUserById_withInvalidAuthorization_shouldReturnUnauthorizedOrForbidden() throws Exception {
        // 给定
        Long userId = 1L;

        // 模拟用户存在
        User user = new User();
        user.setId(userId);
        user.setUsername("existingUser");
        given(userService.getUserbyUserID(userId)).willReturn(user);

        // 模拟授权失败
        doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN))
                .when(userService).authenticateUser(anyString(), eq(userId));

        // 当
        MockHttpServletRequestBuilder getRequest = get("/users/" + userId)
                .header("Authorization", "invalid_token");

        // 那么
        mockMvc.perform(getRequest)
                .andExpect(status().isForbidden());
    }


    /*
      5. editUser
       */

    // Successful cases: successfully modified and returned 204 no content
    @Test
    public void editUser_withValidInputAndAuthorization_shouldReturnNoContent() throws Exception {
        // 给定
        Long userId = 1L;
        EditUserPutDTO editUserDTO = new EditUserPutDTO();
        editUserDTO.setUsername("updatedUsername");

        // 当
        MockHttpServletRequestBuilder putRequest = put("/users/" + userId)
                .header("Authorization", "valid_token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(editUserDTO));

        // 那么
        mockMvc.perform(putRequest)
                .andExpect(status().isNoContent());
    }


    // User does not exist: return 404 error
    @Test
    public void editUser_withNonExistentUser_shouldReturnNotFound() throws Exception {
        // 给定
        Long userId = 100L;  // 假设这是一个不存在的用户ID
        EditUserPutDTO editUserDTO = new EditUserPutDTO();
        editUserDTO.setUsername("nonexistentUsername");

        doNothing().when(userService).authenticateUser(anyString(), eq(userId));
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND)).when(userService).editUserbyUser(any(User.class));

        // 当
        MockHttpServletRequestBuilder putRequest = put("/users/" + userId)
                .header("Authorization", "valid_token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(editUserDTO));

        // 那么
        mockMvc.perform(putRequest)
                .andExpect(status().isNotFound());
    }


    //Authorization Failure: Returns 403
    @Test
    public void editUser_withInvalidAuthorization_shouldReturnUnauthorizedOrForbidden() throws Exception {
        // 给定
        Long userId = 1L;
        EditUserPutDTO editUserDTO = new EditUserPutDTO();
        editUserDTO.setUsername("anyUsername");

        doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN)).when(userService).authenticateUser(anyString(), eq(userId));

        // 当
        MockHttpServletRequestBuilder putRequest = put("/users/" + userId)
                .header("Authorization", "invalid_token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(editUserDTO));

        // 那么
        mockMvc.perform(putRequest)
                .andExpect(status().isForbidden());
    }

    /*
      6. logoutUser
       */

    //Success Stories: Back to 204 no content
    @Test
    public void logoutUser_withValidAuthorization_shouldReturnNoContent() throws Exception {
        // 给定
        Long userId = 1L;


        // 当
        MockHttpServletRequestBuilder putRequest = put("/users/" + userId + "/logout")
                .header("Authorization", "valid_token");

        // 那么
        mockMvc.perform(putRequest)
                .andExpect(status().isNoContent());
    }

    //User does not exist: return 404 error
    @Test
    public void logoutUser_withNonExistentUser_shouldReturnNotFound() throws Exception {
        // 给定
        Long userId = 100L;  // 假设这是一个不存在的用户ID

        doNothing().when(userService).authenticateUser(anyString(), eq(userId));
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND)).when(userService).logoutUserbyUserID(userId);

        // 当
        MockHttpServletRequestBuilder putRequest = put("/users/" + userId + "/logout")
                .header("Authorization", "valid_token");

        // 那么
        mockMvc.perform(putRequest)
                .andExpect(status().isNotFound());
    }

    //Authorization Failure: Return 403
    @Test
    public void logoutUser_withInvalidAuthorization_shouldReturnUnauthorizedOrForbidden() throws Exception {
        // 给定
        Long userId = 1L;

        doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN)).when(userService).authenticateUser(anyString(), eq(userId));

        // 当
        MockHttpServletRequestBuilder putRequest = put("/users/" + userId + "/logout")
                .header("Authorization", "invalid_token");

        // 那么
        mockMvc.perform(putRequest)
                .andExpect(status().isForbidden());
    }


    /*
      7. authenticateUser
       */

    //Success story: return true
    @Test
    public void authenticateUser_withValidCredentials_shouldReturnTrue() throws Exception {
        // 给定
        Long userId = 1L;

        doNothing().when(userService).authenticateUser(anyString(), eq(userId));

        // 当
        MockHttpServletRequestBuilder getRequest = get("/users/authenticate/" + userId)
                .header("Authorization", "valid_token");

        // 那么
        mockMvc.perform(getRequest)
                .andExpect(status().isOk());
    }

    //User does not exist or authorization failed: return 403
    @Test
    public void authenticateUser_withInvalidCredentialsOrUserNotFound_shouldReturnUnauthorizedOrForbidden() throws Exception {
        // 给定
        Long userId = 1L;

        doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN)).when(userService).authenticateUser(anyString(), eq(userId));

        // 当
        MockHttpServletRequestBuilder getRequest = get("/users/authenticate/" + userId)
                .header("Authorization", "invalid_token");

        // 那么
        mockMvc.perform(getRequest)
                .andExpect(status().isForbidden());
    }


    /*
      8. getUserAchievements
       */

    //Success Stories: Back to Achievement List
    @Test
    public void getUserAchievements_withValidUser_shouldReturnAchievements() throws Exception {
        // 给定
        Long userId = 1L;
        User user = new User();
        user.setId(userId);

        Achievement achievement1 = new Achievement(); // 假设只有默认构造器
        Achievement achievement2 = new Achievement();

        Set<Achievement> achievements = new HashSet<>(Arrays.asList(achievement1, achievement2));
        user.setAchievements(achievements);

        given(userService.getUserbyUserID(userId)).willReturn(user);

        // 当
        MockHttpServletRequestBuilder getRequest = get("/users/" + userId + "/achievements")
                .header("Authorization", "valid_token");

        // 那么
        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }


    //User does not exist: return 404 error
    @Test
    public void getUserAchievements_withNonExistentUser_shouldReturnNotFound() throws Exception {
        // 给定
        Long userId = 100L; // 假设这是一个不存在的用户ID

        given(userService.getUserbyUserID(userId)).willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        // 当
        MockHttpServletRequestBuilder getRequest = get("/users/" + userId + "/achievements")
                .header("Authorization", "valid_token");

        // 那么
        mockMvc.perform(getRequest)
                .andExpect(status().isNotFound());
    }

    //The Case of No Achievement: Returning the Empty Collection
    @Test
    public void getUserAchievements_withNoAchievements_shouldReturnEmptyList() throws Exception {
        // 给定
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setAchievements(new HashSet<>()); // 空的成就集合

        given(userService.getUserbyUserID(userId)).willReturn(user);

        // 当
        MockHttpServletRequestBuilder getRequest = get("/users/" + userId + "/achievements")
                .header("Authorization", "valid_token");

        // 那么
        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }


    // @Test
    // public void givenUsers_whenGetUsers_thenReturnJsonArray() throws Exception {
    //     // given
    //     User user = new User();
    //     user.setName("Firstname Lastname");
    //     user.setUsername("firstname@lastname");
    //     user.setStatus(UserStatus.OFFLINE);

    //     List<User> allUsers = Collections.singletonList(user);

    //     // this mocks the UserService -> we define above what the userService should
    //     // return when getUsers() is called
    //     given(userService.getUsers()).willReturn(allUsers);

    //     // when
    //     MockHttpServletRequestBuilder getRequest = get("/users").contentType(MediaType.APPLICATION_JSON);

    //     // then
    //     mockMvc.perform(getRequest).andExpect(status().isOk())
    //             .andExpect(jsonPath("$", hasSize(1)))
    //             .andExpect(jsonPath("$[0].name", is(user.getName())))
    //             .andExpect(jsonPath("$[0].username", is(user.getUsername())))
    //             .andExpect(jsonPath("$[0].status", is(user.getStatus().toString())));
    // }

    @Test
    //POST success
    public void createUser_validInput_userCreated() throws Exception {
        // given
        User user = new User();
        user.setId(1L);
        user.setUsername("testUsername");
        user.setToken("1");
        user.setPassword("testPassword");
        user.setStatus(UserStatus.OFFLINE);

        UserPostDTO userPostDTO = new UserPostDTO();
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
                .andExpect(jsonPath("$.username", is(user.getUsername())))
                .andExpect(jsonPath("$.status", is(user.getStatus().toString())));
    }
    //POST：fail
    @Test
    public void doubledUser_validInput_throwexceptions() throws Exception {
        // given
        UserPostDTO userPostDTO = new UserPostDTO();
        userPostDTO.setUsername("testUsername");
        userPostDTO.setPassword("testPassword");
        given(userService.createUser(Mockito.any())).willThrow(new ResponseStatusException(HttpStatus.CONFLICT));

        // when/then -> do the request + validate the result
        MockHttpServletRequestBuilder postRequest = post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userPostDTO));

        // then
        mockMvc.perform(postRequest)
                .andExpect(status().isConflict()); //409
    }

    //GET: success
    // @Test
    // public void givenUser_whenUserId_ReturnFullUserInfo() throws Exception {

    //     User user = new User();
    //     LocalDate date = LocalDate.now();

    //     // given user
    //     user.setId(1L);
    //     user.setName("Dennis");
    //     user.setUsername("Dennis");
    //     user.setPassword("Dennis");
    //     user.setCreation_date(date);
    //     user.setStatus(UserStatus.ONLINE);
    //     user.setBirthday(date);

    //     given(userService.getUserbyUserID(Mockito.any())).willReturn(user);

    //     // when
    //     MockHttpServletRequestBuilder getRequest = get("/users/1")
    //             .contentType(MediaType.APPLICATION_JSON);

    //     // then
    //     mockMvc.perform(getRequest)
    //             .andExpect(jsonPath("$.id", is(user.getId().intValue())))
    //             .andExpect(jsonPath("$.username", is(user.getUsername())))
    //             .andExpect(jsonPath("$.status", is(user.getStatus().toString())))
    //             .andExpect(jsonPath("$.creation_date", is(user.getCreation_date().toString())))
    //             .andExpect(jsonPath("$.birthday", is(user.getBirthday().toString())))
    //             .andExpect(jsonPath("$.name", is(user.getName())));
    // }
    //GET:fail
    // @Test
    // public void givenId_IdNotFound_throwexception() throws Exception {
    //     //given
    //     given(userService.getUserbyUserID(Mockito.any())).willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

    //     // when
    //     MockHttpServletRequestBuilder getRequest = get("/users/10086")
    //             .contentType(MediaType.APPLICATION_JSON);

    //     // then
    //     mockMvc.perform(getRequest)
    //             .andExpect(status().isNotFound());
    // }



    /**
    //PUT: success
    @Test
    public void givenUser_whenEdit_ReturnEdited() throws Exception {
        User user = new User();
        LocalDate date = LocalDate.now();

        // given user
        user.setId(189L);
        user.setUsername("SoPra555");
        user.setPassword("SoPra555");
        user.setCreation_date(date);
        user.setStatus(UserStatus.ONLINE);

        EditUserPutDTO editUserPutDTO = new EditUserPutDTO();

        given(userService.editUserbyUser(Mockito.any())).willReturn(user);

        // when
        MockHttpServletRequestBuilder putRequest = put("/users/189")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(editUserPutDTO));

        // then
        mockMvc.perform(putRequest)
                .andExpect(status().isNoContent());//204
    }
    // PUT: fail
    @Test
    public void givenId_attemptedit_IdNotFound() throws Exception {
        // given$
        EditUserPutDTO editUserPutDTO = new EditUserPutDTO();

        given(userService.editUserbyUser(Mockito.any())).willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));


        // when
        MockHttpServletRequestBuilder putRequest = put("/users/100000")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(editUserPutDTO));

        // then
        mockMvc.perform(putRequest)
                .andExpect(status().isNotFound());//404
    }

     */


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