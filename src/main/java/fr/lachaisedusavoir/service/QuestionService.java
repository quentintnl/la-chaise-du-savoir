package fr.lachaisedusavoir.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class QuestionService {

    @Autowired
    private RestTemplate restTemplate;

    public QuestionService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String fetchQuestions() {
        final String uri = "https://opentdb.com/api.php?amount=10";
        try {
            return restTemplate.getForObject(uri, String.class);
        } catch (HttpClientErrorException e) {

            System.err.println("Error fetching questions from OpenTDB: " + e.getMessage());
            return null;
        }
    }
}
