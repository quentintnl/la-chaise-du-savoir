package fr.lachaisedusavoir.accessingdatamysql;

import org.springframework.data.repository.CrudRepository;

public interface GameSessionRepository extends CrudRepository<GameSession, Integer> {

}
