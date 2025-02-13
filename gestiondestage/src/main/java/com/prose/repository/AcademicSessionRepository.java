package com.prose.repository;

import com.prose.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface AcademicSessionRepository extends JpaRepository<Session, Long> {

    @Query("SELECT s FROM Session s WHERE s.year = :year AND s.season = :season")
    Optional<Session> findSessionByYearAndSeason(String year, String season);
}
