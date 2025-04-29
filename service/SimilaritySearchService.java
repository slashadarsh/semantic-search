package com.springai.semanticsearch.service;


import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SimilaritySearchService {

    private EmbeddingModel embeddingModel;

    private ChatClient chatClient;

    private PgVectorService pgVectorService;

    @Value("classpath:/prompts/extknowledgebase-pdf.st")
    private Resource extKnowledgeBasePdf;

    @Autowired
    public SimilaritySearchService(EmbeddingModel embeddingModel, PgVectorService pgVectorService, ChatClient.Builder builder) {
        this.embeddingModel = embeddingModel;
        this.pgVectorService = pgVectorService;
        this.chatClient = builder.build();
    }
    public List<String> searchSimilarVectorsFromPG(String input) throws IOException {
        return pgVectorService.searchCampaignEmbeddings(input);
    }

    public String searchIndex(String input, String language) throws IOException{
        List<String> contextList = pgVectorService.searchCampaignEmbeddings(input);
        String context=contextList.stream().collect(Collectors.joining("/n "));
        return chatClient.prompt()
                .system(s -> {
                    s.text(extKnowledgeBasePdf);
                    s.param("pdf_extract",context);
                    s.param("language",language);
                })
                .user(u -> {
                    u.text(input);
                })
                .call().content();
    }

    public String searchIndexWoRAG(String input) throws IOException{
        return chatClient.prompt()
                .system(s -> {
                    s.text("You are a helpful assistant, please help the user with their query.");
                })
                .user(u -> {
                    u.text(input);
                })
                .call().content();
    }
}