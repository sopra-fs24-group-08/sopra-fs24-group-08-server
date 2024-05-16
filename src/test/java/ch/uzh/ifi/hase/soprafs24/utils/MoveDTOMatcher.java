package ch.uzh.ifi.hase.soprafs24.utils;

import ch.uzh.ifi.hase.soprafs24.rest.dto.MoveDTO;
import org.mockito.ArgumentMatcher;

import java.util.Objects;

public class MoveDTOMatcher implements ArgumentMatcher<MoveDTO> {
    private final MoveDTO expected;

    public MoveDTOMatcher(MoveDTO expected) {
        this.expected = expected;
    }

    @Override
    public boolean matches(MoveDTO actual) {
        return actual != null &&
                actual.getPlayerId().equals(expected.getPlayerId()) &&
                actual.getCardId().equals(expected.getCardId()) &&
                Objects.equals(actual.getPosition(), expected.getPosition()) &&
                actual.getMoveType().equals(expected.getMoveType());
    }
}
