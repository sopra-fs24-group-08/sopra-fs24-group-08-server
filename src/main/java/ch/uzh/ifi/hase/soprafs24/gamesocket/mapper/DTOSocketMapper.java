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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Mapper
public interface DTOSocketMapper {
    DTOSocketMapper INSTANCE = Mappers.getMapper(DTOSocketMapper.class);

    CardDTO convertEntityToCardDTO(Card card);

    default List<CardDTO> mapCards(List<Card> cards) {
        if (cards == null) {
            return null;
        }
        return cards.stream().map(this::convertEntityToCardDTO).collect(Collectors.toList());
    }



    @Mapping(target = "id", source = "id")
    @Mapping(target = "color", source = "color")
    @Mapping(target = "cards", expression = "java(mapCardsBasedOnType(square))")
    @Mapping(target = "occupied", expression = "java(square.isOccupied())")
    GridSquareDTO convertEntityToGridSquareDTO(GridSquare square);


    default int getCardPileSize(Game game) {
        GridSquare cardPileSquare = game.getBoard().getCardPileSquare();
        return (cardPileSquare != null && cardPileSquare.getCards() != null) ? cardPileSquare.getCards().size() : 0;
    }

    default List<CardDTO> mapCardsBasedOnType(GridSquare square) {
        if (square.isCardPile()) {
            return new ArrayList<>();  // Always return an empty array for the card pile
        } else {
            return mapCards(square.getCards());  // Return the list of CardDTO or an empty list
        }
    }

    default List<GridSquareDTO> mapGridSquares(List<GridSquare> squares) {
        if (squares == null) {
            return new ArrayList<>();
        }
        return squares.stream().map(this::convertEntityToGridSquareDTO).collect(Collectors.toList());
    }

    @Mapping(target = "winnerId", source = "winner.id", defaultExpression = "java(null)")
    @Mapping(target = "loserId", source = "loser.id", defaultExpression = "java(null)")
    @Mapping(target = "gridSquares", source = "board.gridSquares")
    GameStateDTO convertEntityToGameStateDTO(Game game);

    default GameStateDTO convertEntityToGameStateDTOForPlayer(Game game, Long playerId) {
        // This method customizes the game state to be specific to the player, ensuring they do not see card details they shouldn't
        Player player = game.getPlayers().stream()
                .filter(p -> p.getId().equals(playerId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Player not found"));

        Player opponent = game.getPlayers().stream()
                .filter(p -> !p.getId().equals(playerId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Opponent not found"));

        System.out.println("Converting game with playerID" +game + "////"+ playerId);
        GameStateDTO gameStateDTO = convertEntityToGameStateDTO(game);
        gameStateDTO.setPlayerHand(mapCards(player.getHand()));
        gameStateDTO.setCardPileSize(getCardPileSize(game));
        gameStateDTO.setCurrentScore(player.getScore());
        gameStateDTO.setOpponentScore(opponent.getScore());
        gameStateDTO.setGridSquares(mapGridSquares(game.getBoard().getGridSquares()));
        return gameStateDTO;
    }
}
