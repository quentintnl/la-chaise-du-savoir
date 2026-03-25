package fr.lachaisedusavoir.models;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "round_answer")
public class RoundAnswer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "round_id", nullable = false)
    private Rounds round_id;

    @Column(name = "player_id")
    @JoinColumn(name = "player_id", nullable = false)
    private Integer player_id;

    @Column(name = "correct_answers")
    @JoinColumn(name = "correct_answers", nullable = false)
    private Integer correct_answers;

}
