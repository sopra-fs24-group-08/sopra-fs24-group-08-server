package ch.uzh.ifi.hase.soprafs24.rest.mapper;
import ch.uzh.ifi.hase.soprafs24.entity.Icon;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.IconGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPostDTO;
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

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "token", ignore = true),
            @Mapping(target = "creation_date", ignore = true),
            @Mapping(target = "status", ignore = true),
            @Mapping(target = "birthday", ignore = true),
            @Mapping(source = "password", target = "password"),
            @Mapping(source = "username", target = "username")})
  User convertUserPostDTOtoEntity(UserPostDTO userPostDTO);



  IconGetDTO convertEntityToIconGetDTO(Icon icon);
    @Mapping(source = "id", target = "id")
    @Mapping(source = "username", target = "username")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "birthday", target = "birthday")
    @Mapping(source = "creation_date", target = "creation_date")
    @Mapping(source = "icons", target = "icons") // Ensure this mapping is correct
    UserGetDTO convertEntityToUserGetDTO(User user);
    default List<IconGetDTO> mapIcons(Set<Icon> icons) {
        return icons.stream().map(this::convertEntityToIconGetDTO).collect(Collectors.toList());
    }


}
