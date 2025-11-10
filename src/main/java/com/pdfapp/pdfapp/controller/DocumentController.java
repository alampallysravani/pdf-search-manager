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
import org.springframework.security.access.prepost.PreAuthorize;
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
    private final Path pdfDir = Paths.get("uploaded_pdfs");
    private final Path textDir = Paths.get("extracted_texts");

    public DocumentController(DocumentRepository documentRepository, UserRepository userRepository) {
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;

        try {
            Files.createDirectories(pdfDir);
            Files.createDirectories(textDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ✅ Upload (Admin only)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file,
                                        @RequestParam(value = "ownerId", required = false) Long ownerId) {
        if (file.isEmpty()) return ResponseEntity.badRequest().body("No file selected");

        try {
            String filename = file.getOriginalFilename();
            Path pdfPath = pdfDir.resolve(filename);
            Files.write(pdfPath, file.getBytes());

            // Extract text
            String extractedText = extractText(file);
            Path textPath = textDir.resolve(filename + ".txt");
            Files.writeString(textPath, extractedText);

            // Save metadata
            Document doc = new Document();
            doc.setFilename(filename);
            doc.setMimeType(file.getContentType());
            doc.setPdfFilePath(pdfPath.toString());
            doc.setTextFilePath(textPath.toString());

            if (ownerId != null) {
                userRepository.findById(ownerId).ifPresent(doc::setOwner);
            }

            documentRepository.save(doc);
            return ResponseEntity.ok(new DocumentDTO(doc));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("File upload failed: " + e.getMessage());
        }
    }

    private String extractText(MultipartFile file) throws IOException {
        String contentType = file.getContentType();
        if (contentType == null) return "";

        if (contentType.contains("pdf")) {
            try (PDDocument pdfDoc = PDDocument.load(file.getBytes())) {
                return new PDFTextStripper().getText(pdfDoc);
            }
        } else if (contentType.contains("word")) {
            try (XWPFDocument docx = new XWPFDocument(file.getInputStream())) {
                StringBuilder sb = new StringBuilder();
                for (XWPFParagraph p : docx.getParagraphs()) {
                    sb.append(p.getText()).append("\n");
                }
                return sb.toString();
            }
        }
        return "";
    }

    // ✅ Show all docs for both Admin & Users
    @GetMapping
    public List<DocumentDTO> getDocuments(@RequestParam(value = "ownerId", required = false) Long ownerId) {
        List<Document> docs = documentRepository.findAll();

        return docs.stream()
                .map(DocumentDTO::new)
                .sorted(Comparator.comparing(DocumentDTO::getUploadedAt, Comparator.nullsLast(String::compareTo)).reversed())
                .collect(Collectors.toList());
    }

    // ✅ Open PDF/DOCX file inline
    @GetMapping("/{id}/file")
    public ResponseEntity<Resource> openFile(@PathVariable Long id) throws IOException {
        Document doc = documentRepository.findById(id).orElse(null);
        if (doc == null) return ResponseEntity.notFound().build();

        Path path = Paths.get(doc.getPdfFilePath());
        if (!Files.exists(path)) return ResponseEntity.notFound().build();

        ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(path));
        MediaType type = doc.getMimeType().toLowerCase().contains("word")
                ? MediaType.APPLICATION_OCTET_STREAM
                : MediaType.APPLICATION_PDF;

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + path.getFileName() + "\"")
                .contentType(type)
                .contentLength(Files.size(path))
                .body(resource);
    }

    // ✅ Download extracted text
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadText(@PathVariable Long id) throws IOException {
        Document doc = documentRepository.findById(id).orElse(null);
        if (doc == null) return ResponseEntity.notFound().build();

        Path path = Paths.get(doc.getTextFilePath());
        if (!Files.exists(path)) return ResponseEntity.notFound().build();

        ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(path));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + path.getFileName() + "\"")
                .contentType(MediaType.TEXT_PLAIN)
                .contentLength(Files.size(path))
                .body(resource);
    }

    // ✅ Delete (Admin only)
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteDocument(@PathVariable Long id) {
        Optional<Document> optionalDoc = documentRepository.findById(id);
        if (optionalDoc.isEmpty()) return ResponseEntity.status(404).body("Document not found");

        Document doc = optionalDoc.get();
        try {
            if (doc.getPdfFilePath() != null) Files.deleteIfExists(Paths.get(doc.getPdfFilePath()));
            if (doc.getTextFilePath() != null) Files.deleteIfExists(Paths.get(doc.getTextFilePath()));
            documentRepository.delete(doc);
            return ResponseEntity.ok("Document deleted successfully");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Failed to delete files");
        }
    }

    // ✅ Search globally across files
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

    // ✅ Search inside one document
    @GetMapping("/{id}/search")
    public ResponseEntity<List<String>> searchInFile(@PathVariable Long id,
                                                     @RequestParam String keyword) throws IOException {
        Document doc = documentRepository.findById(id).orElse(null);
        if (doc == null) return ResponseEntity.notFound().build();

        Path path = Paths.get(doc.getTextFilePath());
        if (!Files.exists(path)) return ResponseEntity.notFound().build();

        List<String> lines = Files.readAllLines(path);
        List<String> results = lines.stream()
                .filter(line -> line.toLowerCase().contains(keyword.toLowerCase()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(results);
    }
}
