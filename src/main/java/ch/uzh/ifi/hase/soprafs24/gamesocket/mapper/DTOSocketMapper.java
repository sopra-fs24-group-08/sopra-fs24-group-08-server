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
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.stream.Collectors;

@Mapper
public interface DTOSocketMapper {
    DTOSocketMapper INSTANCE = Mappers.getMapper(DTOSocketMapper.class);

    @Mapping(source = "gameId", target = "gameId")
    @Mapping(source = "gameStatus", target = "gameStatus")
    @Mapping(source = "currentTurnPlayerId", target = "currentTurnPlayerId")
    @Mapping(source = "winner.id", target = "winnerId")
    GameStateDTO convertEntityToGameStateDTO(Game game);

    default GameStateDTO convertEntityToGameStateDTOForPlayer(Game game, Long playerId) {
        GameStateDTO gameStateDTO = convertEntityToGameStateDTO(game); // Use the basic mapping then modify

        Player player = game.getPlayers().stream()
                .filter(p -> p.getUser().getId().equals(playerId))
                .findFirst()
                .orElse(null);

        if (player != null) {
            List<CardDTO> playerCards = player.getHand().stream()
                    .map(this::convertEntityToCardDTO)
                    .collect(Collectors.toList());
            gameStateDTO.setPlayerHand(playerCards);
        }

        List<GridSquareDTO> gridSquares = game.getBoard().getGridSquares().stream()
                .map(this::convertEntityToGridSquareDTO)
                .collect(Collectors.toList());
        gameStateDTO.setGridSquares(gridSquares);

        return gameStateDTO;
    }

    CardDTO convertEntityToCardDTO(Card card);

    // Central method to handle both card and no card scenarios for GridSquares
    default GridSquareDTO convertEntityToGridSquareDTO(GridSquare square) {
        CardDTO cardDto = square.getCard() != null ? convertEntityToCardDTO(square.getCard()) : null;
        return new GridSquareDTO(square.getId(), square.getColor(), square.isOccupied(), cardDto);
    }
}
