package es.ulpgc.hpi.arcadeservice.architecture;

import es.ulpgc.hpi.arcadeservice.architecture.usecases.PlayerUseCaseImpl;
import es.ulpgc.hpi.arcadeservice.architecture.ports.PlayerRepositoryImpl;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PlayerUseCaseTest {
    @Test
    void createsAndFindsAPlayer() {
        var useCase = new PlayerUseCaseImpl(new PlayerRepositoryImpl());
        var player = useCase.create("Ada");
        assertEquals(1L, player.id());
        assertEquals(0, player.score());
        assertEquals(player, useCase.findById(player.id()).orElseThrow());
    }

    @Test
    void rejectsBlankNickname() {
        var useCase = new PlayerUseCaseImpl(new PlayerRepositoryImpl());
        assertThrows(IllegalArgumentException.class, () -> useCase.create(" "));
    }

    @Test
    void rejectsDuplicateNickname() {
        var useCase = new PlayerUseCaseImpl(new PlayerRepositoryImpl());
        useCase.create("Ada");
        assertThrows(IllegalArgumentException.class, () -> useCase.create("Ada"));
        assertEquals(1, useCase.list().size());
    }

    @Test
    void addsScoreToPlayer() {
        var useCase = new PlayerUseCaseImpl(new PlayerRepositoryImpl());
        var player = useCase.create("Ada");
        var updated = useCase.addScore(player.id(), 100);
        assertEquals(100, updated.score());
    }

    @Test
    void rejectsNonPositiveScore() {
        var useCase = new PlayerUseCaseImpl(new PlayerRepositoryImpl());
        var player = useCase.create("Ada");
        assertThrows(IllegalArgumentException.class, () -> useCase.addScore(player.id(), 0));
        assertThrows(IllegalArgumentException.class, () -> useCase.addScore(player.id(), -50));
    }

    @Test
    void leaderboardOrdersByScoreDescendingAndThenByNickname() {
        var useCase = new PlayerUseCaseImpl(new PlayerRepositoryImpl());
        var p1 = useCase.create("Bob");
        var p2 = useCase.create("Alice");
        var p3 = useCase.create("Charlie");

        useCase.addScore(p1.id(), 50);
        useCase.addScore(p2.id(), 100);
        useCase.addScore(p3.id(), 50);

        var leaderboard = useCase.leaderboard();

        assertEquals("Alice", leaderboard.get(0).nickname());   // 100 pts
        assertEquals("Bob", leaderboard.get(1).nickname());     // 50 pts
        assertEquals("Charlie", leaderboard.get(2).nickname()); // 50 pts
    }
}
