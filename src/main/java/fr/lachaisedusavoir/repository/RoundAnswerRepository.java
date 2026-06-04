package fr.lachaisedusavoir.repository;

import fr.lachaisedusavoir.models.RoundAnswer;
import org.springframework.data.repository.CrudRepository;

public interface RoundAnswerRepository extends CrudRepository<RoundAnswer, Integer> {
    @org.springframework.data.jpa.repository.Query("SELECT r FROM RoundAnswer r WHERE r.round_id.id = :roundId AND r.player_id = :playerId")
    java.util.Optional<RoundAnswer> findByRoundIdAndPlayerId(@org.springframework.data.repository.query.Param("roundId") Integer roundId, @org.springframework.data.repository.query.Param("playerId") Integer playerId);
}