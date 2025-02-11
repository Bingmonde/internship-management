package com.prose.repository;

import com.prose.entity.InternshipEvaluation;
import com.prose.entity.users.Teacher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InternshipEvaluationRepository extends JpaRepository<InternshipEvaluation, Long> {

    @Query("select ie from InternshipEvaluation ie " +
            "where ie.teacher.id = :profId and ie.evaluationEmployer is null " +
            "and ie.evaluationIntern is null")
    List<InternshipEvaluation> findCurrentEvaluationsByTeacherId(Long profId);

    @Query("SELECT ie FROM InternshipEvaluation ie " +
            "WHERE ie.teacher.id = :teacherId "
    )
    List<InternshipEvaluation> findByTeacherId(@Param("teacherId") long teacherId);

    @Query("SELECT ie FROM InternshipEvaluation ie WHERE ie.internshipOffer.jobOfferApplication.jobOffer.employeur.id = :employerId")
    List<InternshipEvaluation> findCurrentEvaluationsByEmployerId(@Param("employerId") long employerId);

    @Query("SELECT ie FROM InternshipEvaluation ie WHERE ie.internshipOffer.id = :internshipOfferId")
    Optional<InternshipEvaluation> findByInternshipOfferId(@Param("internshipOfferId") long internshipOfferId);

    @Query("""
    SELECT ie FROM InternshipEvaluation ie 
    WHERE ie.evaluationIntern is null
    AND ie.internshipOffer.jobOfferApplication.jobOffer.session.id = :sessionId
    AND (
         concat(trim(lower(ie.internshipOffer.jobOfferApplication.curriculumVitae.student.nom)) ,'' ,trim(lower(ie.internshipOffer.jobOfferApplication.curriculumVitae.student.prenom))) like %:query%        
    )
    """)
    Page<InternshipEvaluation> findAllStudentsNotYetEvaluatedByTeacher(@Param("query") String query, Pageable pageable, @Param("sessionId") Long sessionId);

    @Query("""
    SELECT ie FROM InternshipEvaluation ie 
    WHERE ie.evaluationEmployer is null
    AND ie.internshipOffer.jobOfferApplication.jobOffer.session.id = :sessionId
    AND (
         concat(trim(lower(ie.internshipOffer.jobOfferApplication.curriculumVitae.student.nom)) ,'' ,trim(lower(ie.internshipOffer.jobOfferApplication.curriculumVitae.student.prenom))) like %:query%        
    )
    """)
    Page<InternshipEvaluation> findAllStudentsEmployeurNotYetEvaluatedByTeacher(@Param("query") String query, Pageable pageable,
                                                                                @Param("sessionId") Long sessionId);

}
