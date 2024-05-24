package ch.uzh.ifi.hase.soprafs24.controller;


import ch.uzh.ifi.hase.soprafs24.service.CloudStorageService;
import ch.uzh.ifi.hase.soprafs24.rest.dto.AvatarDTO;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;


@RestController
public class AvatarController {
    private final RestTemplate restTemplate;
    private final UserService userService;
    private final CloudStorageService cloudStorageService;

    @Value("${app.environment}")
    private String appEnvironment;

    @Autowired
    public AvatarController(RestTemplate restTemplate, UserService userService, CloudStorageService cloudStorageService) {
        this.restTemplate = restTemplate;
        this.userService = userService;
        this.cloudStorageService = cloudStorageService;
    }

    @GetMapping("/api/proxy/cat-avatar")
    public ResponseEntity<?> getCatAvatar(@RequestParam String name) {
        String url = "https://cat-avatars.vercel.app/api/cat?name=" + name;
        ResponseEntity<byte[]> response = restTemplate.getForEntity(url, byte[].class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            if ("production".equals(appEnvironment)) {
                // In production, store the avatar in Cloud Storage and return the public URL
                String objectName = "avatars/" + name + ".png";
                try {
                    String publicUrl = cloudStorageService.uploadObject(objectName, response.getBody());
                    return ResponseEntity.ok().body(publicUrl);
                } catch (IOException e) {
                    e.printStackTrace();
                    return ResponseEntity.status(500).body("Error storing avatar in Cloud Storage");
                }
            } else {
                // In development, return the blob directly
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(Objects.requireNonNull(response.getHeaders().getContentType()).toString()))
                        .body(response.getBody());
            }
        } else {
            return ResponseEntity.status(500).body("Error fetching avatar from external API");
        }
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
