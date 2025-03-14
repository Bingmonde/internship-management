package com.prose.repository;

import com.prose.entity.users.UserApp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserAppRepository extends JpaRepository<UserApp, Long> {
    @Query("""
        select u from UserApp u where trim(lower(u.credentials.email)) = :email
    """)
    Optional<UserApp> findUserAppByEmail(@Param("email") String email);
}
