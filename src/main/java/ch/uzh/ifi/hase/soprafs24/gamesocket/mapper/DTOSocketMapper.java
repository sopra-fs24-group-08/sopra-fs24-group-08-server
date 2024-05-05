package ch.uzh.ifi.hase.soprafs24.gamesocket.mapper;

import ch.uzh.ifi.hase.soprafs24.entity.Card;
import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.entity.GridSquare;
import ch.uzh.ifi.hase.soprafs24.entity.Player;
import ch.uzh.ifi.hase.soprafs24.gamesocket.dto.GameStateDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.CardDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.GridSquareDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.stream.Collectors;

@Mapper
public interface DTOSocketMapper {
    DTOSocketMapper INSTANCE = Mappers.getMapper(DTOSocketMapper.class);


    // Updated DTO mappings to utilize direct list mapping
    @Mapping(target = "card", source = "card", qualifiedByName = "safeCard")
    GridSquareDTO convertEntityToGridSquareDTO(GridSquare square);

    // Rename or clarify usage
    @Named("defaultCard")
    CardDTO convertEntityToCardDTO(Card card);

    @Named("safeCard")
    default CardDTO safeConvertEntityToCardDTO(Card card) {
        if (card == null) {
            return null;
        }
        return convertEntityToCardDTO(card);
    }

    default GridSquareDTO safeConvertEntityToGridSquareDTO(GridSquare square) {
        if (square == null) {
            return null;
        }
        return convertEntityToGridSquareDTO(square);
    }

    @Mapping(target = "winnerId", expression = "java(game.getWinner() != null ? game.getWinner().getId() : null)")
    GameStateDTO convertEntityToGameStateDTO(Game game);

    default GameStateDTO convertEntityToGameStateDTOForPlayer(Game game, Long playerId) {
        GameStateDTO gameStateDTO = convertEntityToGameStateDTO(game);

        Player player = game.getPlayers().stream()
                .filter(p -> p.getId().equals(playerId))
                .findFirst()
                .orElse(null);

        if (player != null) {
            gameStateDTO.setPlayerHand(player.getHand().stream().map(this::safeConvertEntityToCardDTO).collect(Collectors.toList()));
            gameStateDTO.setCurrentScore(player.getScore());

            Player opponent = game.getPlayers().stream()
                    .filter(p -> !p.getId().equals(playerId))
                    .findFirst()
                    .orElse(null);

            if (opponent != null) {
                gameStateDTO.setOpponentScore(opponent.getScore());
            }
        }

        gameStateDTO.setGridSquares(game.getBoard().getGridSquares().stream().map(this::safeConvertEntityToGridSquareDTO).collect(Collectors.toList()));

        return gameStateDTO;
    }
}
