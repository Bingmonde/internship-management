package com.prose.repository;

import com.prose.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AcademicSessionRepository extends JpaRepository<Session, Long> {

}
