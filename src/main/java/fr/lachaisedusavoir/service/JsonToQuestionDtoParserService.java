package fr.lachaisedusavoir.service;

import fr.lachaisedusavoir.dto.QuestionDto;

import tools.jackson.databind.ObjectMapper;


import java.util.ArrayList;

public class JsonToQuestionDtoParserService {
    public void jsonQuestionToQuestionDto(){

    }

    public void jsonTokenToTokenDTO(){
        //String jsonResponse =
    }

    public void test_wesh(){
        try {
            ArrayList<String> incorrect = new ArrayList<String>();
            incorrect.add("tip");
            incorrect.add("tip1");
            QuestionDto q = new QuestionDto("Uip","Iip","Iip","Iip","Iip",incorrect);

            ObjectMapper mapper = new ObjectMapper();
            String jsonStr = mapper.writeValueAsString(q);
            System.out.println("JSON string : "+ jsonStr);

            String jsonInput = "{\"type\":\"Jane\",\"difficulty\":\"Doe\",\"category\":\"Doe\",\"question\":\"Doe?\",\"correct_answer\":\"Doe\",\"incorrect_answer\":\"Doe\",}";
            QuestionDto result = mapper.readValue(jsonStr, QuestionDto.class);
            //QuestionDto result2 = mapper.readValue(new URL("https://opentdb.com/api.php?amount=1"), QuestionDto.class);
            System.out.println("User from JSON: " + result.getType() + " " + result.getDifficulty());
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
