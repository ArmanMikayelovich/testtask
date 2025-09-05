package com.dataaart.arshakyan.testtask.processingservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ProcessingResult(
    @JsonProperty("freq_word") String mostFrequentWord,
    @JsonProperty("avg_paragraph_size") String avgParagraphSize,
    @JsonProperty("avg_paragraph_processing_time") String avgProcessingTime,
    @JsonProperty("total_processing_time") String totalProcessingTime
) {}
