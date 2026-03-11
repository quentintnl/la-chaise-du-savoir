package fr.lachaisedusavoir;

import fr.lachaisedusavoir.dto.QuestionDto;
import fr.lachaisedusavoir.service.JsonToQuestionDtoParserService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Main {

	public static void main(String[] args) {
		JsonToQuestionDtoParserService test = new JsonToQuestionDtoParserService();
		test.test_wesh();
		//SpringApplication.run(Main.class, args);
	}

}
