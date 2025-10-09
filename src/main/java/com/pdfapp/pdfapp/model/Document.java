package com.pdfapp.pdfapp.model;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "documents")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    private String filename;
    private String mimeType;

    @Lob
    private byte[] pdfBlob;

    @Lob
    @Column(columnDefinition = "CLOB")
    private String extractedText;

    @Temporal(TemporalType.TIMESTAMP)
    private Date uploadedAt ;

    public Document() {}

    // getters & setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }

    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }

    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }

    public byte[] getPdfBlob() { return pdfBlob; }
    public void setPdfBlob(byte[] pdfBlob) { this.pdfBlob = pdfBlob; }

    public String getExtractedText() { return extractedText; }
    public void setExtractedText(String extractedText) { this.extractedText = extractedText; }

    public Date getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(Date uploadedAt) { this.uploadedAt = uploadedAt; }
}
