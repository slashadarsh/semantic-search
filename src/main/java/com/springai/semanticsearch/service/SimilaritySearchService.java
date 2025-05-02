package com.springai.semanticsearch.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SimilaritySearchService {

    private static final String DEFAULT_SYSTEM_PROMPT = "You are a helpful assistant, please help the user with their query.";

    private final ChatClient chatClient;
    private final PgVectorService pgVectorService;

    @Value("classpath:/prompts/extknowledgebase-pdf.st")
    private Resource extKnowledgeBasePdf;

    public SimilaritySearchService(PgVectorService pgVectorService, ChatClient.Builder builder) {
        this.pgVectorService = pgVectorService;
        this.chatClient = builder.build();
    }

    public List<String> searchSimilarVectorsFromPG(String input) throws IOException {
        return pgVectorService.searchCampaignEmbeddings(input);
    }

    public String searchIndex(String input, String language) throws IOException {
        List<String> contextList = pgVectorService.searchCampaignEmbeddings(input);
        String context = contextList.stream().collect(Collectors.joining("\n"));
        System.out.println("Context: " + context);

        return buildChatPrompt(input, context, language);
    }

    public String searchIndexWoRAG(String input) throws IOException {
        return buildChatPrompt(input, null, null);
    }

    private String buildChatPrompt(String input, String context, String language) throws IOException {
        System.out.println("context is : " + context);
        return chatClient.prompt()
                .system(s -> {
                    s.text(extKnowledgeBasePdf);
                    s.param("pdf_extract",context);
                    s.param("language",language);
                })
                .user(u -> u.text(input))
                .call().content();
    }
}