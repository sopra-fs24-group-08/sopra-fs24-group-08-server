package ch.uzh.ifi.hase.soprafs24.rest.mapper;

import ch.uzh.ifi.hase.soprafs24.entity.Board;
import ch.uzh.ifi.hase.soprafs24.entity.GridSquare;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import java.util.List;
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
    @Mapping(target = "creation_date", ignore = true)
    @Mapping(target = "birthday", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "token", ignore = true)
    @Mapping(target = "status", ignore = true)
    User userPostDTOToUser(UserPostDTO userPostDTO);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "grid", source = "grid")
    @Mapping(target = "player1Id", source = "player1Id")
    @Mapping(target = "player2Id", source = "player2Id")
    @Mapping(target = "coinTossResult", source = "coinTossResult")
    @Mapping(target = "player1sTurn", source = "player1sTurn")
    @Mapping(target = "awaitingPlayerChoice", source = "awaitingPlayerChoice")
    GameGetDTO boardToGameGetDTO(Board board);

    default List<GridSquareDTO> mapGridToGridSquareDTOs(List<GridSquare> grid) {
        if (grid == null) {
            return null;
        }
        return grid.stream()
                .map(this::gridSquareToGridSquareDTO)
                .collect(Collectors.toList());
    }
    GridSquareDTO gridSquareToGridSquareDTO(GridSquare gridSquare);




    @Mapping(source = "id", target = "id")
    GameGetDTO convertBoardToGameGetDTO(Board board);



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
    @Mapping(source = "status", target = "status")
    @Mapping(source = "creation_date", target = "creation_date")
    @Mapping(source = "birthday", target = "birthday")


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


}