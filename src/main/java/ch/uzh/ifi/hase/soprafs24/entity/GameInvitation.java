package ch.uzh.ifi.hase.soprafs24.entity;

import ch.uzh.ifi.hase.soprafs24.constant.RequestStatus;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "GAMEINVITATION")
public class GameInvitation implements Serializable{

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
}