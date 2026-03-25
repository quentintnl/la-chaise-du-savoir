package fr.lachaisedusavoir.models;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "rounds")
public class Rounds {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    @Column(name = "round_number")
    private Integer round_number;

    @Column(name = "total_question")
    private Integer total_question;

}
