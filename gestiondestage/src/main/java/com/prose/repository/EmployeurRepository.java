package com.prose.repository;

import com.prose.entity.users.Employeur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EmployeurRepository extends JpaRepository<Employeur, Long> {
    @Query("""
        select u from Employeur u where trim(lower(u.credentials.email)) = :email
    """)
    Optional<Employeur> findUserAppByEmail(@Param("email") String email);
}
