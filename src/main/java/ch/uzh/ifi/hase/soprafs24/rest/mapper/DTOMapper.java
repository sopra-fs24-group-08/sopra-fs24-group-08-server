package ch.uzh.ifi.hase.soprafs24.rest.mapper;

import ch.uzh.ifi.hase.soprafs24.entity.*;
import ch.uzh.ifi.hase.soprafs24.gamesocket.dto.GameStateDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.*;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    //Functional Mapping Working for the OutsideGame Features
    @Mapping(source = "username", target = "username")
    @Mapping(source = "password", target = "password")

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "token", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "creation_date", ignore = true)
    @Mapping(target = "birthday", ignore = true)

    User convertUserPostDTOtoEntity(UserPostDTO userPostDTO);



    IconGetDTO iconToIconGetDTO(Icon icon);
    BannerGetDTO bannerToBannerDTO(Banner banner);
    AchievementGetDTO achievementToAchievementDTO(Achievement achievement);
    @Mapping(source = "id", target = "id")
    @Mapping(source = "username", target = "username")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "birthday", target = "birthday")
    @Mapping(source = "creation_date", target = "creation_date")
    @Mapping(source = "currIcon", target = "currIcon" )
    @Mapping(target = "icons", expression = "java(mapIcons(user.getIcons()))")
    @Mapping(source = "banners", target = "banners")
    @Mapping(source = "achievements", target = "achievements")
    @Mapping(source = "token", target = "token")

    UserGetDTO convertEntityToUserGetDTO(User user);
    default List<IconGetDTO> mapIcons(Set<Icon> icons) {
        return icons.stream()
                .map(this::iconToIconGetDTO)
                .collect(Collectors.toList());
    }

    @Mapping(source = "username", target = "username")
    @Mapping(source = "password", target = "password")

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "token", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "creation_date", ignore = true)
    @Mapping(target = "birthday", ignore = true)

    User convertLoginUserPostDTOtoEntity(LoginUserPostDTO loginUserPostDTO);


    @Mapping(source = "username", target = "username")
    @Mapping(source = "birthday", target = "birthday")
    @Mapping(source = "password", target = "password")

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "token", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "creation_date", ignore = true)

    User convertEditUserPutDTOtoEntity(EditUserPutDTO editUserPutDTO);


    // User convert to FriendGetDTO to protect data
    @Mapping(source = "username", target = "username")
    @Mapping(source = "id", target = "id")

    FriendGetDTO convertEntityToFriendGetDTO(User user);

    // friendRequestDTO convert to friendRequest entity and converse
    FriendRequest convertFriendRequestDTOtoEntity(FriendRequestDTO friendRequestDTO);
    FriendRequestDTO convertEntityToFriendRequestDTO(FriendRequest friendRequest);

    // convert to other user
    OtherUserGetDTO convertEntityToOtherUserGetDTO(User user);
}


