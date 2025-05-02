package com.springai.semanticsearch.service;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.ai.embedding.EmbeddingModel;

import java.util.ArrayList;
import java.util.List;

@Service
public class PgVectorService {

    private static final float MATCH_THRESHOLD = 0.7f;
    private static final int MATCH_CNT = 5;
    private static final String INSERT_QUERY =
            "INSERT INTO campaign_embeddings (id, content, content_embeddings) VALUES (:id, :content, :content_embeddings::double precision[])";
    private static final String SEARCH_QUERY =
            "SELECT content FROM campaign_embeddings WHERE 1 - (content_embeddings <=> :user_prompt::vector) > :match_threshold " +
                    "ORDER BY content_embeddings <=> :user_prompt::vector LIMIT :match_cnt";
    private static final String GET_LATEST_ID_QUERY =
            "SELECT id FROM campaign_embeddings ORDER BY id DESC LIMIT 1";

    private final JdbcClient jdbcClient;
    private final EmbeddingModel embeddingModel;

    public PgVectorService(JdbcClient jdbcClient, EmbeddingModel embeddingModel) {
        this.jdbcClient = jdbcClient;
        this.embeddingModel = embeddingModel;
    }
    public void insertRecord(Long id, String content, float[] contentEmbeddings) {
        String contentEmbeddingsStr = convertListToPgArray(convertFloatArrayToList(contentEmbeddings));
        jdbcClient.sql(INSERT_QUERY)
                .param("id", id)
                .param("content", content)
                .param("content_embeddings", contentEmbeddingsStr)
                .update();
    }

    public List<String> searchCampaignEmbeddings(String prompt) {
        float[] promptEmbedding = embeddingModel.embed(prompt);
        List<Float> userPromptEmbeddings = convertFloatArrayToList(promptEmbedding);
        System.out.println("userPromptEmbeddings = " + userPromptEmbeddings);
        return jdbcClient.sql(SEARCH_QUERY)
                .param("user_prompt", userPromptEmbeddings.toString())
                .param("match_threshold", MATCH_THRESHOLD)
                .param("match_cnt", MATCH_CNT)
                .query(String.class)
                .list();
    }

    public Long getLatestId() {
        return jdbcClient.sql(GET_LATEST_ID_QUERY)
                .query(Long.class)
                .optional()
                .orElse(0L);
    }

    private String convertListToPgArray(List<Float> list) {
        return list.toString().replace("[", "{").replace("]", "}");
    }

    private List<Float> convertFloatArrayToList(float[] array) {
        List<Float> floatList = new ArrayList<>();
        for (float value : array) {
            floatList.add(value);
        }
        return floatList;
    }
}