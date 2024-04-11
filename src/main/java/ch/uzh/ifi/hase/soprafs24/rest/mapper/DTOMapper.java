package ch.uzh.ifi.hase.soprafs24.rest.mapper;
import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.entity.GridRow;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.*;
import org.mapstruct.factory.Mappers;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
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

    UserGetDTO userToUserGetDTO(User user);
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "token", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "creation_date", ignore = true)
    @Mapping(target = "birthday", ignore = true)
    User userPostDTOToUser(UserPostDTO userPostDTO);

    // Custom mappings for complex types or where names/types don't match
    @Mapping(target = "grid", source = "grid")
    GameGetDTO gameToGameGetDTO(Game game);

    // Mapping List<GridRow> to List<GridRowDTO> within the Game to GameGetDTO conversion
    default List<GridRowDTO> mapGridToGridRowDTOs(List<GridRow> grid) {
        if (grid == null) {
            return null;
        }
        return grid.stream()
                .map(this::gridRowToGridRowDTO)
                .collect(Collectors.toList());
    }

    GameGetDTO convertGameToGameGetDTO(Game game);
    @Mapping(target = "grid", ignore = true) // Ignore automatic mapping
    GameGetDTO convertEntityToGameGetDTO(Game game);

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

    // Mapping for individual GridRow to GridRowDTO
    @Mapping(target = "cell1", source = "cell1")
    @Mapping(target = "cell2", source = "cell2")
    @Mapping(target = "cell3", source = "cell3")
    GridRowDTO gridRowToGridRowDTO(GridRow gridRow);

}