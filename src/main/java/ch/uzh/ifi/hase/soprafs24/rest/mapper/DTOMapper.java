package ch.uzh.ifi.hase.soprafs24.rest.mapper;

import ch.uzh.ifi.hase.soprafs24.entity.FriendRequest;
import ch.uzh.ifi.hase.soprafs24.entity.GameInvitation;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.*;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;


/**
 * DTOMapper
 * This class is responsible for generating classes that will automatically
 * transform/map the internal representation
 * of an entity (e.g., the User) to the external/API representation (e.g.,
 * UserGetDTO for getting, UserPostDTO for creating)
 * and vice versa.
 * Additional mappers can be defined for new entities.
 * Always created one mapper for getting information (GET) and one mapper for
 * creating information (POST).
 */
@Mapper
public interface DTOMapper {

  DTOMapper INSTANCE = Mappers.getMapper(DTOMapper.class);


  @Mapping(source = "username", target = "username")
  @Mapping(source = "password", target = "password")
  @Mapping(source = "name", target = "name")

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "token", ignore = true)
  @Mapping(target = "status", ignore = true)
  @Mapping(target = "creation_date", ignore = true)
  @Mapping(target = "birthday", ignore = true)

  User convertUserPostDTOtoEntity(UserPostDTO userPostDTO);

  @Mapping(source = "id", target = "id")
  @Mapping(source = "name", target = "name")
  @Mapping(source = "username", target = "username")
  @Mapping(source = "status", target = "status")


  UserGetDTO convertEntityToUserGetDTO(User user);

  @Mapping(source = "username", target = "username")
  @Mapping(source = "password", target = "password")

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "name", ignore = true)
  @Mapping(target = "token", ignore = true)
  @Mapping(target = "status", ignore = true)
  @Mapping(target = "creation_date", ignore = true)
  @Mapping(target = "birthday", ignore = true)


  User convertLoginUserPostDTOtoEntity(LoginUserPostDTO loginUserPostDTO);

  @Mapping(source = "username", target = "username")
  @Mapping(source = "birthday", target = "birthday")
  @Mapping(source = "password", target = "password")

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "name", ignore = true)
  @Mapping(target = "token", ignore = true)
  @Mapping(target = "status", ignore = true)
  @Mapping(target = "creation_date", ignore = true)


  User convertEditUserPutDTOtoEntity(EditUserPutDTO editUserPutDTO);

  @Mapping(source = "status", target = "status")
  LogoutUserGetDTO convertEntityToLogoutUserGetDTO(User user);

    // User convert to FriendGetDTO to protect data
    @Mapping(source = "username", target = "username")
    @Mapping(source = "id", target = "id")

    FriendGetDTO convertEntityToFriendGetDTO(User user);

    // friendRequestDTO convert to friendRequest entity and converse
    @Mapping(source = "receiverId", target = "receiverId")

    FriendRequest convertFriendRequestDTOtoEntity(FriendRequestDTO friendRequestDTO);
    FriendRequestDTO convertEntityToFriendRequestDTO(FriendRequest friendRequest);

    // game invitation <-> entity
    @Mapping(source = "receiverId", target = "receiverId")

    GameInvitation convertGameInvitationDTOtoEntity(GameInvitationDTO gameInvitationDTO);
    GameInvitationDTO convertEntityToGameInvitationDTO(GameInvitation gameInvitation);
}
