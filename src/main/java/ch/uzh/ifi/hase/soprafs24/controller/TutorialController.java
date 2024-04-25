package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.constant.GlobalConstants;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
public class TutorialController {

    @GetMapping("/tutorial")
    @ResponseStatus(HttpStatus.OK)
    public String provideTutorialUrl() { 
      String tutorialUrl = GlobalConstants.TUTORIAL_VIDEOID;
      return tutorialUrl;
    }
  
}