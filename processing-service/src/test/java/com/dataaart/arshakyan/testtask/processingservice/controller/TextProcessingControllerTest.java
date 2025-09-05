package com.dataaart.arshakyan.testtask.processingservice.controller;

import com.dataaart.arshakyan.testtask.processingservice.dto.ProcessingResult;
import com.dataaart.arshakyan.testtask.processingservice.service.TextProcessingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.concurrent.ExecutionException;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TextProcessingController.class)
class TextProcessingControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private TextProcessingService textProcessingService;

    @Test
    void processText_validParameter_shouldReturnResult() throws Exception {
        // Given
        ProcessingResult mockResult = new ProcessingResult("apple", "10.00", "1.000ms", "5ms");
        when(textProcessingService.process(eq(2))).thenReturn(mockResult);

        // When
        mockMvc
                .perform(get("/betvictor/text").param("p", "2"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.freq_word").value("apple"))
                .andExpect(jsonPath("$.avg_paragraph_size").value("10.00"))
                .andExpect(jsonPath("$.avg_paragraph_processing_time").value("1.000ms"))
                .andExpect(jsonPath("$.total_processing_time").value("5ms"));

        // Then
        verify(textProcessingService).process(2);
    }

    @Test
    void processText_invalidParameter_shouldReturnBadRequest() throws Exception {

        mockMvc.perform(get("/betvictor/text").param("p", "0")).andExpect(status().isBadRequest());
    }

    @Test
    void processText_whenServiceThrows_shouldReturnServerError() throws Exception {
        when(textProcessingService.process(eq(2))).thenThrow(new ExecutionException("failure", new RuntimeException("boom")));

        mockMvc.perform(get("/betvictor/text").param("p", "2")).andExpect(status().is5xxServerError());
    }
}