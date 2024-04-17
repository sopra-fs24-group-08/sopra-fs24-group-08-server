package ch.uzh.ifi.hase.soprafs24.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;


public class GameSession implements Serializable{

    @Id
    @GeneratedValue
    private Long id;
    @Column
    private LocalDateTime creationTime;
    @Column
    private List<Player> players;


}