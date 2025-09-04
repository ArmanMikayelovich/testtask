package com.dataaart.arshakyan.testtask.processingservice.controller;

import com.dataaart.arshakyan.testtask.processingservice.dto.ProcessingResult;
import com.dataaart.arshakyan.testtask.processingservice.service.TextProcessingService;
import jakarta.validation.constraints.Min;
import java.util.concurrent.ExecutionException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/betvictor")
public class TextProcessingController {

    private final TextProcessingService textProcessingService;

    public TextProcessingController(TextProcessingService textProcessingService) {
        this.textProcessingService = textProcessingService;
    }

    @GetMapping("/text")
    public ResponseEntity<ProcessingResult> processText(@RequestParam("p") @Min(1) Integer paragraphs)
        throws InterruptedException, ExecutionException {

        ProcessingResult result = textProcessingService.process(paragraphs);
        return ResponseEntity.ok(result);
    }

}