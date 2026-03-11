package fr.lachaisedusavoir.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Table(name = "session")
public class Session {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "api_token", nullable = false, length = 255)
    private String apiToken;

    public Session(User user, String apiToken) {
        this.user = user;
        this.apiToken = apiToken;
    }
}

