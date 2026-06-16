package fr.lachaisedusavoir.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuestionServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private QuestionService questionService;

    private static final String API_URL =
            "https://opentdb.com/api.php?amount=10";

    @Test
    void fetchQuestions_shouldReturnJson_whenApiCallSucceeds() {

        String expectedJson = """
            {
              "response_code": 0,
              "results": []
            }
            """;

        when(restTemplate.getForObject(API_URL, String.class))
                .thenReturn(expectedJson);

        String result = questionService.fetchQuestions();

        assertNotNull(result);
        assertEquals(expectedJson, result);

        verify(restTemplate)
                .getForObject(API_URL, String.class);
    }

    @Test
    void fetchQuestions_shouldReturnNull_whenHttpClientErrorOccurs() {

        when(restTemplate.getForObject(API_URL, String.class))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        String result = questionService.fetchQuestions();

        assertNull(result);

        verify(restTemplate)
                .getForObject(API_URL, String.class);
    }

    @Test
    void fetchQuestions_shouldCallCorrectUrl() {

        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn("{}");

        questionService.fetchQuestions();

        verify(restTemplate)
                .getForObject(
                        "https://opentdb.com/api.php?amount=10",
                        String.class
                );
    }
}