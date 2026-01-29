package fr.lachaisedusavoir.accessingdatamysql;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

@Entity
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

    // Getters et Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getUserWinstreak() {
        return userWinstreak;
    }

    public void setUserWinstreak(Integer userWinstreak) {
        this.userWinstreak = userWinstreak;
    }

    public Integer getGlobalPoints() {
        return globalPoints;
    }

    public void setGlobalPoints(Integer globalPoints) {
        this.globalPoints = globalPoints;
    }
}
