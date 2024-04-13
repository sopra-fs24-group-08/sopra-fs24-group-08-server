package ch.uzh.ifi.hase.soprafs24.entity;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Entity
@Table(name = "USER")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private UserStatus status;

    @Column(nullable = false)
    private LocalDate creation_date;

    @Column
    private LocalDate birthday;

    @ManyToMany
    @JoinTable(
            name = "user_friends",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "friend_id")
    )
    private List<User> friends;

    @Column
    private boolean inGame;

    private boolean icon1Available;
    private boolean icon2Available;
    private boolean icon3Available;

    private boolean banner1Available;
    private boolean banner2Available;
    private boolean banner3Available;

    private List<Integer> achievements;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public LocalDate getCreationDate() {
        return creation_date;
    }

    public void setCreationDate(LocalDate creationDate) {
        this.creation_date = creationDate;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }

    public List<User> getFriends(){
        return friends;
    }

    public void addFriend(User friend){
        this.friends.add(friend);
    }

    public void deleteFriend(User friend){
        this.friends.remove(friend);
    }

    public Boolean getInGame() {
        return inGame;
    }
    public void setInGame(Boolean inGame) {
        this.inGame = inGame;
    }

    public List<Boolean> getIcons() {
        return Arrays.asList(icon1Available, icon2Available, icon3Available);
    }

    public void setIcons(boolean iconAvailable){
        iconAvailable = true;
    }

    public List<Boolean> getBanners() {
        return Arrays.asList(banner1Available, banner2Available, banner3Available);
    }

    public void setBanners(boolean bannerAvailable){
        bannerAvailable = true;
    }

    // Assuming we are just adding the entire list of achievements
    public void setAchievements(List<Integer> achievements) {
        this.achievements = achievements;
    }

    public List<Integer> getAchievements() {
        return achievements;
    }

    public void setCreation_date(LocalDate creation_date) {this.creation_date = creation_date;}

    public LocalDate getCreation_date() {return creation_date;}

    // Enums
    public enum Icon {
        ICON1,
        ICON2,
        ICON3
    }

    public enum Banner {
        BANNER1,
        BANNER2,
        BANNER3
    }

    public enum Achievement {
        ACHIEVEMENT_1,
        ACHIEVEMENT_2,
        ACHIEVEMENT_3
    }
}
