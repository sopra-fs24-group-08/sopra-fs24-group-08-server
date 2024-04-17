package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
public class AuthenticationController {

    private final UserService userService;

    AuthenticationController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/users/authentication/token")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> Authentication(@RequestBody UserPostDTO userPostDTO) {
        // convert API user to internal representation


        User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);

        //giving up on http variant for now, might not even be necessary for M1
        //returns token
        if (userService.checkIfValid(userInput) != null) {

            ResponseCookie cookie = ResponseCookie.from("token", userService.checkIfValid(userInput))
                    //Research exactly why it doesn't work later
                    //nvm not needed at all for M1
                    .httpOnly(true)
                    .secure(true)
                    .sameSite("Lax")
                    .path("/")
                    .build();
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.SET_COOKIE,cookie.toString());
            // Return ResponseEntity with headers and token as body
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(userService.checkIfValid(userInput));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("");
        }
    }
    @GetMapping("/users/authentication")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public UserGetDTO auth(@RequestHeader("Authorization") String token,@RequestBody UserPostDTO userPostDTO) {
        userService.authToken(token);
        User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);
        User authUser = userService.loginAuth(userInput);
        return DTOMapper.INSTANCE.convertEntityToUserGetDTO(authUser);
    }
    //Handle it for Localstorage/SessionStorage sth else??
    @PostMapping("/users/authentication/login")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public UserGetDTO login(@RequestBody UserPostDTO userPostDTO) {
        User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);
        User authUser = userService.loginAuth(userInput);
        return DTOMapper.INSTANCE.convertEntityToUserGetDTO(authUser);
    }


}

