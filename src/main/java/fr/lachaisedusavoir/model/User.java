package fr.lachaisedusavoir.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String login;

    @Column(nullable = false)
    private String password;

    @Column(name = "user_winstreak")
    private Integer userWinstreak;

    @Column(name = "global_points")
    private Integer globalPoints;

    public User(String login, String password) {
        this.login = login;
        this.password = password;
        this.userWinstreak = 0;
        this.globalPoints = 0;
    }
}
