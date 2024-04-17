package ch.uzh.ifi.hase.soprafs24.entity;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import org.springframework.data.annotation.CreatedDate;
import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Internal User Representation
 * This class composes the internal representation of the user and defines how
 * the user is stored in the database.
 * Every variable will be mapped into a database field with the @Column
 * annotation
 * - nullable = false -> this cannot be left empty
 * - unique = true -> this value must be unqiue across the database -> composes
 * the primary key
 */
@Entity
@Table(name = "USER")
public class User implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue
  private Long id;

  @Column(nullable = false, unique = true)
  private String username;

  @CreatedDate
  @Column(nullable = false)
  private LocalDate creation_date;

  @Column(nullable = false)
  private String password;

  @Column(nullable = false, unique = true)
  private String token;

  @Column(nullable = false)
  private UserStatus status;

  @Column(nullable = true)
  private LocalDate birthday;


  //lazy better when you have lots of data
  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
          name = "user_achievements",
          joinColumns = @JoinColumn(name = "user_id"),
          inverseJoinColumns = @JoinColumn(name = "achievement_id")
  )
  private Set<Achievement> achievements = new HashSet<>();

  //Adjust friends depending on how others have implemented it
  @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  @JoinTable(
          name = "user_friends",
          joinColumns = @JoinColumn(name = "user_id"),
          inverseJoinColumns = @JoinColumn(name = "friend_id")
    )
  private Set<User> friends = new HashSet<>();



    public Set<Banner> getBanners() {
        return banners;
    }

    public void setBanners(Set<Banner> banners) {
        this.banners = banners;
    }

    @ManyToMany
    @JoinTable(
            name = "user_banners",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "banner_id")
    )
    private Set<Banner> banners = new HashSet<>();

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
            name = "user_icons",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "icon_id")
    )
  private Set<Icon> icons = new HashSet<>();

  @ManyToOne
  @JoinColumn(name = "curr_icon_id")
  private Icon currIcon;


  public Set<Icon> getIcons() {
        return icons;
  }

  public void setIcons(Set<Icon> icons) {
        this.icons = icons;
  }

  public void addIcon(Icon icon) {
        this.icons.add(icon);
    }



public Set<User> getFriends() {
    return this.friends;
}

public void addFriend(User friend) {
    this.friends.add(friend);
    friend.getFriends().add(this);
}

public void removeFriend(User friend) {
    this.friends.remove(friend);
    friend.getFriends().remove(this);
}

public Set<Achievement> getAchievements() {
  return achievements;
}

public void setAchievements(Set<Achievement> achievements) {
  this.achievements = achievements;
}

public void addAchievement(Achievement achievement) {
    this.achievements.add(achievement);
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

public String getPassword() {
return password;
}

public void setPassword(String password) {
    this.password = password;
}


public String getToken() {
return token;
}

public void setToken(String token) {
this.token = token;
}

public LocalDate getCreation_date(){
    return creation_date;
}

public void setCreation_date(LocalDate creation_date){
    this.creation_date = creation_date;}

  public UserStatus getStatus() {
    return status;
  }

  public void setStatus(UserStatus status) {
    this.status = status;
  }

  public LocalDate getBirthday(){
      return birthday;
  }

  public void setBirthday(LocalDate birthday) {
      this.birthday = birthday;
  }

  public Icon getCurrIcon() {
        return currIcon;
    }

    public void setCurrIcon(Icon currIcon) {
        this.currIcon = currIcon;
    }
}
