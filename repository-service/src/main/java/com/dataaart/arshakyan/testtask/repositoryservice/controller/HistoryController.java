package com.dataaart.arshakyan.testtask.repositoryservice.controller;

import com.dataaart.arshakyan.testtask.repositoryservice.persistance.entity.ProcessingResultEntity;
import com.dataaart.arshakyan.testtask.repositoryservice.persistance.repository.ProcessingResultRepository;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/betvictor")
public class HistoryController {

    private final ProcessingResultRepository repository;

    public HistoryController(ProcessingResultRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/history")
    public ResponseEntity<List<ProcessingResultEntity>> getHistory() {
        return ResponseEntity.ok(repository.findTop10ByOrderByIdDesc());
    }

}
