package ch.uzh.ifi.hase.soprafs24.controller;


import ch.uzh.ifi.hase.soprafs24.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;


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
                .contentType(MediaType.parseMediaType(response.getHeaders().getContentType().toString()))
                .body(response.getBody());
    }

    @PutMapping("/{id}/updateIcon")
    public ResponseEntity<?> updateIcon(@PathVariable Long id, @RequestBody String avatarUrl) {
        userService.updateUserAvatar(id, avatarUrl);  // Pass the URL string directly
        return ResponseEntity.ok().build();
    }
}
