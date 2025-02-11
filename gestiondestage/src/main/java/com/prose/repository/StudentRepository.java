package com.prose.repository;


import com.prose.entity.Discipline;
import com.prose.entity.Session;
import com.prose.entity.users.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    @Query("""
        select u from Student u where trim(lower(u.credentials.email)) = :email
    """)
    Optional<Student> findUserAppByEmail(@Param("email") String email);

    @Query("""
        select u from Student u where u.discipline = :discipline and trim(lower(concat(u.prenom, u.nom))) like %:query%
""")
    Page<Student> findByDiscipline(@Param("discipline") Discipline discipline, @Param("query") String query, Pageable pageable);

    List<Student> findByDiscipline(Discipline discipline);

    @Query("""
        SELECT jobApp.curriculumVitae.student
        FROM JobOfferApplication jobApp
        WHERE jobApp.jobOffer.session.id = :sessionId 
        AND (concat(trim(lower(jobApp.curriculumVitae.student.prenom)) , ' ', trim(lower(jobApp.curriculumVitae.student.nom))) like %:query%)
    """)
    Page<Student> findAll(Pageable pageable, @Param("query") String query, @Param("sessionId") Long sessionId);
}
