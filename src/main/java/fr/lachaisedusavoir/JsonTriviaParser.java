package fr.lachaisedusavoir;

import fr.lachaisedusavoir.DTO.QuestionDTO;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;

public class JsonTriviaParser {

    public jsonQuestionToQuestionClas

    public void test_wesh(){
    try {
        ArrayList<String>
        QuestionDTO q = new QuestionDTO("Uip","Iip","Iip","Iip","Iip",);

        ObjectMapper mapper = new ObjectMapper();
        String jsonStr = mapper.writeValueAsString(q);
        System.out.println("JSON string : "+ jsonStr);

        String jsonInput = "{\"name\":\"Jane\",\"surname\":\"Doe\",\"age\":25}";
        QuestionDTO result = mapper.readValue(jsonStr, QuestionDTO.class);
        System.out.println("User from JSON: " + result.getType() + result.getDifficulty() + result.age);
    }catch (Exception e) {
        e.printStackTrace();
    }
    }
}
