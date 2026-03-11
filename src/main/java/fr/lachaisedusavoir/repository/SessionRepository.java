package fr.lachaisedusavoir.repository;

import fr.lachaisedusavoir.model.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {
    Optional<Session> findByApiToken(String apiToken);
    Optional<Session> findByUserId(Long userId);
    void deleteByUserId(Long userId);
}
