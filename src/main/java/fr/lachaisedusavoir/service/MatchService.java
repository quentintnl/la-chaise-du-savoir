package fr.lachaisedusavoir.service;

import fr.lachaisedusavoir.models.GameMatch;
import fr.lachaisedusavoir.models.User;
import fr.lachaisedusavoir.repository.GameMatchRepository;
import fr.lachaisedusavoir.repository.UserRepository;
import fr.lachaisedusavoir.dto.QuestionDTO;
import fr.lachaisedusavoir.models.Rounds;
import fr.lachaisedusavoir.models.RoundAnswer;
import fr.lachaisedusavoir.models.Win;
import fr.lachaisedusavoir.models.WinSession;
import fr.lachaisedusavoir.repository.RoundsRepository;
import fr.lachaisedusavoir.repository.RoundAnswerRepository;
import fr.lachaisedusavoir.repository.WinRepository;
import fr.lachaisedusavoir.repository.WinSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class MatchService {
    private final GameMatchRepository gameMatchRepository;
    private final UserRepository userRepository;
    private final RoundsRepository roundsRepository;
    private final RoundAnswerRepository roundAnswerRepository;
    private final WinRepository winRepository;
    private final WinSessionRepository winSessionRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    public QuestionDTO getQuestion() {
        String url = "https://opentdb.com/api.php?amount=1&type=multiple";
        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response != null && response.containsKey("results")) {
                List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");
                if (!results.isEmpty()) {
                    Map<String, Object> qData = results.get(0);
                    return new QuestionDTO(
                        (String) qData.get("type"),
                        (String) qData.get("difficulty"),
                        (String) qData.get("category"),
                        (String) qData.get("question"),
                        (String) qData.get("correct_answer"),
                        new ArrayList<>((List<String>) qData.get("incorrect_answers"))
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Fallback
        ArrayList<String> incorrect = new ArrayList<>();
        incorrect.add("Mars");
        incorrect.add("Jupiter");
        incorrect.add("Vénus");
        return new QuestionDTO("multiple", "easy", "Science", "Quelle est notre planète ?", "Terre", incorrect);
    }

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

    @Transactional
    public String submitAnswer(Integer matchId, Integer userId, boolean isCorrect) {
        GameMatch match = gameMatchRepository.findById(matchId)
            .orElseThrow(() -> new RuntimeException("Match not found"));

        if (!isCorrect) {
            return "Mauvaise réponse, réessayez la prochaine fois !";
        }

        Rounds round = roundsRepository.findByMatchId(matchId).orElseGet(() -> {
            Rounds r = new Rounds();
            r.setMatch(match);
            r.setRound_number(1);
            r.setTotal_question(10);
            return roundsRepository.save(r);
        });

        RoundAnswer ra = roundAnswerRepository.findByRoundIdAndPlayerId(round.getId(), userId).orElseGet(() -> {
            RoundAnswer newRa = new RoundAnswer();
            newRa.setRound_id(round);
            newRa.setPlayer_id(userId);
            newRa.setCorrect_answers(0);
            return roundAnswerRepository.save(newRa);
        });

        ra.setCorrect_answers(ra.getCorrect_answers() + 1);
        roundAnswerRepository.save(ra);

        if (ra.getCorrect_answers() >= 5) {
            User user = userRepository.findById(userId).orElseThrow();
            user.setGlobalPoints(user.getGlobalPoints() != null ? user.getGlobalPoints() + 10 : 10);
            user.setUserWinstreak(user.getUserWinstreak() != null ? user.getUserWinstreak() + 1 : 1);
            userRepository.save(user);

            Win win = new Win();
            win.setUser(user);
            win.setPoints(10);
            winRepository.save(win);

            WinSession winSession = new WinSession();
            winSession.setWin(win);
            winSession.setGameSession(match);
            winSessionRepository.save(winSession);

            return "Victoire ! Vous avez atteint 5 bonnes réponses.";
        }

        return "Bonne réponse ! Vous avez " + ra.getCorrect_answers() + "/5 bonnes réponses.";
    }
}
