package ch.uzh.ifi.hase.soprafs24.service;
import ch.uzh.ifi.hase.soprafs24.constant.GameStatus;
import ch.uzh.ifi.hase.soprafs24.constant.MoveType;
import ch.uzh.ifi.hase.soprafs24.entity.*;
import ch.uzh.ifi.hase.soprafs24.repository.CardRepository;
import ch.uzh.ifi.hase.soprafs24.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs24.repository.PlayerRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.GameStateDTO;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.MoveDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;


@Service
@Transactional
public class GameService {
    private final GameRepository gameRepository;
    private final UserRepository userRepository;
    private final PlayerRepository playerRepository;
    private final CardRepository cardRepository;


    @Autowired
    public GameService(@Qualifier("gameRepository") GameRepository gameRepository,UserService userService,PlayerRepository playerRepository,UserRepository userRepository,
                       CardRepository cardRepository) {
        this.gameRepository = gameRepository;
        this.userRepository = userRepository;
        this.playerRepository = playerRepository;
        this.cardRepository = cardRepository;
    }
    //Need method for the matchmaking,once 2 players ready you create game->> We should have done a Lobby perhaps

    public Game createGame() {
        Game game = new Game();

        // Initialize the board and card pile
        Board board = new Board();
        CardPile cardPile = new CardPile();

        game.setBoard(board);
        game.setCardPile(cardPile);

        // Save the fully initialized game
        gameRepository.save(game);
        gameRepository.flush();
        return game;
    }

    public Game retrieveGameState(Long gameId){
        return gameRepository.findById(gameId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Game was not found"));
    }

    public Game startGame(Long gameId,Long userId1, Long userId2) {
        User user1 = userRepository.findById(userId1).orElseThrow();
        User user2 = userRepository.findById(userId2).orElseThrow();

        Game game = gameRepository.findByGameId(gameId);

        Player player1 = new Player(user1, game);
        Player player2 = new Player(user2, game);

        // Coin toss to decide who starts
        boolean firstPlayerStarts = new Random().nextBoolean();
        dealInitialCards(game,player1, player2, firstPlayerStarts);

        playerRepository.save(player1);
        playerRepository.save(player2);

        game.setPlayers(Arrays.asList(player1, player2));
        game.setGameStatus(GameStatus.ONGOING);

        return gameRepository.save(game);
    }

    private void dealInitialCards(Game game,Player player1, Player player2, boolean firstPlayerStarts) {

        // Decide how many cards each player gets
        Player firstPlayer = firstPlayerStarts ? player1 : player2;
        Player secondPlayer = firstPlayerStarts ? player2 : player1;
        List<Card> cards = game.getCardPile().getCards();
        // Distribute cards
        giveCards(firstPlayer, cards, 2);
        giveCards(secondPlayer, cards, 3);

        // Save the remaining cards back to the game's card pile
        game.getCardPile().setCards(cards);
        gameRepository.save(game);
    }

    private void giveCards(Player player, List<Card> pile, int count) {
        for (int i = 0; i < count; i++) {
            if (!pile.isEmpty()) {
                player.addCardToHand(pile.remove(0));
            }
        }}

    public void processMove(Long gameId, MoveDTO move) {
            if (move.getMoveType() == MoveType.PLACE) {
                placeCard(gameId,move);
            } else if (move.getMoveType() == MoveType.DRAW) {
                drawCard(gameId,move);
            } else {
                throw new IllegalArgumentException("Unsupported move type: " + move.getMoveType());
            }
        }


        private void drawCard(Long gameId,MoveDTO move) {
            // Logic to handle drawing a card
            System.out.println("Handling a DRAW move for player " + move.getPlayerId());
            // Further implementation details
        }


        public void placeCard(Long gameId, MoveDTO move) {
        Game game = gameRepository.findById(gameId).orElseThrow();
        Player player = playerRepository.findById(playerId).orElseThrow();
        Card card = cardRepository.findById(cardId).orElseThrow();
        //decide if cardpile diff for every game or every server start diff only
        Board board = game.getBoard();
        GridSquare square = board.getSquareAt(position);

        // Check if player can play the card and update scores accordingly
        if (canPlayCard(player, card, square)) {
            updateStateAfterPlay(game, player, card, square);
        }

        checkGameOverConditions(game);
        gameRepository.save(game);
    }

    private boolean canPlayCard(Player player, Card card, GridSquare square) {
    return true;
    }

    private void updateStateAfterPlay(Game game, Player player, Card card, GridSquare square) {

    }

    private void checkGameOverConditions(Game game) {
        // Logic to check if the game should end
    }

    public Player getWinningPlayer(Long gameId){
        Game game = retrieveGameState(gameId);

        if (game.getPlayers().isEmpty()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"No players inside of this game");
        }
        Player player = Collections.max(game.getPlayers(),new Comparator<Player>(){
            public int compare(Player player1,Player player2){
                return player1.getScore() - player2.getScore();
            }
        });
        return player;

    }

    public Player getWinner(Long gameId){
        Game game = retrieveGameState(gameId);
        Player player = getWinningPlayer(gameId);
        if (game.getGameStatus() == GameStatus.WINNER){
            return player;
        }
        throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE,"Game isn't finished yet");
    }

    public void endGame(Long gameId, Long winnerPlayerId) {
        Game game = gameRepository.findById(gameId).orElseThrow();
        Player winner = playerRepository.findById(winnerPlayerId).orElseThrow();

        game.setGameStatus(GameStatus.WINNER);
        game.setWinner(winner);

        gameRepository.save(game);
    }

}
