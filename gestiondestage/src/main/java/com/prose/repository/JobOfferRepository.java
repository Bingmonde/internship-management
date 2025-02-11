package com.prose.repository;

import com.prose.entity.JobOffer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobOfferRepository extends JpaRepository<JobOffer, Long> {

    @Query("""
        select j from JobOffer j where j.employeur.id = :employeurId and j.session.id = :sessionId
    """)
    List<JobOffer> findByEmployeurId(Long employeurId, Long sessionId);

    @Query("""
        select j from JobOffer j where j.isApproved = false and j.session.id = :sessionId""")
    List<JobOffer> findByIsApprovedFalse(@Param("sessionId") Long sessionId);

    @Query("""
        select j from JobOffer j where j.isApproved = true and j.session.id = :sessionId
        AND (
        trim(lower(j.titre)) like %:query% or\s
        trim(lower(j.lieu)) like %:query% or\s
        trim(lower(j.typeTravail)) like %:query%
        )
    """)
    Page<JobOffer> findByIsApprovedTrue(Pageable pageable, @Param("query") String query, @Param("sessionId") Long sessionId);

    @Query("""
        select j from JobOffer j where j.isApproved = false and j.session.id = :sessionId
        AND 
        (trim(lower(j.titre)) like %:query% or 
        trim(lower(j.lieu)) like %:query% or 
        trim(lower(j.typeTravail)) like %:query% )
        """)
    Page<JobOffer> findByIsApprovedFalsePage(Pageable pageable, @Param("query") String query, @Param("sessionId") Long sessionId);

    @Query("""
        select case when exists (select 1 from JobOffer j where j.isApproved = false and j.session.id = :sessionId) then true else false end
    """)
    boolean notApprovedInSession(Long sessionId);

}
