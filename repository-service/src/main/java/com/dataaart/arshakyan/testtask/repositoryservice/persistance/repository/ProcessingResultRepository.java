package com.dataaart.arshakyan.testtask.repositoryservice.persistance.repository;

import com.dataaart.arshakyan.testtask.repositoryservice.persistance.entity.ProcessingResultEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcessingResultRepository extends JpaRepository<ProcessingResultEntity, Long> {

    List<ProcessingResultEntity> findTop10ByOrderByIdDesc();

}