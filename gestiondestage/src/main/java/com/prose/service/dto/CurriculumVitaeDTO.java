package com.prose.service.dto;

import com.prose.entity.CurriculumVitae;
import lombok.Builder;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;

public record CurriculumVitaeDTO (
        Long id,
        PDFDocuUploadDTO pdfDocu,
        LocalDateTime dateHeureAjout,
        String status,
        StudentDTO studentDTO
) {
    @Builder
    public CurriculumVitaeDTO {
    }

    public static CurriculumVitaeDTO toDTO(CurriculumVitae curriculumVitae) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

        return CurriculumVitaeDTO.builder()
                .id(curriculumVitae.getId())
                .pdfDocu(PDFDocuUploadDTO.toDTO(curriculumVitae.getPdfDocu()))
                .dateHeureAjout(curriculumVitae.getDateHeureAjout())
                .status(curriculumVitae.getStatus().toString())
                .studentDTO(StudentDTO.toDTO(curriculumVitae.getStudent()))
                .build();
    }
}
