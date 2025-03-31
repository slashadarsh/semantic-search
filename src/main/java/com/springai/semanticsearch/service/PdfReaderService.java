package com.springai.semanticsearch.service;


import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PdfReaderService {

    private static final int MAX_CHARS = 4000;

    @Autowired
    private EmbeddingModel embeddingModel;

    @Autowired
    private PgVectorService pgVectorService;

    public String processPdfFiles(List<Path> filePaths) throws IOException {
        PDFTextStripper pdfStripper = new PDFTextStripper();
       // int sequenceNumber = getLatestSequenceNumber() + 1;
        long sequenceNumber = pgVectorService.getLatestId() +1;
        for (Path filePath : filePaths) {
            try (PDDocument document = PDDocument.load(filePath.toFile())) {
                int numberOfPages = document.getNumberOfPages();
                for (int i = 0; i < numberOfPages; i++) {
                    pdfStripper.setStartPage(i + 1);
                    pdfStripper.setEndPage(i + 1);
                    String text = pdfStripper.getText(document);
                    for (int j = 0; j < text.length(); j += MAX_CHARS) {
                        String chunk = text.substring(j, Math.min(j + MAX_CHARS, text.length()));
                        storeTextChunksInPostgres(chunk, sequenceNumber);
                    }
                    sequenceNumber++;
                    System.out.println("Processed page " + i + " of " + filePath.getFileName());
                }
            }
        }
        return "Successfully processed PDF files";
    }

    public List<Path> getFilesFromFolder(String folderPath) throws IOException {
        Path path = Paths.get(folderPath);
        List<Path> filesInFolder;
        if (Files.exists(path)) {
            filesInFolder = Files.list(path)
                    .filter(Files::isRegularFile)
                    .collect(Collectors.toList());
        } else {
            throw new IOException("Directory not found: " + folderPath);
        }
        return filesInFolder;
    }


    private void storeTextChunksInPostgres(String chunk, long sequenceNumber) throws IOException {
        List<Double> vector = embeddingModel.embed(chunk);
        pgVectorService.insertRecord(sequenceNumber, chunk, vector);
    }

}