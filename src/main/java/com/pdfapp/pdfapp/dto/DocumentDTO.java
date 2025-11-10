package com.pdfapp.pdfapp.dto;

import com.pdfapp.pdfapp.model.Document;
import java.time.format.DateTimeFormatter;

public class DocumentDTO {
    private Long id;
    private String filename;
    private String mimeType;
    private String ownerName;
    private Long ownerId;
    private String uploadedAt;

    public DocumentDTO(Document doc) {
        this.id = doc.getId();
        this.filename = doc.getFilename();
        this.mimeType = doc.getMimeType();
        this.ownerId = (doc.getOwner() != null) ? doc.getOwner().getId() : null;
        this.ownerName = (doc.getOwner() != null) ? doc.getOwner().getUsername() : "N/A";
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        this.uploadedAt = doc.getUploadedAt() != null ? doc.getUploadedAt().format(fmt) : "N/A";
    }

    public Long getId() { return id; }
    public String getFilename() { return filename; }
    public String getMimeType() { return mimeType; }
    public String getOwnerName() { return ownerName; }
    public Long getOwnerId() { return ownerId; }
    public String getUploadedAt() { return uploadedAt; }
}
