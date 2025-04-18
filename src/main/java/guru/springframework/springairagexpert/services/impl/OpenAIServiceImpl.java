package guru.springframework.springairagexpert.services.impl;

import guru.springframework.springairagexpert.model.Answer;
import guru.springframework.springairagexpert.model.Question;
import guru.springframework.springairagexpert.services.OpenAIService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class OpenAIServiceImpl implements OpenAIService {

    final ChatModel chatModel;
    final VectorStore vectorStore;

    @Value("classpath:templates/rag-prompt-template.st")
    private Resource ragPromptTemplate;

    @Value("classpath:templates/system-message.st")
    private Resource systemMessageTemplate;

    @Override
    public Answer getAnswer(Question question) {
        log.info("OpenAIServiceImpl.getAnswer(Question)");
        log.info("Question: {}", question.questionText());

        PromptTemplate systemMessagePromptTemplate = new SystemPromptTemplate(systemMessageTemplate);
        Message systemMessage = systemMessagePromptTemplate.createMessage();

        List<Document> documents = Optional.ofNullable(vectorStore.similaritySearch(SearchRequest.builder()
                .query(question.questionText()).topK(3).build())).orElse(new ArrayList<>());
        List<String> contentList = documents.stream().map(Document::getText).toList();

        PromptTemplate promptTemplate = new PromptTemplate(ragPromptTemplate);

        Message userMessage = promptTemplate.createMessage(Map.of("input", question.questionText(),
                "documents", String.join("\n", contentList)));

        // contentList.forEach(s -> {System.out.println(s + "\n\n\n\n");});

        ChatResponse chatResponse = chatModel.call(new Prompt(List.of(systemMessage, userMessage)));

        return new Answer(chatResponse.getResult().getOutput().getText());
    }

}
