package com.springai.semanticsearch.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.springai.semanticsearch.dto.FilePath;
import com.springai.semanticsearch.dto.SimilaritySearchRequest;
import com.springai.semanticsearch.service.PdfReaderService;
import com.springai.semanticsearch.service.SimilaritySearchService;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class VectorController {

    @Autowired
    private PdfReaderService pdfReaderService;

    @Autowired
    private SimilaritySearchService similaritySearchService;

    @PostMapping("/insert-pdf")
    public ResponseEntity<String> insertDocuments(@RequestBody FilePath filePath) throws IOException {
        return new ResponseEntity<>(pdfReaderService.processPdfFiles(pdfReaderService.getFilesFromFolder(filePath.getFilePath())), HttpStatus.OK);
    }

    @PostMapping("/search-similar-PG")
    public ResponseEntity<List<String>> searchSimilarVectorsPG(@RequestBody SimilaritySearchRequest request) throws IOException {
        return new ResponseEntity<>(similaritySearchService.searchSimilarVectorsFromPG(request.getInput()), HttpStatus.OK);
    }

    @PostMapping("/search")
    public ResponseEntity<String> vectorQuery(@RequestBody SimilaritySearchRequest request) throws IOException {
        String result=similaritySearchService.searchIndex(request.getInput(), request.getLanguage());
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("status", "OK");
        responseMap.put("code", 200);
        responseMap.put("message", "Request Successful");
        responseMap.put("data", Map.of("message", result));
        ObjectMapper objectMapper = new ObjectMapper();
        String responseJson = objectMapper.writeValueAsString(responseMap);
        return new ResponseEntity<>(responseJson, HttpStatus.OK);
    }

    @PostMapping("/search-wo-rag")
    public ResponseEntity<String> searchWoRag(@RequestBody SimilaritySearchRequest request) throws IOException {
        String result = similaritySearchService.searchIndexWoRAG(request.getInput());
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("status", "OK");
        responseMap.put("code", 200);
        responseMap.put("message", "Request Successful");
        responseMap.put("data", Map.of("message", result));
        ObjectMapper objectMapper = new ObjectMapper();
        String responseJson = objectMapper.writeValueAsString(responseMap);
        return new ResponseEntity<>(responseJson, HttpStatus.OK);
    }
}
