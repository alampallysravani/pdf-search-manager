package com.pdfapp.pdfapp.service;

import com.pdfapp.pdfapp.model.Document;
import com.pdfapp.pdfapp.model.User;
import com.pdfapp.pdfapp.repository.DocumentRepository;
import com.pdfapp.pdfapp.repository.UserRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;

    @Value("${storage.base-folder:uploads}")
    private String baseFolder;

    public DocumentService(DocumentRepository documentRepository, UserRepository userRepository) {
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
    }

    public Document uploadDocument(MultipartFile file, Long ownerId) throws IOException {
        User user = userRepository.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("Owner not found"));

        Path pdfDir = Paths.get(baseFolder, "pdfs");
        Path textDir = Paths.get(baseFolder, "text");
        Files.createDirectories(pdfDir);
        Files.createDirectories(textDir);

        Path pdfPath = pdfDir.resolve(file.getOriginalFilename());
        Files.write(pdfPath, file.getBytes());

        String extractedText = "";
        if ("application/pdf".equals(file.getContentType())) {
            try (PDDocument pdfDoc = PDDocument.load(file.getBytes())) {
                PDFTextStripper stripper = new PDFTextStripper();
                extractedText = stripper.getText(pdfDoc);
            }
        }

        String textFileName = file.getOriginalFilename().replace(".pdf", ".txt");
        Path textFilePath = textDir.resolve(textFileName);
        Files.writeString(textFilePath, extractedText);

        Document doc = new Document();
        doc.setFilename(file.getOriginalFilename());
        doc.setMimeType(file.getContentType());
        doc.setPdfFilePath(pdfPath.toString());
        doc.setTextFilePath(textFilePath.toString());
        doc.setOwner(user);

        return documentRepository.save(doc);
    }

    public List<Document> getUserDocuments(Long ownerId) {
        User user = userRepository.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return documentRepository.findByOwner(user);
    }

    public void deleteDocument(Long documentId) throws IOException {
        Document doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        if (doc.getPdfFilePath() != null) Files.deleteIfExists(Path.of(doc.getPdfFilePath()));
        if (doc.getTextFilePath() != null) Files.deleteIfExists(Path.of(doc.getTextFilePath()));

        documentRepository.delete(doc);
    }
}
