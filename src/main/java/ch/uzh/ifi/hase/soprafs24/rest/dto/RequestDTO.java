package ch.uzh.ifi.hase.soprafs24.rest.dto;

import ch.uzh.ifi.hase.soprafs24.constant.RequestStatus;
import ch.uzh.ifi.hase.soprafs24.constant.RequestType;

public class RequestDTO {

    private Long senderId;
    private String senderName;
    private Long receiverId;
    private String receiverName;
    private RequestType requestType;
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

    public RequestType getRequestType(){
        return requestType;
    }

    public void setRequestType(RequestType requestType){
        this.requestType = requestType;
    }

    public String getSenderName(){
        return senderName;
    }

    public void setSenderName(String senderName){
        this.senderName = senderName;
    }

    public String getReceiverName(){
        return receiverName;
    }

    public void setReceiverName(String receiverName){
        this.receiverName = receiverName;
    }

}

