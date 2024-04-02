package ch.uzh.ifi.hase.soprafs24.rest.dto;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class UserGetDTO {

  private Long id;
  private String username;
  private LocalDate creation_date;
  private UserStatus status;
  private LocalDate birthday;
  private String token;



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

  public LocalDate getBirthday(){
        return birthday;
  }

  public void setBirthday(LocalDate birthday){
        this.birthday = birthday;
  }

  private String getToken(){
      return token;
  }
}
