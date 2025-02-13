package com.prose.repository;

import com.prose.entity.users.ProgramManager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProgramManagerRepository extends JpaRepository<ProgramManager, Long> {
    @Query("""
        select u from ProgramManager u where trim(lower(u.credentials.email)) = :email
    """)
    Optional<ProgramManager> findUserAppByEmail(@Param("email") String email);
}
