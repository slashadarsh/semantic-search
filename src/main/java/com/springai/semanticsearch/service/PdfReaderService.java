package com.springai.semanticsearch.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.ai.embedding.EmbeddingModel;
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

    private final EmbeddingModel embeddingModel;
    private final PgVectorService pgVectorService;

    public PdfReaderService(EmbeddingModel embeddingModel, PgVectorService pgVectorService) {
        this.embeddingModel = embeddingModel;
        this.pgVectorService = pgVectorService;
    }
    public String processPdfFiles(List<Path> filePaths) throws IOException {
        PDFTextStripper pdfStripper = new PDFTextStripper();
        long sequenceNumber = pgVectorService.getLatestId() + 1;

        for (Path filePath : filePaths) {
            processSinglePdfFile(filePath, pdfStripper, sequenceNumber);
        }

        return "Successfully processed PDF files";
    }

    public List<Path> getFilesFromFolder(String folderPath) throws IOException {
        Path path = Paths.get(folderPath);
        if (!Files.exists(path)) {
            throw new IOException("Directory not found: " + folderPath);
        }

        return Files.list(path)
                .filter(Files::isRegularFile)
                .collect(Collectors.toList());
    }

    private void processSinglePdfFile(Path filePath, PDFTextStripper pdfStripper, long sequenceNumber) throws IOException {
        try (PDDocument document = PDDocument.load(filePath.toFile())) {
            int numberOfPages = document.getNumberOfPages();
            for (int i = 0; i < numberOfPages; i++) {
                processSinglePage(document, pdfStripper, i, sequenceNumber);
                sequenceNumber++;
            }
        }
    }

    private void processSinglePage(PDDocument document, PDFTextStripper pdfStripper, int pageIndex, long sequenceNumber) throws IOException {
        pdfStripper.setStartPage(pageIndex + 1);
        pdfStripper.setEndPage(pageIndex + 1);
        String text = pdfStripper.getText(document);

        for (int j = 0; j < text.length(); j += MAX_CHARS) {
            String chunk = text.substring(j, Math.min(j + MAX_CHARS, text.length()));
            storeTextChunksInPostgres(chunk, sequenceNumber);
        }

        System.out.println("Processed page " + pageIndex + " of document");
    }

    private void storeTextChunksInPostgres(String chunk, long sequenceNumber) throws IOException {
        float[] vector = embeddingModel.embed(chunk);
        pgVectorService.insertRecord(sequenceNumber, chunk, vector);
    }
}