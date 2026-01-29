package fr.lachaisedusavoir.accessingdatamysql;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping(path="/api")
public class MainController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private WinRepository winRepository;

    @Autowired
    private GameSessionRepository gameSessionRepository;

    @Autowired
    private WinSessionRepository winSessionRepository;

    // User endpoints
    @PostMapping(path="/user/add")
    public @ResponseBody String addNewUser(@RequestParam String login,
                                           @RequestParam String password) {
        User user = new User();
        user.setLogin(login);
        user.setPassword(password);
        userRepository.save(user);
        return "User saved";
    }

    @GetMapping(path="/user/all")
    public @ResponseBody Iterable<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Session endpoints
    @PostMapping(path="/session/add")
    public @ResponseBody String addNewSession(@RequestParam Integer userId,
                                              @RequestParam String apiToken) {
        Session session = new Session();
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return "User not found";
        }
        session.setUser(user);
        session.setApiToken(apiToken);
        sessionRepository.save(session);
        return "Session saved";
    }

    @GetMapping(path="/session/all")
    public @ResponseBody Iterable<Session> getAllSessions() {
        return sessionRepository.findAll();
    }

    // Win endpoints
    @PostMapping(path="/win/add")
    public @ResponseBody String addNewWin(@RequestParam Integer userId,
                                          @RequestParam Integer points) {
        Win win = new Win();
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return "User not found";
        }
        win.setUser(user);
        win.setPoints(points);
        winRepository.save(win);
        return "Win saved";
    }

    @GetMapping(path="/win/all")
    public @ResponseBody Iterable<Win> getAllWins() {
        return winRepository.findAll();
    }

    // GameSession endpoints
    @PostMapping(path="/game-session/add")
    public @ResponseBody String addNewGameSession(@RequestParam Integer userId,
                                                  @RequestParam String apiToken) {
        GameSession gameSession = new GameSession();
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return "User not found";
        }
        gameSession.setUser(user);
        gameSession.setApiToken(apiToken);
        gameSessionRepository.save(gameSession);
        return "Game session saved";
    }

    @GetMapping(path="/game-session/all")
    public @ResponseBody Iterable<GameSession> getAllGameSessions() {
        return gameSessionRepository.findAll();
    }

    // WinSession endpoints
    @PostMapping(path="/win-session/add")
    public @ResponseBody String addNewWinSession(@RequestParam Integer winId,
                                                 @RequestParam Integer gameSessionId) {
        WinSession winSession = new WinSession();
        Win win = winRepository.findById(winId).orElse(null);
        GameSession gameSession = gameSessionRepository.findById(gameSessionId).orElse(null);

        if (win == null) {
            return "Win not found";
        }
        if (gameSession == null) {
            return "Game session not found";
        }

        winSession.setWin(win);
        winSession.setGameSession(gameSession);
        winSessionRepository.save(winSession);
        return "Win session saved";
    }

    @GetMapping(path="/win-session/all")
    public @ResponseBody Iterable<WinSession> getAllWinSessions() {
        return winSessionRepository.findAll();
    }
}
