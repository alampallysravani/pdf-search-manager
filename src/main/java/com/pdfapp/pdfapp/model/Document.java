package com.pdfapp.pdfapp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "documents")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "FILENAME")
    private String filename;

    @Column(name = "MIME_TYPE")
    private String mimeType;

    @Column(name = "PDF_FILE_PATH")
    private String pdfFilePath;

    @Column(name = "TEXT_FILE_PATH")
    private String textFilePath;

    @Column(name = "UPLOADED_AT")
    private LocalDateTime uploadedAt;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    public Document() {
        this.uploadedAt = LocalDateTime.now();
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }

    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }

    public String getPdfFilePath() { return pdfFilePath; }
    public void setPdfFilePath(String pdfFilePath) { this.pdfFilePath = pdfFilePath; }

    public String getTextFilePath() { return textFilePath; }
    public void setTextFilePath(String textFilePath) { this.textFilePath = textFilePath; }

    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
}
