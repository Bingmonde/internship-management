package com.prose.repository;

import com.prose.entity.PDFDocu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PDFDocuRepository extends JpaRepository<PDFDocu, Long> {
    PDFDocu save(PDFDocu pdfDocu);

    Optional<PDFDocu> findByFileName(String fileName);

}
