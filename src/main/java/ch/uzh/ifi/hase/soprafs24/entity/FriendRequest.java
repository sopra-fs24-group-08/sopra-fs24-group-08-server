package ch.uzh.ifi.hase.soprafs24.entity;

import javax.persistence.*;

import ch.uzh.ifi.hase.soprafs24.constant.RequestStatus;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "FRIENDREQUEST")
public class FriendRequest implements Serializable {
    
    /* benefit of having a request entity: requests need to be stored, by checking requests, we don't need to check
     * if friendId exists or not. By checking status, we return polling response.
     */

    @Id
    @GeneratedValue
    private Long id;

    @Column
    private LocalDateTime creationTime;

    @Column
    private Long senderId;

    @Column(nullable = false)
    private Long receiverId;

    @Column
    private RequestStatus status;

    public Long getId(){
        return id;
    }

    public void setId(Long id){
        this.id = id;
    }

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

    public LocalDateTime getCreationTime(){
        return creationTime;
    }

    public void setCreationTime(LocalDateTime creationTime){
        this.creationTime = creationTime;
    }
}
