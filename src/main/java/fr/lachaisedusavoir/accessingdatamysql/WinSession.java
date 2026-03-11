package fr.lachaisedusavoir.accessingdatamysql;

import jakarta.persistence.*;

@Entity
@Table(name = "win_session")
public class WinSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "win_id", nullable = false)
    private Win win;

    @ManyToOne
    @JoinColumn(name = "game_session_id", nullable = false)
    private GameSession gameSession;

    // Getters et Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Win getWin() {
        return win;
    }

    public void setWin(Win win) {
        this.win = win;
    }

    public GameSession getGameSession() {
        return gameSession;
    }

    public void setGameSession(GameSession gameSession) {
        this.gameSession = gameSession;
    }
}
