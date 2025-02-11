package com.prose.service.dto;

import com.prose.entity.Session;

import java.util.List;

public record SessionDTO(Long id,
                         String season,
                         String year,
                         String startDate,
                         String endDate) {

    public static SessionDTO toDTO(Session session) {
        if(session.getJobOffers() == null) {
            return new SessionDTO(
                session.getId(),
                session.getSeason(),
                session.getYear(),
                session.getStartDate().toString(),
                session.getEndDate().toString()
            );
        }
        return new SessionDTO(
            session.getId(),
            session.getSeason(),
            session.getYear(),
            session.getStartDate().toString(),
            session.getEndDate().toString()
        );
    }
}
