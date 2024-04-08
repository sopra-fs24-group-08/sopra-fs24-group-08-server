package ch.uzh.ifi.hase.soprafs24.rest.dto;

import ch.uzh.ifi.hase.soprafs24.constant.RequestStatus;

public class FriendRequestDTO {

    private Long senderId;
    private Long receiverId;
    private RequestStatus status;

    public Long getSenderId(){
        return senderId;
    }

    public void setSenderId(Long senderId){
        this.senderId = senderId;
    }

    public Long getReceiverId(){
        return receiverId;
    }

    public void setReceiverId(Long receiverId){
        this.receiverId = receiverId;
    }

    public RequestStatus getStatus(){
        return status;
    }

    public void setStatus(RequestStatus status){
        this.status = status;
    }
}
