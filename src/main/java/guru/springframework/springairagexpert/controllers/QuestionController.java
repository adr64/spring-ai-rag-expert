package guru.springframework.springairagexpert.controllers;

import guru.springframework.springairagexpert.model.Answer;
import guru.springframework.springairagexpert.model.Question;
import guru.springframework.springairagexpert.services.OpenAIService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class QuestionController {

    private final OpenAIService openAIService;

    public QuestionController(OpenAIService openAIService) {
        this.openAIService = openAIService;
    }

    @PostMapping("/ask")
    public Answer ask(@RequestBody Question question) {
        return this.openAIService.getAnswer(question);
    }
}
