package com.prose.repository;

import com.prose.entity.ApprovalStatus;
import com.prose.entity.InternshipOffer;
import com.prose.entity.JobOffer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.prose.service.dto.InternshipOfferDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InternshipOfferRepository extends JpaRepository<InternshipOffer, Long> {

    int countByJobOfferApplication_JobOfferAndConfirmationStatusIn(JobOffer jobOffer, List<ApprovalStatus> waiting);

    @Query("""
        select io from InternshipOffer io
        join io.jobOfferApplication joa
        join joa.curriculumVitae cv
        join cv.student s
        where s.id = :stuid
        and io.jobOfferApplication.jobOffer.session.id = :sid
        and (lower(joa.jobOffer.titre) like %:query% or lower(joa.jobOffer.lieu) like %:query% or lower(joa.jobOffer.typeTravail) like %:query%)
    """)
    Page<InternshipOffer> findInternshipOffersByStudentId(@Param("stuid") Long stuid, @Param("sid") Long sessionId, @Param("query") String query, Pageable pageable);

    @Query("""
        select io from InternshipOffer io
        where io.confirmationStatus = com.prose.entity.ApprovalStatus.ACCEPTED
        and io.jobOfferApplication.jobOffer.session.id = :sesid
        order by io.confirmationDate desc
   """)
    Page<InternshipOffer> findInternshipOfferWithOrAwaitingContract(@Param("sesid") long sessionId, Pageable pageable);

    @Query("""
        select io from InternshipOffer io
        where io.contract is not null
        and io.confirmationStatus = com.prose.entity.ApprovalStatus.ACCEPTED
        and io.jobOfferApplication.curriculumVitae.student.id = :sid
        and io.jobOfferApplication.jobOffer.session.id = :sesid
        order by io.confirmationDate desc
   """)
    Page<InternshipOffer> findInternshipOfferWithContractStudent(@Param("sid") long studentId, @Param("sesid") long sessionId, Pageable pageable);

    @Query("""
        select io from InternshipOffer io
        where io.contract is not null
        and io.confirmationStatus = com.prose.entity.ApprovalStatus.ACCEPTED
        and io.jobOfferApplication.jobOffer.employeur.id = :eid
        and io.jobOfferApplication.jobOffer.session.id = :sesid
        order by io.confirmationDate desc
   """)
    Page<InternshipOffer> findInternshipOfferWithContractEmployer(@Param("eid") long employerId, @Param("sesid") long sessionId, Pageable pageable);

    @Query("""
        select io from InternshipOffer io
        where io.confirmationStatus = com.prose.entity.ApprovalStatus.ACCEPTED
        and io.jobOfferApplication.jobOffer.session.id = :sesid
        order by io.confirmationDate desc
   """)
    List<InternshipOffer> findInternshipOfferWithOrAwaitingContract(@Param("sesid") long sessionId);

    @Query("""
        select io from InternshipOffer io
        where io.contract is not null
        and io.confirmationStatus = com.prose.entity.ApprovalStatus.ACCEPTED
        and io.jobOfferApplication.curriculumVitae.student.id = :sid
        and io.jobOfferApplication.jobOffer.session.id = :sesid
        order by io.confirmationDate desc
   """)
    List<InternshipOffer> findInternshipOfferWithContractStudent(@Param("sid") long studentId, @Param("sesid") long sessionId);

    @Query("""
        select io from InternshipOffer io
        where io.contract is not null
        and io.confirmationStatus = com.prose.entity.ApprovalStatus.ACCEPTED
        and io.jobOfferApplication.jobOffer.employeur.id = :eid
        and io.jobOfferApplication.jobOffer.session.id = :sesid
        order by io.confirmationDate desc
   """)
    List<InternshipOffer> findInternshipOfferWithContractEmployer(@Param("eid") long employerId, @Param("sesid") long sessionId);
    @Query("""
        select io from InternshipOffer io
        left join io.contract c
        where c.employerSign is not null
        and c.studentSign is not null
        and c.managerSign is not null
        and io.internshipEvaluation is null
        order by c.managerSign desc """)
    List<InternshipOffer> findInternshipOfferWithContractAllSigned();
    @Query("""
        select io from InternshipOffer io
        where io.contract is not null
        and io.confirmationStatus = com.prose.entity.ApprovalStatus.ACCEPTED
        and io.jobOfferApplication.curriculumVitae.teacher.id = :tid
        order by io.confirmationDate desc
    """)
    List<InternshipOffer> findInternshipOfferWithContractTeacher(@Param("tid") long teacherId);

    @Query("""
    select io from InternshipOffer io
    where io.jobOfferApplication.jobOffer.employeur.id = :employerId
""")
    List<InternshipOffer> findByEmployerId(@Param("employerId") long employerId);

    Optional<InternshipOffer> getInternshipOfferById(Long id);

    Optional<InternshipOffer> getInternshipOfferByJobOfferApplicationId(Long jobOfferApplicationId);

    @Query("""
        select io from InternshipOffer io
        where io.jobOfferApplication.jobOffer.id = :jobOfferId
        and io.jobOfferApplication.jobOffer.session.id = :sessionId
    """)
    List<InternshipOffer> getInternshipOffersByJobOfferIdAndSessionId(@Param("jobOfferId") long jobOfferId, @Param("sessionId") long sessionId);

    @Query("""
    SELECT io FROM InternshipOffer io
    WHERE io.confirmationStatus = :status 
    AND io.jobOfferApplication.jobOffer.session.id = :sessionId
    AND (lower(io.jobOfferApplication.jobOffer.titre) like %:query% or 
    lower(io.jobOfferApplication.jobOffer.lieu) like %:query% or 
    lower(io.jobOfferApplication.jobOffer.typeTravail) like %:query%)
    """)
    Page<InternshipOffer> findInternshipOfferByStatus(@Param("status") ApprovalStatus status, Pageable pageable,
                                                      @Param("query") String query,
                                                      @Param("sessionId") Long sessionId);

    @Query("""
    select io from InternshipOffer io
    where io.jobOfferApplication.jobOffer.titre like %:query% or\s
    io.jobOfferApplication.jobOffer.lieu like %:query% or\s
    io.jobOfferApplication.jobOffer.typeTravail like %:query%""")
    Page<InternshipOffer> findAll(Pageable pageable, @Param("query") String query);

}
