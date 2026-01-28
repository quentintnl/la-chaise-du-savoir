package fr.lachaisedusavoir.DTO;

import java.lang.reflect.Array;
import java.util.ArrayList;

@Data
public class QuestionDTO {
    private String type;
    private String difficulty;
    private String category;
    private String question;
    private String correct_answer;
    private ArrayList<String> incorrect_answer;

    public QuestionDTO(String type, String difficulty, String category, String question, String correct_answer, ArrayList<String> incorrect_answer) {
        this.type = type;
        this.difficulty = difficulty;
        this.category = category;
        this.question = question;
        this.correct_answer = correct_answer;
        this.incorrect_answer = incorrect_answer;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public void setCorrect_answer(String correct_answer) {
        this.correct_answer = correct_answer;
    }

    public void setIncorrect_answer(ArrayList<String> incorrect_answer) {
        this.incorrect_answer = incorrect_answer;
    }

    public String getType() {
        return type;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public String getCategory() {
        return category;
    }

    public String getQuestion() {
        return question;
    }

    public String getCorrect_answer() {
        return correct_answer;
    }

    public ArrayList<String> getIncorrect_answer() {
        return incorrect_answer;
    }
}
