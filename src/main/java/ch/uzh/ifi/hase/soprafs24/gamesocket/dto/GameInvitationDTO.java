package ch.uzh.ifi.hase.soprafs24.gamesocket.dto;

import ch.uzh.ifi.hase.soprafs24.constant.RequestStatus;

public class GameInvitationDTO {
    private RequestStatus status;
    private Long gameId;
    private Long senderId;
    private Long receiverId;

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public Long getGameId() {
        return gameId;
    }

    public void setGameId(Long gameId) {
        this.gameId = gameId;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public Long getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(Long receiverId) {
        this.receiverId = receiverId;
    }
}
