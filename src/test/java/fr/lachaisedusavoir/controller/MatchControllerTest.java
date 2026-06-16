package fr.lachaisedusavoir.controller;

import fr.lachaisedusavoir.dto.QuestionDto;
import fr.lachaisedusavoir.models.GameMatch;
import fr.lachaisedusavoir.models.User;
import fr.lachaisedusavoir.repository.UserRepository;
import fr.lachaisedusavoir.service.JsonToQuestionDtoParserService;
import fr.lachaisedusavoir.service.MatchService;
import fr.lachaisedusavoir.service.QuestionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class MatchControllerTest {

    @Mock
    private MatchService matchService;

    @Mock
    private QuestionService questionService;

    @Mock
    private JsonToQuestionDtoParserService jsonToQuestionDtoParserService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MatchController matchController;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(matchController).build();
    }

    @Test
    void createMatch_shouldReturn500_whenExceptionOccurs() throws Exception {

        when(userRepository.findByLogin("john"))
                .thenThrow(new RuntimeException());

        Authentication auth =
                new UsernamePasswordAuthenticationToken("john", null);

        mockMvc.perform(post("/api/match/create")
                        .principal(auth))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(
                        "Une erreur est survenue lors de la création du match"));
    }
    @Test
    void joinMatch_shouldReturn400_whenRuntimeExceptionOccurs() throws Exception {

        User user = mock(User.class);
        when(user.getId()).thenReturn(2);

        when(userRepository.findByLogin("john"))
                .thenReturn(Optional.of(user));

        when(matchService.joinMatch(anyString(), anyInt()))
                .thenThrow(new RuntimeException("Code invalide"));

        Authentication auth =
                new UsernamePasswordAuthenticationToken("john", null);

        mockMvc.perform(post("/api/match/join")
                        .param("inviteCode", "INVALID")
                        .principal(auth))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Code invalide"));
    }

    @Test
    void getQuestion_shouldReturnQuestions_whenSuccess() throws Exception {

        String json = "{\"questions\":[]}";

        QuestionDto question = mock(QuestionDto.class);

        when(questionService.fetchQuestions())
                .thenReturn(json);

        when(jsonToQuestionDtoParserService.parse(json))
                .thenReturn(List.of(question));

        mockMvc.perform(get("/api/match/1/question"))
                .andExpect(status().isOk());
    }

    @Test
    void getQuestion_shouldReturn500_whenJsonIsNull() throws Exception {

        when(questionService.fetchQuestions())
                .thenReturn(null);

        mockMvc.perform(get("/api/match/1/question"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getQuestion_shouldReturn500_whenExceptionOccurs() throws Exception {

        when(questionService.fetchQuestions())
                .thenThrow(new RuntimeException());

        mockMvc.perform(get("/api/match/1/question"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(
                        "Une erreur est survenue lors de la récupération de la question"));
    }

    @Test
    void submitWinner_shouldIncrementPointsAndSaveUser() {

        User user = new User();
        user.setId(1);
        user.setLogin("john");
        user.setGlobalPoints(5);

        when(userRepository.findByLogin("john"))
                .thenReturn(Optional.of(user));

        Authentication auth =
                new UsernamePasswordAuthenticationToken("john", null);

        matchController.submitWinner(42, auth);

        ArgumentCaptor<User> captor =
                ArgumentCaptor.forClass(User.class);

        verify(userRepository).save(captor.capture());

        User savedUser = captor.getValue();

        assert savedUser.getGlobalPoints() == 6;
    }

    @Test
    void submitWinner_shouldReturnSuccessMessage() {

        User user = new User();
        user.setId(1);
        user.setLogin("john");
        user.setGlobalPoints(0);

        when(userRepository.findByLogin("john"))
                .thenReturn(Optional.of(user));

        Authentication auth =
                new UsernamePasswordAuthenticationToken("john", null);

        var response = matchController.submitWinner(99, auth);

        assert response.getStatusCode().is2xxSuccessful();
        assert response.getBody()
                .equals("Match 99 won by user john (total points: 1)");
    }
}