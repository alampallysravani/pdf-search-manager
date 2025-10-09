package com.pdfapp.pdfapp.dto;

import com.pdfapp.pdfapp.model.Document;
import java.util.Date;
import java.text.SimpleDateFormat;

public class DocumentDTO {
    private Long id;
    private String filename;
    private String mimeType;
    private Long ownerId;
    private Date uploadedAt;
    private String uploadedAtFormatted; // new field

    public DocumentDTO() {}

    public DocumentDTO(Document d) {
        if (d != null) {
            this.id = d.getId();
            this.filename = d.getFilename();
            this.mimeType = d.getMimeType();
            this.uploadedAt = d.getUploadedAt();
            if (d.getOwner() != null) this.ownerId = d.getOwner().getId();

            // format uploadedAt as string
            if (this.uploadedAt != null) {
                this.uploadedAtFormatted = new SimpleDateFormat("dd-MM-yyyy HH:mm").format(this.uploadedAt);
            } else {
                this.uploadedAtFormatted = "";
            }
        }
    }

    // getters & setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }

    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }

    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }

    public Date getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(Date uploadedAt) { this.uploadedAt = uploadedAt; }

    public String getUploadedAtFormatted() { return uploadedAtFormatted; }
    public void setUploadedAtFormatted(String uploadedAtFormatted) { this.uploadedAtFormatted = uploadedAtFormatted; }
}
