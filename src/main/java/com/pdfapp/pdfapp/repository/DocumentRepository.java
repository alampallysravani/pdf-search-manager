package com.pdfapp.pdfapp.repository;

import com.pdfapp.pdfapp.model.Document;
import com.pdfapp.pdfapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    // Fetch all documents belonging to a specific user
    List<Document> findByOwner(User owner);

    // Fetch all documents by owner ID directly
    List<Document> findByOwnerId(Long ownerId);

    // ❌ Removed custom SQL query — searching is now done from text files in DocumentController
}
