package com.prose.repository;

import com.prose.entity.JobPermission;
import com.prose.entity.users.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobPermissionRepository extends JpaRepository<JobPermission, Long> {

    @Query("""
        select u from JobPermission u where u.jobOffer.id = :id
    """)
    Optional<JobPermission> findByJobOfferId(long id);

    @Query("""
        select u from JobPermission u
        where u.jobOffer.session.id = :sid
        and u.disciplines = :s_discipline
        and ((u.students is empty) or (:student MEMBER OF u.students))
        and u.jobOffer.isApproved
        and (trim(lower(u.jobOffer.titre)) like %:query% or trim(lower(u.jobOffer.lieu)) like %:query% or trim(lower(u.jobOffer.typeTravail)) like %:query% or trim(lower(cast(u.jobOffer.tauxHoraire as string))) like %:query%)
    """)
    Page<JobPermission> findBySessionIdAndSearch(@Param("sid") Long sessionId, @Param("s_discipline") String discipline, @Param("student") Student student, @Param("query") String query, Pageable pageable);
}
