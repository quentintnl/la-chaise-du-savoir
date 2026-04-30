package fr.lachaisedusavoir.repository;

import fr.lachaisedusavoir.models.GameMatch;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameMatchRepository extends CrudRepository<GameMatch, Integer> {

}

