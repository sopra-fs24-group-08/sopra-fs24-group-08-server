package ch.uzh.ifi.hase.soprafs24.controller;


import ch.uzh.ifi.hase.soprafs24.rest.dto.AvatarDTO;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Objects;


@RestController
public class AvatarController {
    private final RestTemplate restTemplate;
    private final UserService userService;

    @Autowired
    public AvatarController(RestTemplate restTemplate, UserService userService) {
        this.restTemplate = restTemplate;
        this.userService = userService;
    }

    @GetMapping("/api/proxy/cat-avatar")
    public ResponseEntity<byte[]> getCatAvatar(@RequestParam String name) {
        String url = "https://cat-avatars.vercel.app/api/cat?name=" + name;
        ResponseEntity<byte[]> response = restTemplate.getForEntity(url, byte[].class);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(Objects.requireNonNull(response.getHeaders().getContentType()).toString()))
                .body(response.getBody());
    }

    @PutMapping("/users/{id}/updateIcon")
    public ResponseEntity<?> updateAvatar(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        try {
            String avatarUrl = payload.get("imageUrl");
            // Print the received URL to validate its contents
            System.out.println("Received avatar URL: " + avatarUrl);

            userService.updateUserAvatar(id, avatarUrl);
            return ResponseEntity.ok().body("Avatar updated successfully");
        } catch (Exception e) {
            // Printing error details on the console
            System.err.println("Error updating avatar: " + e.getMessage());
            return ResponseEntity.badRequest().body("Error updating avatar: " + e.getMessage());
        }
    }

    @RestController
    @RequestMapping("/api/users")
    public class UserController {

        private final UserService userService;

        @Autowired
        public UserController(UserService userService) {
            this.userService = userService;
        }

        @GetMapping("/{userId}/avatar")
        public ResponseEntity<AvatarDTO> getUserAvatar(@PathVariable Long userId) {
            return userService.getAvatarUrl(userId)
                    .map(AvatarDTO::new)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        }
    }


}
