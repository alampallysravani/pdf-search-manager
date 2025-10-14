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
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final String TEXT_FOLDER = "extracted_texts";

    public DocumentController(DocumentRepository documentRepository, UserRepository userRepository) {
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;

        // Ensure folder exists
        File folder = new File(TEXT_FOLDER);
        if (!folder.exists()) folder.mkdirs();
    }

    // ✅ Upload PDF/DOCX and extract text to folder
    @PostMapping("/upload")
    public ResponseEntity<DocumentDTO> upload(@RequestParam("file") MultipartFile file,
                                              @RequestParam(value = "ownerId", required = false) Long ownerId) throws IOException {
        if (file.isEmpty()) return ResponseEntity.badRequest().build();

        String extractedText = "";

        // Extract text from PDF or DOCX
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

        // Save text to file if needed
        String textFileName = file.getOriginalFilename() + ".txt";
        Path textFilePath = Paths.get(TEXT_FOLDER, textFileName);
        Files.writeString(textFilePath, extractedText);

        // ✅ Create Document and save extracted text
        Document doc = new Document();
        doc.setFilename(file.getOriginalFilename());
        doc.setMimeType(file.getContentType());
        doc.setExtractedText(extractedText); // ✅ save extracted text
        doc.setTextFilePath(textFilePath.toAbsolutePath().toString());

        if (ownerId != null && ownerId > 0) {
            userRepository.findById(ownerId).ifPresent(doc::setOwner);
        }

        documentRepository.save(doc);
        return ResponseEntity.ok(new DocumentDTO(doc));
    }


    // ✅ List all documents
    @GetMapping
    public List<DocumentDTO> listAll() {
        return documentRepository.findAll().stream()
                .map(DocumentDTO::new)
                .collect(Collectors.toList());
    }

    // ✅ Download extracted text file
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        Document doc = documentRepository.findById(id).orElse(null);
        if (doc == null) return ResponseEntity.notFound().build();

        Path path = Paths.get(doc.getTextFilePath());
        if (!Files.exists(path)) return ResponseEntity.notFound().build();

        try {
            ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(path));
            HttpHeaders headers = new HttpHeaders();
            headers.setContentDisposition(
                    ContentDisposition.attachment().filename(path.getFileName().toString()).build());
            headers.setContentType(MediaType.TEXT_PLAIN);

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(Files.size(path))
                    .body(resource);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).<Resource>build();
        }
    }

    // ✅ Search by filename
    @GetMapping("/search")
    public List<DocumentDTO> searchDocuments(@RequestParam String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) return List.of();

        return documentRepository.searchByText(keyword.trim())
                .stream()
                .map(DocumentDTO::new)
                .collect(Collectors.toList());
    }

    // ✅ Delete document + extracted text file
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        Document doc = documentRepository.findById(id).orElse(null);
        if (doc == null) return ResponseEntity.notFound().build();

        try {
            if (doc.getTextFilePath() != null) Files.deleteIfExists(Paths.get(doc.getTextFilePath()));
            documentRepository.delete(doc);
            return ResponseEntity.noContent().build();
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).<Void>build();
        }
    }
}
