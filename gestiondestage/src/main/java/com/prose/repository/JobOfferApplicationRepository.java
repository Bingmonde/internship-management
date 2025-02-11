package com.prose.repository;


import com.prose.entity.ApprovalStatus;
import com.prose.entity.JobOfferApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobOfferApplicationRepository extends JpaRepository<JobOfferApplication, Long> {

    @Query("""
        select j from JobOfferApplication j where j.curriculumVitae.student.id = :sid and j.jobOffer.id = :jid
    """)
    List<JobOfferApplication> findJobOfferApplicationByStudentAndOffer(@Param("sid") long studentId, @Param("jid") long jobOfferId);

    @Query("""
        select j from JobOfferApplication j where j.curriculumVitae.student.id = :sid and j.jobOffer.session.id = :ssid
    """)
    List<JobOfferApplication> findJobOfferApplicationByStudent(@Param("sid") long studentId, @Param("ssid") long sessionId);
    @Query("""
        select j from JobOfferApplication j 
        where j.jobOffer.id = :jid 
        and j.jobOffer.session.id = :sid
        and j.isActive
        and j.curriculumVitae.status = :cvstat
        and j.jobOffer.isApproved
        and concat(j.curriculumVitae.student.prenom, ' ', j.curriculumVitae.student.nom) like %:q%
    """)
    Page<JobOfferApplication> findJobOfferApplicationByJobOfferId(@Param("jid") Long offerId, @Param("sid") Long sessionId,@Param("q") String query, @Param("cvstat") ApprovalStatus status, Pageable pageable);

    @Query("""
        select j from JobOfferApplication j
        where
        j.jobOffer.titre like %:query% or
        j.jobOffer.lieu like %:query% or
        j.jobOffer.typeTravail like %:query%""")
    Page<JobOfferApplication> findAll(Pageable pageable, @Param("query") String query);



    @Query("""
    select j from JobOfferApplication j
    where j.jobOffer.id = :jid
    and j.jobOffer.session.id = :sid
    and j.isActive
    and j.curriculumVitae.status = :cvstat
    and j.jobOffer.isApproved
    and concat(j.curriculumVitae.student.prenom, ' ', j.curriculumVitae.student.nom) like %:q%
    and (
        size(j.jobInterviews) = 0 or
        not exists (
            select ji from JobInterview ji
            where ji.jobOfferApplication = j and ji.cancelledDate is not null
        )
    )
""")
    Page<JobOfferApplication> findJobOfferApplicationByJobOfferIdOnlyInterviewees(
            @Param("jid") Long offerId,
            @Param("sid") Long sessionId,
            @Param("q") String query,
            @Param("cvstat") ApprovalStatus status,
            Pageable pageable);

    @Query("""
    select j from JobOfferApplication j 
    where j.jobOffer.id = :jid 
    and j.jobOffer.session.id = :sid
    and j.isActive
    and j.curriculumVitae.status = :cvstat
    and j.jobOffer.isApproved
    and concat(j.curriculumVitae.student.prenom, ' ', j.curriculumVitae.student.nom) like %:q%
    and j.internshipOffer is not null
""")
    Page<JobOfferApplication> findJobOfferApplicationByJobOfferIdOnlyWithInternshipOffered(
            @Param("jid") Long offerId,
            @Param("sid") Long sessionId,
            @Param("q") String query,
            @Param("cvstat") ApprovalStatus status,
            Pageable pageable);





    // count how many job applications are there for a given job offer, session, and isActive true
    @Query("""
        select count(j) from JobOfferApplication j 
        where j.jobOffer.id = :id 
        and j.jobOffer.session.id = :sessionId
        and j.isActive
    """)
    long countByJobOfferId(Long id, Long sessionId);
}


