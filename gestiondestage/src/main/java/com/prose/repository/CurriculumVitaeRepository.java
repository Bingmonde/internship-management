package com.prose.repository;

import com.prose.entity.ApprovalStatus;
import com.prose.entity.CurriculumVitae;
import com.prose.entity.users.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CurriculumVitaeRepository extends JpaRepository<CurriculumVitae, Long> {

    List<CurriculumVitae> findByStudentId(Long studentId);
    Page<CurriculumVitae> findByStudentIdOrderByDateHeureAjoutDesc(Long studentId, Pageable pageable);

    List<CurriculumVitae> findByStudentIdAndStatus(Long studentId, ApprovalStatus approvalStatus);

    @Query("""
        select cv from CurriculumVitae cv
        where cv.status = :status""")
    List<CurriculumVitae> findByStatus(@Param("status") ApprovalStatus approvalStatus);

    boolean existsCurriculumVitaeByStudentIdAndStatusIs(Long student_id, ApprovalStatus status);

    @Query("""
        select cv from CurriculumVitae cv
        where concat(cv.student.nom, ' ', cv.student.prenom) like %:query%""")
    Page<CurriculumVitae> findAll(Pageable pageable, String query);

    @Query("""
    SELECT stud
    FROM Student stud
    WHERE stud.id NOT IN (
        SELECT curriculumVitae.student.id
        FROM CurriculumVitae curriculumVitae
    )
    AND CONCAT(TRIM(LOWER(stud.nom)), ' ', TRIM(LOWER(stud.prenom))) LIKE %:query%
    """)
    Page<Student> getAllStudentsWithNoCV(Pageable pageable, @Param("query") String query);
    @Query("""
        SELECT curriculumVitae
        FROM CurriculumVitae curriculumVitae
        WHERE curriculumVitae NOT IN (
            select cv from CurriculumVitae cv
            where cv.status = :status
        )
        AND (
            concat(trim(lower(curriculumVitae.student.nom)),' ',trim(lower(curriculumVitae.student.prenom))) like %:query%
        )
    """)
    Page<CurriculumVitae> findByNotStatusPage(@Param("status") ApprovalStatus approvalStatus, Pageable pageable, @Param("query") String query);

   /*
   * @Query("""
    SELECT jobApp.curriculumVitae
    FROM JobOfferApplication jobApp
    WHERE jobApp.curriculumVitae.status IN :statuses
    AND jobApp.jobOffer.session.id = :sessionId
    AND CONCAT(TRIM(LOWER(jobApp.curriculumVitae.student.nom)), ' ', TRIM(LOWER(jobApp.curriculumVitae.student.prenom))) LIKE %:query%
""")
    Page<CurriculumVitae> findByStatusInAndSessionPage(
            @Param("statuses") List<ApprovalStatus> statuses,
            Pageable pageable,
            @Param("query") String query,
            @Param("sessionId") Long sessionId
    );*/
}
