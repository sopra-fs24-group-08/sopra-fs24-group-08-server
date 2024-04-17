package ch.uzh.ifi.hase.soprafs24.rest.dto;

import java.util.List;

public class CombinedUpdateDTO {

    private List<FriendRequestDTO> friendRequestDTOs;
    private List<GameInvitationDTO> gameInvitationDTOs;

    public CombinedUpdateDTO(List<FriendRequestDTO> friendRequestDTOs, List<GameInvitationDTO> gameInviteDTOs) {
        this.friendRequestDTOs = friendRequestDTOs;
        this.gameInvitationDTOs = gameInviteDTOs;
    }

    public List<FriendRequestDTO> getFriendRequestDTOs(){
        return friendRequestDTOs;
    }

    public void addFriendRequestDTOs(List<FriendRequestDTO> friendRequestDTOs){
        this.friendRequestDTOs.addAll(friendRequestDTOs);
    }
    public void setFriendRequestDTOs(List<FriendRequestDTO> friendRequestDTOs){
        this.friendRequestDTOs = friendRequestDTOs;
    }

    public List<GameInvitationDTO> getGameInvitationDTOs(){
        return gameInvitationDTOs;
    }

    public void addGameInvitationDTOs(List<GameInvitationDTO> gameInvitationDTOs){
        this.gameInvitationDTOs.addAll(gameInvitationDTOs);
    }

    public void setGameInvitationDTOs(List<GameInvitationDTO> gameInvitationDTOs){
        this.gameInvitationDTOs = gameInvitationDTOs;
    }

}