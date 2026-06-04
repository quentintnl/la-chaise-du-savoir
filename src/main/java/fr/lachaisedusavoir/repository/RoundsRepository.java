package fr.lachaisedusavoir.repository;

import fr.lachaisedusavoir.models.Rounds;
import org.springframework.data.repository.CrudRepository;

public interface RoundsRepository extends CrudRepository<Rounds, Integer> {
    java.util.Optional<Rounds> findByMatchId(Integer matchId);
}
