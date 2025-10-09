package com.pdfapp.pdfapp.controller;

import com.pdfapp.pdfapp.dto.DocumentDTO;
import com.pdfapp.pdfapp.model.Document;
import com.pdfapp.pdfapp.repository.DocumentRepository;
import com.pdfapp.pdfapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private UserRepository userRepository;

    // ✅ Upload document (PDF or DOCX) with text extraction
    @PostMapping("/upload")
    public ResponseEntity<DocumentDTO> upload(@RequestParam("file") MultipartFile file,
                                              @RequestParam(value = "ownerId", required = false) Long ownerId) throws IOException {
        if (file.isEmpty()) return ResponseEntity.badRequest().build();

        Document doc = new Document();
        doc.setFilename(file.getOriginalFilename());
        doc.setMimeType(file.getContentType() != null ? file.getContentType() : "application/pdf");
        doc.setPdfBlob(file.getBytes());

        String extractedText = "";

        try {
            // PDF text extraction
            if ("application/pdf".equals(doc.getMimeType())) {
                try (PDDocument pdfDoc = PDDocument.load(file.getBytes())) {
                    PDFTextStripper stripper = new PDFTextStripper();
                    extractedText = stripper.getText(pdfDoc);
                }
            }
            // DOCX text extraction
            else if ("application/vnd.openxmlformats-officedocument.wordprocessingml.document".equals(doc.getMimeType())) {
                try (XWPFDocument docx = new XWPFDocument(new ByteArrayInputStream(file.getBytes()))) {
                    StringBuilder sb = new StringBuilder();
                    for (XWPFParagraph para : docx.getParagraphs()) {
                        sb.append(para.getText()).append("\n");
                    }
                    extractedText = sb.toString();
                }
            }
        } catch (Exception e) {
            System.out.println("Text extraction failed: " + e.getMessage());
            extractedText = "";
        }

        doc.setExtractedText(extractedText);

        // Set owner if provided
        if (ownerId != null) {
            userRepository.findById(ownerId).ifPresent(doc::setOwner);
        }

        documentRepository.save(doc);
        return ResponseEntity.ok(new DocumentDTO(doc));
    }

    // ✅ List all documents
    @GetMapping
    public List<DocumentDTO> listAll() {
        return documentRepository.findAll().stream().map(DocumentDTO::new).collect(Collectors.toList());
    }

    // ✅ Download document
    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> download(@PathVariable Long id) {
        return documentRepository.findById(id)
                .map(doc -> {
                    byte[] data = doc.getPdfBlob();
                    String filename = doc.getFilename() != null ? doc.getFilename() : "file.pdf";
                    String mimeType = doc.getMimeType() != null ? doc.getMimeType() : "application/pdf";

                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.parseMediaType(mimeType));
                    headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());

                    return new ResponseEntity<>(data, headers, HttpStatus.OK);
                })
                .orElseGet(() -> new ResponseEntity<>(new byte[0], HttpStatus.NOT_FOUND));
    }

    // ✅ Search by filename or extracted content
    @GetMapping("/search")
    public List<DocumentDTO> searchDocuments(@RequestParam String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return List.of();
        }
        List<Document> results = documentRepository.searchByText(keyword.trim());
        return results.stream().map(DocumentDTO::new).collect(Collectors.toList());
    }

    // ✅ Delete document
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        if (documentRepository.existsById(id)) {
            documentRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
