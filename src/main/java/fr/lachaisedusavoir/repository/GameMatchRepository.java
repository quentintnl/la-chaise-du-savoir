package fr.lachaisedusavoir.repository;

import fr.lachaisedusavoir.models.GameMatch;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GameMatchRepository extends CrudRepository<GameMatch, Integer> {
    Optional<GameMatch> findByInviteCode(String inviteCode);
    
    @org.springframework.data.jpa.repository.Query("SELECT m FROM GameMatch m WHERE m.user1.id = :userId OR m.user2.id = :userId")
    java.util.List<GameMatch> findAllMatchesByUserId(@org.springframework.data.repository.query.Param("userId") Integer userId);
}
