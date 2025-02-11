package com.prose.service.dto;

import com.prose.entity.Discipline;
import com.prose.entity.JobPermission;
import lombok.Builder;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Builder
public record JobPermissionDTO (
        Long id,
        List<Discipline> disciplines,
        List<StudentDTO> students,
        LocalDate expirationDate,
        boolean isApproved

){
    public static JobPermissionDTO toDTO(JobPermission jobPermission) {
        String discipline = jobPermission.getDisciplines();
        List<Discipline> disciplinesList = Arrays.asList();
        if(discipline != null && !discipline.isEmpty()){
            disciplinesList = Arrays.stream(jobPermission.getDisciplines().split(";")).map(Discipline::toEnum).toList();
        }
        return JobPermissionDTO.builder()
                .id(jobPermission.getId())
                .disciplines(disciplinesList)
                .students(jobPermission.getStudents().stream().map(StudentDTO::toDTO).toList())
                .expirationDate(jobPermission.getExpirationDate())
                .isApproved(jobPermission.getJobOffer().isApproved())
                .build();
    }
}