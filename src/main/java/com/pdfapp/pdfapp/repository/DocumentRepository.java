package com.pdfapp.pdfapp.repository;

import com.pdfapp.pdfapp.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface DocumentRepository extends JpaRepository<Document, Long> {

	@Query("SELECT d FROM Document d WHERE d.filename LIKE %:keyword% OR d.extractedText LIKE %:keyword%")
	List<Document> searchByText(@Param("keyword") String keyword);

}

