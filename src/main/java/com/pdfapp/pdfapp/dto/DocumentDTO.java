package com.pdfapp.pdfapp.dto;

import com.pdfapp.pdfapp.model.Document;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

public class DocumentDTO {

    private Long id;
    private String filename;
    private String mimeType;
    private String uploadedAtFormatted;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    public DocumentDTO() {}

    public DocumentDTO(Document document) {
        if (document != null) {
            this.id = document.getId();
            this.filename = document.getFilename();
            this.mimeType = document.getMimeType();

            LocalDateTime uploadedAt = document.getUploadedAt();
            this.uploadedAtFormatted = uploadedAt != null ? uploadedAt.format(FORMATTER) : "";
        }
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }

    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }

    public String getUploadedAtFormatted() { return uploadedAtFormatted; }
    public void setUploadedAtFormatted(String uploadedAtFormatted) { this.uploadedAtFormatted = uploadedAtFormatted; }
}
