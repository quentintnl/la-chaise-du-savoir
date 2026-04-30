package fr.lachaisedusavoir.service;

import fr.lachaisedusavoir.models.GameMatch;
import fr.lachaisedusavoir.models.User;
import fr.lachaisedusavoir.repository.GameMatchRepository;
import fr.lachaisedusavoir.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class MatchService {
    private final GameMatchRepository gameMatchRepository;
    private  final UserRepository userRepository;

    @Transactional
    public GameMatch createMatch(Integer user1Id, Integer user2Id) {
        User user1 = userRepository.findById(user1Id).orElseThrow();
        User user2 = userRepository.findById(user2Id).orElseThrow();

        GameMatch match = new GameMatch();
        match.setUser1(user1);
        match.setUser2(user2);  
        match.setCreated_at(new java.util.Date());
        match.setStatus(false);

        return gameMatchRepository.save(match);
    }
}
