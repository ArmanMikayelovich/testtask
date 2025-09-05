package com.dataaart.arshakyan.testtask.repositoryservice.kafka;

import com.dataaart.arshakyan.testtask.repositoryservice.dto.ProcessingResult;
import com.dataaart.arshakyan.testtask.repositoryservice.persistance.entity.ProcessingResultEntity;
import com.dataaart.arshakyan.testtask.repositoryservice.persistance.repository.ProcessingResultRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class KafkaConsumerServiceTest {

    private ProcessingResultRepository repository;
    private ObjectMapper objectMapper;
    private KafkaConsumerService service;

    @BeforeEach
    void setUp() {
        repository = mock(ProcessingResultRepository.class);
        objectMapper = mock(ObjectMapper.class);
        service = new KafkaConsumerService(repository, objectMapper);
    }

    @Test
    void consume_shouldDeserializeAndSaveEntity() throws Exception {
        // Given
        String payload = "{\"freq_word\":\"apple\",\"avg_paragraph_size\":\"10.00\",\"avg_paragraph_processing_time\":\"1.000ms\",\"total_processing_time\":\"5ms\"}";
        ProcessingResult dto = new ProcessingResult("apple", "10.00", "1.000ms", "5ms");

        when(objectMapper.readValue(anyString(), eq(ProcessingResult.class))).thenReturn(dto);

        // WHen
        service.consume(payload);

        // Then
        ArgumentCaptor<ProcessingResultEntity> captor = ArgumentCaptor.forClass(ProcessingResultEntity.class);
        verify(repository).save(captor.capture());

        ProcessingResultEntity saved = captor.getValue();
        assertThat(saved.getFreqWord()).isEqualTo("apple");
        assertThat(saved.getAvgParagraphSize()).isEqualTo("10.00");
        assertThat(saved.getAvgParagraphProcessingTime()).isEqualTo("1.000ms");
        assertThat(saved.getTotalProcessingTime()).isEqualTo("5ms");
    }

    @Test
    void consume_shouldLogErrorOnDeserializationFailure() throws Exception {
        when(objectMapper.readValue(anyString(), eq(ProcessingResult.class))).thenThrow(new RuntimeException("boom"));

        service.consume("invalid json");

        verify(repository, never()).save(any());
    }
}