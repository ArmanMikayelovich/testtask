package com.dataaart.arshakyan.testtask.repositoryservice.controller;

import com.dataaart.arshakyan.testtask.repositoryservice.persistance.entity.ProcessingResultEntity;
import com.dataaart.arshakyan.testtask.repositoryservice.persistance.repository.ProcessingResultRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HistoryController.class)
class HistoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProcessingResultRepository repository;

    @Test
    void getHistory_shouldReturnTop10Results() throws Exception {
        List<ProcessingResultEntity> mockResults = List.of(
                new ProcessingResultEntity("apple", "10.00", "1.000ms", "5ms")
        );

        when(repository.findTop10ByOrderByIdDesc()).thenReturn(mockResults);

        mockMvc.perform(get("/betvictor/history"))
               .andExpect(status().isOk())
               .andExpect(content().contentType(MediaType.APPLICATION_JSON))
               .andExpect(jsonPath("$[0].freqWord").value("apple"))
               .andExpect(jsonPath("$[0].avgParagraphSize").value("10.00"))
               .andExpect(jsonPath("$[0].avgParagraphProcessingTime").value("1.000ms"))
               .andExpect(jsonPath("$[0].totalProcessingTime").value("5ms"));

        Mockito.verify(repository).findTop10ByOrderByIdDesc();
    }
}