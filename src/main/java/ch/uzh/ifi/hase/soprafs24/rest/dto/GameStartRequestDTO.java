package ch.uzh.ifi.hase.soprafs24.rest.dto;

public class GameStartRequestDTO {
    public Long getUserId1() {
        return userId1;
    }

    public void setUserId1(Long userId1) {
        this.userId1 = userId1;
    }

    private Long userId1;
    private Long userId2;

    public Long getUserId2() {
        return userId2;
    }

    public void setUserId2(Long userId2) {
        this.userId2 = userId2;
    }
}
