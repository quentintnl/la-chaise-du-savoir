package fr.lachaisedusavoir.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 50)
    private String login;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(name = "user_winstreak")
    private Integer userWinstreak = 0;

    @Column(name = "global_points")
    private Integer globalPoints = 0;

    public User(String login, String password) {
        this.login = login;
        this.password = password;
        this.userWinstreak = 0;
        this.globalPoints = 0;
    }
}

