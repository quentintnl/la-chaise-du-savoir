package fr.lachaisedusavoir.controller;

import fr.lachaisedusavoir.dto.QuestionDto;
import fr.lachaisedusavoir.service.JsonToQuestionDtoParserService;
import fr.lachaisedusavoir.service.QuestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    private final QuestionService questionService;
    private final JsonToQuestionDtoParserService jsonToQuestionDtoParserService;

    public QuestionController(QuestionService questionService, JsonToQuestionDtoParserService jsonToQuestionDtoParserService) {
        this.questionService = questionService;
        this.jsonToQuestionDtoParserService = jsonToQuestionDtoParserService;
    }

    @GetMapping
    public ResponseEntity<List<QuestionDto>> getQuestions() {
        String questionsJson = questionService.fetchQuestions();
        if (questionsJson != null) {
            List<QuestionDto> questions = jsonToQuestionDtoParserService.parse(questionsJson);
            return ResponseEntity.ok(questions);
        }
        return ResponseEntity.status(500).build();
    }
}