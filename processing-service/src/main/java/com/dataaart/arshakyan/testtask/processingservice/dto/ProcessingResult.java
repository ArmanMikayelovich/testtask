package com.dataaart.arshakyan.testtask.processingservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ProcessingResult(
    @JsonProperty("freq_word") String freq_word,
    @JsonProperty("avg_paragraph_size") String avg_paragraph_size,
    @JsonProperty("avg_paragraph_processing_time") String avg_paragraph_processing_time,
    @JsonProperty("total_processing_time") String total_processing_time
) {}
