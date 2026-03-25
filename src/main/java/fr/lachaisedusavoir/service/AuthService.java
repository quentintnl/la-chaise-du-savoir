package fr.lachaisedusavoir.service;

import fr.lachaisedusavoir.models.Session;
import fr.lachaisedusavoir.repository.SessionRepository;
import fr.lachaisedusavoir.models.User;
import fr.lachaisedusavoir.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Session signup(String login, String password) {
        // Vérifier si l'utilisateur existe déjà
        if (userRepository.existsByLogin(login)) {
            throw new IllegalArgumentException("User already exists with login: " + login);
        }

        // Créer l'utilisateur avec mot de passe hashé
        User user = new User(login, passwordEncoder.encode(password));
        User savedUser = userRepository.save(user);

        // Créer une session pour l'utilisateur
        String apiToken = UUID.randomUUID().toString();
        Session session = new Session(savedUser, apiToken);

        return sessionRepository.save(session);
    }

    @Transactional
    public Session login(String login, String password) {
        // Trouver l'utilisateur
        User user = userRepository.findByLogin(login)
                .orElseThrow(() -> new IllegalArgumentException("Invalid login or password"));

        // Vérifier le mot de passe
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Invalid login or password");
        }

        // Supprimer les anciennes sessions
        sessionRepository.deleteByUserId(user.getId());

        // Créer une nouvelle session
        String apiToken = UUID.randomUUID().toString();
        Session session = new Session(user, apiToken);

        return sessionRepository.save(session);
    }

    @Transactional
    public void logout(Integer userId) {
        sessionRepository.deleteByUserId(userId);
    }

    public User getUserById(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé avec l'ID: " + userId));
    }
}
