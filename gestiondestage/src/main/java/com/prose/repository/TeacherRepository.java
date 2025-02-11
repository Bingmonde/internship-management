package com.prose.repository;


import com.prose.entity.users.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    @Query("""
        select u from Teacher u where trim(lower(u.credentials.email)) = :email
    """)
    Optional<Teacher> findUserAppByEmail(@Param("email") String email);
}
