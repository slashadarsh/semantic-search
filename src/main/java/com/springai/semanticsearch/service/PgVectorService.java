package com.springai.semanticsearch.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.ai.embedding.EmbeddingModel;

import java.util.List;

@Service
public class PgVectorService {

    private static final float MATCH_THRESHOLD = 0.7f;
    private static final int MATCH_CNT = 5;

    private JdbcClient jdbcClient;

    private EmbeddingModel embeddingModel;

    @Autowired
    public PgVectorService(JdbcClient jdbcClient, EmbeddingModel embeddingModel) {
        this.jdbcClient = jdbcClient;
        this.embeddingModel = embeddingModel;
    }

    public void insertRecord(Long id, String content, List<Double> contentEmbeddings) {
        String contentEmbeddingsStr = contentEmbeddings.toString().replace("[", "{").replace("]", "}");
        jdbcClient.sql("INSERT INTO test_embeddings (id, content, content_embeddings) VALUES (:id, :content, :content_embeddings::double precision[])")
                .param("id", id)
                .param("content", content)
                .param("content_embeddings", contentEmbeddingsStr)
                .update();
    }



    public List<String> searchTestEmbeddings(String prompt) {
        List<Double> promptEmbedding = embeddingModel.embed(prompt);

        JdbcClient.StatementSpec query = jdbcClient.sql(
                        "SELECT content " +
                                "FROM test_embeddings WHERE 1 - (content_embeddings <=> :user_promt::vector) > :match_threshold "
                                +
                                "ORDER BY content_embeddings <=> :user_promt::vector LIMIT :match_cnt")
                .param("user_promt", promptEmbedding.toString())
                .param("match_threshold", MATCH_THRESHOLD)
                .param("match_cnt", MATCH_CNT);

        return query.query(String.class).list();
    }

    public Long getLatestId() {
        JdbcClient.StatementSpec query = jdbcClient.sql(
                "SELECT id " +
                        "FROM test_embeddings " +
                        "ORDER BY id DESC " +
                        "LIMIT 1");

        return query.query(Long.class).optional().orElse(0L);
    }

}
