package fr.lachaisedusavoir.models;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "game_session")
public class GameSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "api_token", nullable = false, length = 255)
    private String apiToken;
}

