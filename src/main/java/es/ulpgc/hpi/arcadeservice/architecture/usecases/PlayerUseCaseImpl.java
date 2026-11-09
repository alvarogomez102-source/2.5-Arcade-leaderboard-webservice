package es.ulpgc.hpi.arcadeservice.architecture.usecases;

import es.ulpgc.hpi.arcadeservice.architecture.domain.PlayerFactory;
import es.ulpgc.hpi.arcadeservice.architecture.ports.PlayerRepository;
import es.ulpgc.hpi.arcadeservice.architecture.model.Player;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public final class PlayerUseCaseImpl implements PlayerUseCase {
    private final PlayerRepository repository;
    private final AtomicLong nextId = new AtomicLong(1);

    public PlayerUseCaseImpl(PlayerRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    @Override
    public List<Player> list() {
        return repository.findAll();
    }

    @Override
    public Optional<Player> findById(long id) {
        return repository.findById(id);
    }

    @Override
    public Player create(String nickname) {
        if (nickname != null && repository.findAll().stream()
                .anyMatch(p -> p.nickname() != null && p.nickname().trim().equalsIgnoreCase(nickname.trim()))) {
            throw new IllegalArgumentException("Nickname already exists");
        }
        var player = PlayerFactory.create(nickname, nextId::getAndIncrement);
        repository.save(player);
        return player;
    }

    @Override
    public Player addScore(long id, int points) {
        if (points <= 0) {
            throw new IllegalArgumentException("Points must be strictly positive");
        }

        var player = findById(id)
                .orElseThrow(() -> new java.util.NoSuchElementException("Player not found"));

        var updatedPlayer = new Player(player.id(), player.nickname(), player.score() + points);
        repository.save(updatedPlayer);
        return updatedPlayer;
    }

    @Override
    public List<Player> leaderboard() {
        return repository.findAll().stream()
                .sorted(java.util.Comparator.comparingInt(Player::score).reversed()
                        .thenComparing(Player::nickname))
                .toList();
    }
}
