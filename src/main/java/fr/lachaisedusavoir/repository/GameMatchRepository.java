package fr.lachaisedusavoir.repository;

import fr.lachaisedusavoir.models.GameMatch;
import org.springframework.data.repository.CrudRepository;

public interface GameMatchRepository extends CrudRepository<GameMatch, Integer> {

}

