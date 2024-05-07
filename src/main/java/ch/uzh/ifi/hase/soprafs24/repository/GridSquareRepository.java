package ch.uzh.ifi.hase.soprafs24.repository;

import ch.uzh.ifi.hase.soprafs24.entity.GridSquare;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GridSquareRepository extends JpaRepository<GridSquare, Long> {
    @Query("SELECT COUNT(gs) FROM GridSquare gs WHERE gs.board.id = :boardId AND gs.cards IS EMPTY")
    long countByBoardIdAndIsOccupiedFalse(@Param("boardId") Long boardId);

    @Query("SELECT SIZE(gs.cards) FROM GridSquare gs WHERE gs.isCardPile = true AND gs.board.id = :boardId")
    int countCardsInCardPileGridSquare(@Param("boardId") Long boardId);
}