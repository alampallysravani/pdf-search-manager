package com.pdfapp.pdfapp.repository;

import com.pdfapp.pdfapp.model.Document;
import com.pdfapp.pdfapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findByOwner(User owner);

    @Query(value = "SELECT * FROM documents d WHERE LOWER(d.filename) LIKE %:keyword% OR LOWER(d.extracted_text) LIKE %:keyword%", nativeQuery = true)
    List<Document> searchByText(@Param("keyword") String keyword);
}
