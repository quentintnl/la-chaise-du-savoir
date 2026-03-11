package fr.lachaisedusavoir.repository;

import fr.lachaisedusavoir.models.GameSession;
import org.springframework.data.repository.CrudRepository;

public interface GameSessionRepository extends CrudRepository<GameSession, Integer> {

}

