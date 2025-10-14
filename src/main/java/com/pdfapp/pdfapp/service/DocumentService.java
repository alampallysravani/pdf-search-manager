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

    // Folder where PDFs and extracted text will be stored
    @Value("${storage.base-folder:uploads}")
    private String baseFolder;

    public DocumentService(DocumentRepository documentRepository, UserRepository userRepository) {
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
    }

    /**
     * Uploads a PDF, extracts text, saves extracted text as a file, and stores file path in DB.
     */
    public Document uploadDocument(MultipartFile file, String username) throws IOException {
        // Get user
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Create folders if not exist
        Path pdfDir = Paths.get(baseFolder, "pdfs");
        Path textDir = Paths.get(baseFolder, "text");
        Files.createDirectories(pdfDir);
        Files.createDirectories(textDir);

        // Save uploaded PDF
        Path pdfPath = pdfDir.resolve(file.getOriginalFilename());
        Files.write(pdfPath, file.getBytes());

        // Extract text from PDF
        String extractedText;
        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            PDFTextStripper stripper = new PDFTextStripper();
            extractedText = stripper.getText(document);
        }

        // Save extracted text to a .txt file
        String textFileName = file.getOriginalFilename().replace(".pdf", ".txt");
        Path textFilePath = textDir.resolve(textFileName);
        Files.writeString(textFilePath, extractedText);

        // Create and save Document entity
        Document doc = new Document();
        doc.setFilename(file.getOriginalFilename());
        doc.setMimeType(file.getContentType());
        doc.setUploadedAt(LocalDateTime.now());
        doc.setTextFilePath(textFilePath.toString());
        

        return documentRepository.save(doc);
    }

    /**
     * Returns all documents belonging to a specific user.
     */
    public List<Document> getUserDocuments(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return documentRepository.findByOwner(user);
    }

    /**
     * Searches for documents containing a specific keyword in their extracted text file.
     */
    public List<Document> searchDocuments(String username, String keyword) {
        List<Document> userDocs = getUserDocuments(username);
        return userDocs.stream()
                .filter(doc -> {
                    try {
                        String content = Files.readString(Path.of(doc.getTextFilePath()));
                        return content.toLowerCase().contains(keyword.toLowerCase());
                    } catch (IOException e) {
                        return false;
                    }
                })
                .toList();
    }

    /**
     * Deletes a document and its corresponding files.
     */
    public void deleteDocument(Long documentId) throws IOException {
        Optional<Document> optDoc = documentRepository.findById(documentId);
        if (optDoc.isPresent()) {
            Document doc = optDoc.get();
            // Delete stored files
            Files.deleteIfExists(Path.of(doc.getTextFilePath()));
            documentRepository.deleteById(documentId);
        }
    }
}
