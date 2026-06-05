package fr.lachaisedusavoir.dto;

import lombok.Data;

import java.util.ArrayList;

@Data
public class QuestionDto {
    private String type;
    private String difficulty;
    private String category;
    private String question;
    private String correct_answer;
    private ArrayList<String> incorrect_answer;

    public QuestionDto(String type, String difficulty, String category, String question, String correct_answer, ArrayList<String> incorrect_answer) {
        this.type = type;
        this.difficulty = difficulty;
        this.category = category;
        this.question = question;
        this.correct_answer = correct_answer;
        this.incorrect_answer = incorrect_answer;
    }
}
