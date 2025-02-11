package com.prose.repository;

import com.prose.entity.JobInterview;
import com.prose.entity.users.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface JobInterviewRepository extends JpaRepository<JobInterview, Long> {

    @Query("""
        select i from JobInterview i 
        where i.jobOfferApplication.jobOffer.employeur.id = :eid
        and i.jobOfferApplication.jobOffer.session.id = :sid
        and i.interviewDate between :sdate AND :edate
    """)
    Page<JobInterview> findJobInterviewByEmployerId(@Param("eid") Long employerId, @Param("sid") Long sessionId, @Param("sdate") LocalDateTime startDate, @Param("edate") LocalDateTime endDate, Pageable pageable);

    @Query("""
        select i from JobInterview i 
        where i.jobOfferApplication.jobOffer.employeur.id = :eid
        and i.jobOfferApplication.jobOffer.session.id = :sid
        and i.interviewDate between :sdate AND :edate
        and i.isConfirmedByStudent = :c_stu
    """)
    Page<JobInterview> findJobInterviewByEmployerIdConfirmedByStudent(@Param("eid") Long employerId, @Param("sid") Long sessionId, @Param("sdate") LocalDateTime startDate, @Param("edate") LocalDateTime endDate, @Param("c_stu") boolean confirmedStudent, Pageable pageable);

    @Query("""
        select i from JobInterview i
        where i.jobOfferApplication.jobOffer.employeur.id = :eid
        and i.jobOfferApplication.jobOffer.session.id = :sid
        and i.cancelledDate is null
        and i.interviewDate between :sdate AND :edate
    """)
    Page<JobInterview> findJobInterviewByEmployerIdNotCancelled(@Param("eid") Long employerId, @Param("sid") Long sessionId, @Param("sdate") LocalDateTime startDate, @Param("edate") LocalDateTime endDate, Pageable pageable);

    @Query("""
        select i from JobInterview i
        where i.jobOfferApplication.jobOffer.employeur.id = :eid
        and i.jobOfferApplication.jobOffer.session.id = :sid
        and i.cancelledDate is not null
        and i.interviewDate between :sdate AND :edate
    """)
    Page<JobInterview> findJobInterviewByEmployerIdCancelled(@Param("eid") Long employerId, @Param("sid") Long sessionId, @Param("sdate") LocalDateTime startDate, @Param("edate") LocalDateTime endDate, Pageable pageable);

    @Query("""
        select i from JobInterview i
        where i.jobOfferApplication.curriculumVitae.student.id = :sid
        and i.jobOfferApplication.jobOffer.id = :jid
    """)
    List<JobInterview> findJobInterviewByStudentIdAndJobOfferId(@Param("sid") Long studentId, @Param("jid") Long jobOfferId);

    @Query("""
        select i from JobInterview i
        where i.jobOfferApplication.curriculumVitae.student.id = :eid
        and i.jobOfferApplication.jobOffer.session.id = :sid
        and i.interviewDate between :sdate AND :edate
    """)
    Page<JobInterview> findJobInterviewByStudentIdPaged(@Param("eid") Long employerId, @Param("sid") Long sessionId, @Param("sdate") LocalDateTime startDate, @Param("edate") LocalDateTime endDate, Pageable pageable);

    @Query("""
        select i from JobInterview i
        where i.jobOfferApplication.curriculumVitae.student.id = :eid
        and i.jobOfferApplication.jobOffer.session.id = :sid
        and i.interviewDate between :sdate AND :edate
        and i.isConfirmedByStudent = :c_stu
    """)
    Page<JobInterview> findJobInterviewByStudentIdConfirmedByStudent(@Param("eid") Long employerId, @Param("sid") Long sessionId, @Param("sdate") LocalDateTime startDate, @Param("edate") LocalDateTime endDate, @Param("c_stu") boolean confirmedStudent, Pageable pageable);

    @Query("""
        select i from JobInterview i
        where i.jobOfferApplication.curriculumVitae.student.id = :eid
        and i.jobOfferApplication.jobOffer.session.id = :sid
        and i.cancelledDate is null
        and i.interviewDate between :sdate AND :edate
    """)
    Page<JobInterview> findJobInterviewByStudentIdNotCancelled(@Param("eid") Long employerId, @Param("sid") Long sessionId, @Param("sdate") LocalDateTime startDate, @Param("edate") LocalDateTime endDate, Pageable pageable);

    @Query("""
        select i from JobInterview i
        where i.jobOfferApplication.curriculumVitae.student.id = :eid
        and i.jobOfferApplication.jobOffer.session.id = :sid
        and i.cancelledDate is not null
        and i.interviewDate between :sdate AND :edate
    """)
    Page<JobInterview> findJobInterviewByStudentIdCancelled(@Param("eid") Long employerId, @Param("sid") Long sessionId, @Param("sdate") LocalDateTime startDate, @Param("edate") LocalDateTime endDate, Pageable pageable);

    @Query("""
    SELECT stud
    FROM Student stud
    WHERE stud.id NOT IN (
        SELECT inter.jobOfferApplication.curriculumVitae.student.id 
        FROM JobInterview inter
        WHERE inter.jobOfferApplication.jobOffer.session.id = :sessionId
    )
    AND stud.id IN (
        SELECT jobApp.curriculumVitae.student.id
        FROM JobOfferApplication jobApp
        WHERE jobApp.jobOffer.session.id = :sessionId 
    )
    AND (
        concat(trim(lower(stud.nom)) , ' ', trim(lower(stud.prenom))) like %:query%
    )
    """)
    Page<Student> findAllStudentWithNoInterview(Pageable pageable, @Param("query") String query, @Param("sessionId") Long sessionId);

    @Query("""
    SELECT inter from JobInterview inter
    WHERE inter.isConfirmedByStudent = false AND inter.cancelledDate IS NULL
    AND inter.jobOfferApplication.jobOffer.session.id = :sessionId
    AND (
        concat(trim(lower(inter.jobOfferApplication.curriculumVitae.student.nom) ) , ' ', trim(lower(inter.jobOfferApplication.curriculumVitae.student.prenom))) like %:query%
    )
    """)
    Page<JobInterview> findAllStudentWaitingForInterview(Pageable pageable, @Param("query") String query, @Param("sessionId") Long sessionId);

    @Query("""
    SELECT inter from JobInterview inter
    WHERE inter.isConfirmedByStudent = true AND inter.cancelledDate IS NULL and inter.confirmationDate IS NOT NULL
    AND inter.jobOfferApplication.jobOffer.session.id = :sessionId
    AND inter.id NOT IN (
        SELECT inter.id 
        FROM JobInterview inter
        WHERE inter.isConfirmedByStudent = false AND inter.cancelledDate IS NULL
        AND inter.jobOfferApplication.jobOffer.session.id = :sessionId
        )
    AND inter.id IN(
        SELECT inter.id
        FROM JobInterview inter
        WHERE inter.jobOfferApplication.internshipOffer.confirmationStatus = com.prose.entity.ApprovalStatus.WAITING
    )
    AND (
        concat(trim(lower(inter.jobOfferApplication.curriculumVitae.student.nom) ) , ' ', trim(lower(inter.jobOfferApplication.curriculumVitae.student.prenom))) like %:query%
    )
    """)
    Page<JobInterview> findAllStudentWaitingForInterviewAnswer(Pageable pageable, @Param("query") String query, @Param("sessionId") Long sessionId);
}
