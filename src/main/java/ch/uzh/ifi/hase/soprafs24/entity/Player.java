package ch.uzh.ifi.hase.soprafs24.entity;

public class Player extends User {
    
    private User user;
    public Player(User user){
        this.user = user;
    }
    
}
