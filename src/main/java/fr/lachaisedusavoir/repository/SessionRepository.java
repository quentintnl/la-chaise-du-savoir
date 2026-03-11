package fr.lachaisedusavoir.repository;

import fr.lachaisedusavoir.models.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Repository
public interface SessionRepository extends JpaRepository<Session, Integer> {
    Optional<Session> findByApiToken(String apiToken);
    Optional<Session> findByUserId(Integer userId);
    
    @Modifying
    @Transactional
    void deleteByUserId(Integer userId);
}

