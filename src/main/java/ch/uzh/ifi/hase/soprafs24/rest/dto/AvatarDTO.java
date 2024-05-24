package ch.uzh.ifi.hase.soprafs24.rest.dto;

public class AvatarDTO {
    private String avatarUrl;

    public AvatarDTO(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}
