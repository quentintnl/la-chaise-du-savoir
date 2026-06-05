package fr.lachaisedusavoir.service;

import fr.lachaisedusavoir.dto.QuestionDto;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class JsonToQuestionDtoParserService {

    public List<QuestionDto> parse(String json) {
        List<QuestionDto> questions = new ArrayList<>();
        JSONObject jsonObject = new JSONObject(json);
        JSONArray results = jsonObject.getJSONArray("results");

        for (int i = 0; i < results.length(); i++) {
            JSONObject questionJson = results.getJSONObject(i);

            String category = questionJson.getString("category");
            String type = questionJson.getString("type");
            String difficulty = questionJson.getString("difficulty");
            String questionText = questionJson.getString("question");
            String correctAnswer = questionJson.getString("correct_answer");

            JSONArray incorrectAnswersJson = questionJson.getJSONArray("incorrect_answers");
            ArrayList<String> incorrectAnswers = new ArrayList<>();
            for (int j = 0; j < incorrectAnswersJson.length(); j++) {
                incorrectAnswers.add(incorrectAnswersJson.getString(j));
            }

            questions.add(new QuestionDto(category, type, difficulty, questionText, correctAnswer, incorrectAnswers));
        }
        return questions;
    }
}