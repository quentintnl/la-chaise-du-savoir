package fr.lachaisedusavoir.service;

import fr.lachaisedusavoir.models.GameMatch;
import fr.lachaisedusavoir.models.User;
import fr.lachaisedusavoir.repository.GameMatchRepository;
import fr.lachaisedusavoir.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;

@Service
@RequiredArgsConstructor
public class MatchService {
    private final GameMatchRepository gameMatchRepository;
    private final UserRepository userRepository;

    @Transactional
    public GameMatch createMatch(Integer user1Id) {
        User user1 = userRepository.findById(user1Id).orElseThrow(() -> new RuntimeException("User not found"));

        GameMatch match = new GameMatch();
        match.setUser1(user1);
        match.setCreated_at(new java.util.Date());
        match.setStatus(false);

        // Generate 4 digit code
        String inviteCode = String.format("%04d", new Random().nextInt(10000));
        match.setInviteCode(inviteCode);

        return gameMatchRepository.save(match);
    }

    @Transactional
    public GameMatch joinMatch(String inviteCode, Integer user2Id) {
        GameMatch match = gameMatchRepository.findByInviteCode(inviteCode)
            .orElseThrow(() -> new RuntimeException("Match not found for code: " + inviteCode));

        if (match.getUser2() != null) {
            throw new RuntimeException("Match is already full");
        }

        User user2 = userRepository.findById(user2Id).orElseThrow(() -> new RuntimeException("User not found"));
        match.setUser2(user2);
        match.setStatus(true);

        return gameMatchRepository.save(match);
    }
}
