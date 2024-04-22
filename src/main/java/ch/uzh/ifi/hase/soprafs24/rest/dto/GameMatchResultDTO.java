package ch.uzh.ifi.hase.soprafs24.rest.dto;

public class GameMatchResultDTO {

  private Long userId1;
  private Long userId2;
  private String username1;
  private String username2;

  public Long getUserId1() {
      return userId1;
  }

  public void setUserId1(Long userId1) {
      this.userId1 = userId1;
  }

  public Long getUserId2() {
      return userId2;
  }

  public void setUserId2(Long userId2) {
      this.userId2 = userId2;
  }

  public String getUsername1() {
    return username1;
  }

  public void setUsername1(String username1) {
      this.username1 = username1;
  }

  public String getUsername2() {
    return username2;
  }

  public void setUsername2(String username2) {
      this.username2 = username2;
  }
  
}
