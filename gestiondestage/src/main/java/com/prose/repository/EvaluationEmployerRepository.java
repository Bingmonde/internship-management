package com.prose.repository;

import com.prose.entity.EvaluationEmployer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EvaluationEmployerRepository extends JpaRepository<EvaluationEmployer, Long> {

}
