package com.springai.semanticsearch.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.springai.semanticsearch.dto.FilePath;
import com.springai.semanticsearch.dto.SimilaritySearchRequest;
import com.springai.semanticsearch.service.PdfReaderService;
import com.springai.semanticsearch.service.SimilaritySearchService;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/chat")
public class VectorController {

    private static final String STATUS = "status";
    private static final String CODE = "code";
    private static final String MESSAGE = "message";
    private static final String DATA = "data";
    private static final String OK = "OK";
    private static final int SUCCESS_CODE = 200;

    private final PdfReaderService pdfReaderService;
    private final SimilaritySearchService similaritySearchService;

    public VectorController(PdfReaderService pdfReaderService, SimilaritySearchService similaritySearchService) {
        this.pdfReaderService = pdfReaderService;
        this.similaritySearchService = similaritySearchService;
    }

    @PostMapping("/insert-pdf")
    public ResponseEntity<String> insertDocuments(@RequestBody FilePath filePath) throws IOException {
        String result = pdfReaderService.processPdfFiles(pdfReaderService.getFilesFromFolder(filePath.getFilePath()));
        return ResponseEntity.ok(result);
    }

    @PostMapping("/search-similar-PG")
    public ResponseEntity<List<String>> searchSimilarVectorsPG(@RequestBody SimilaritySearchRequest request) throws IOException {
        List<String> result = similaritySearchService.searchSimilarVectorsFromPG(request.getInput());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/query")
    public ResponseEntity<String> vectorQuery(@RequestBody SimilaritySearchRequest request) throws IOException {
        String result = similaritySearchService.searchIndex(request.getInput(), request.getLanguage());
        return buildJsonResponse(result);
    }

    @PostMapping("/query-wo-rag")
    public ResponseEntity<String> searchWoRag(@RequestBody SimilaritySearchRequest request) throws IOException {
        String result = similaritySearchService.searchIndexWoRAG(request.getInput());
        return buildJsonResponse(result);
    }

    private ResponseEntity<String> buildJsonResponse(String result) throws IOException {
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put(STATUS, OK);
        responseMap.put(CODE, SUCCESS_CODE);
        responseMap.put(MESSAGE, "Request Successful");
        responseMap.put(DATA, Map.of(MESSAGE, result));

        ObjectMapper objectMapper = new ObjectMapper();
        String responseJson = objectMapper.writeValueAsString(responseMap);
        return new ResponseEntity<>(responseJson, HttpStatus.OK);
    }
}