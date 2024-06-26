package ch.uzh.ifi.hase.soprafs24.rest.dto;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.Icon;

import java.time.LocalDate;
import java.util.List;

public class UserGetDTO {

    private Long id;
    private String username;
    private UserStatus status;
    private LocalDate creation_date;
    private LocalDate birthday;
    private Icon currIcon;
    private String avatarUrl;
    private List<IconGetDTO> icons;
    private List<BannerGetDTO> banners;
    private List<AchievementGetDTO> achievements;
    private String token;

    public Icon getCurrIcon() {
        return currIcon;
    }

    public void setCurrIcon(Icon currIcon) {
        this.currIcon = currIcon;
    }

    public List<BannerGetDTO> getBanners() {
        return banners;
    }

    public void setBanners(List<BannerGetDTO> banners) {
        this.banners = banners;
    }



    public List<AchievementGetDTO> getAchievements() {
        return achievements;
    }

    public void setAchievements(List<AchievementGetDTO> achievements) {
        this.achievements = achievements;
    }


    public LocalDate getCreation_date() {
        return creation_date;
    }

    public void setCreation_date(LocalDate creation_date){
        this.creation_date = creation_date;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }


    public List<IconGetDTO> getIcons() {
        return icons;
    }

    public void setIcons(List<IconGetDTO> icons) {
        this.icons = icons;
    }

    // getters and setters
    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }


}
