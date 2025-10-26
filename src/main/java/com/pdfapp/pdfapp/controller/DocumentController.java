package com.pdfapp.pdfapp.controller;

import com.pdfapp.pdfapp.dto.DocumentDTO;
import com.pdfapp.pdfapp.model.Document;
import com.pdfapp.pdfapp.model.User;
import com.pdfapp.pdfapp.repository.DocumentRepository;
import com.pdfapp.pdfapp.repository.UserRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final String PDF_FOLDER = "uploaded_pdfs";
    private final String TEXT_FOLDER = "extracted_texts";

    public DocumentController(DocumentRepository documentRepository, UserRepository userRepository) {
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;

        try {
            Files.createDirectories(Paths.get(System.getProperty("user.dir"), PDF_FOLDER));
            Files.createDirectories(Paths.get(System.getProperty("user.dir"), TEXT_FOLDER));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Upload PDF/DOCX — store file & extracted text in folders
    @PostMapping("/upload")
    public ResponseEntity<DocumentDTO> upload(@RequestParam("file") MultipartFile file,
                                              @RequestParam(value = "ownerId", required = false) Long ownerId) throws IOException {
        if (file.isEmpty()) return ResponseEntity.badRequest().build();

        Path pdfPath = Paths.get(System.getProperty("user.dir"), PDF_FOLDER, file.getOriginalFilename());
        Files.write(pdfPath, file.getBytes());

        String extractedText = "";
        if ("application/pdf".equals(file.getContentType())) {
            try (PDDocument pdfDoc = PDDocument.load(file.getBytes())) {
                PDFTextStripper stripper = new PDFTextStripper();
                extractedText = stripper.getText(pdfDoc);
            }
        } else if ("application/vnd.openxmlformats-officedocument.wordprocessingml.document".equals(file.getContentType())) {
            try (XWPFDocument docx = new XWPFDocument(new ByteArrayInputStream(file.getBytes()))) {
                StringBuilder sb = new StringBuilder();
                for (XWPFParagraph para : docx.getParagraphs()) {
                    sb.append(para.getText()).append("\n");
                }
                extractedText = sb.toString();
            }
        }

        Path textFilePath = Paths.get(System.getProperty("user.dir"), TEXT_FOLDER, file.getOriginalFilename() + ".txt");
        Files.writeString(textFilePath, extractedText);

        Document doc = new Document();
        doc.setFilename(file.getOriginalFilename());
        doc.setMimeType(file.getContentType());
        doc.setPdfFilePath(pdfPath.toAbsolutePath().toString());
        doc.setTextFilePath(textFilePath.toAbsolutePath().toString());

        if (ownerId != null && ownerId > 0) {
            userRepository.findById(ownerId).ifPresent(doc::setOwner);
        }

        documentRepository.save(doc);
        return ResponseEntity.ok(new DocumentDTO(doc));
    }

    // List all uploaded documents
    @GetMapping
    public List<DocumentDTO> listAll() {
        return documentRepository.findAll().stream()
                .map(DocumentDTO::new)
                .collect(Collectors.toList());
    }

    // Download extracted text file
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        Document doc = documentRepository.findById(id).orElse(null);
        if (doc == null) return ResponseEntity.notFound().build();

        Path path = Paths.get(doc.getTextFilePath());
        if (!Files.exists(path)) return ResponseEntity.notFound().build();

        try {
            ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(path));
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + path.getFileName().toString() + "\"")
                    .contentType(MediaType.TEXT_PLAIN)
                    .contentLength(Files.size(path))
                    .body(resource);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Search PDFs by keyword in extracted text files (all files)
    @GetMapping("/search")
    public List<DocumentDTO> searchDocuments(@RequestParam String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) return List.of();

        String searchKey = keyword.toLowerCase();
        return documentRepository.findAll().stream()
                .filter(doc -> {
                    try {
                        String content = Files.readString(Paths.get(doc.getTextFilePath()));
                        return content.toLowerCase().contains(searchKey);
                    } catch (IOException e) {
                        return false;
                    }
                })
                .map(DocumentDTO::new)
                .collect(Collectors.toList());
    }

    // ✅ Search inside a specific file by ID
    @GetMapping("/{id}/search")
    public ResponseEntity<List<String>> searchInFile(@PathVariable Long id,
                                                     @RequestParam String keyword) {
        Document doc = documentRepository.findById(id).orElse(null);
        if (doc == null) return ResponseEntity.notFound().build();

        Path path = Paths.get(doc.getTextFilePath());
        if (!Files.exists(path)) return ResponseEntity.notFound().build();

        try {
            List<String> lines = Files.readAllLines(path);
            String lowerKeyword = keyword.toLowerCase();
            List<String> results = new ArrayList<>();
            for (String line : lines) {
                if (line.toLowerCase().contains(lowerKeyword)) {
                    results.add(line);
                }
            }
            return ResponseEntity.ok(results);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Delete document + both files
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        Optional<Document> optionalDoc = documentRepository.findById(id);
        if (optionalDoc.isEmpty()) return ResponseEntity.notFound().build();

        Document doc = optionalDoc.get();

        try {
            if (doc.getPdfFilePath() != null) Files.deleteIfExists(Paths.get(doc.getPdfFilePath()));
            if (doc.getTextFilePath() != null) Files.deleteIfExists(Paths.get(doc.getTextFilePath()));
            documentRepository.delete(doc);
            return ResponseEntity.noContent().build();
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
