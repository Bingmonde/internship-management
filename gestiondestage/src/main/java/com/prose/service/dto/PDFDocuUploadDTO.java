package com.prose.service.dto;

import com.prose.entity.PDFDocu;
import lombok.Builder;

@Builder
public record PDFDocuUploadDTO (
        String fileName

){
    public static PDFDocuUploadDTO toDTO(PDFDocu pdfDocu) {
        if (pdfDocu == null) {
            return null;
        }
        return new PDFDocuUploadDTO(pdfDocu.getFileName());
    }
}
