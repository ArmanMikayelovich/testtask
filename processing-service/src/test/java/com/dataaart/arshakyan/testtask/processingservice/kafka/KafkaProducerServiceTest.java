package com.dataaart.arshakyan.testtask.processingservice.kafka;

import com.dataaart.arshakyan.testtask.processingservice.dto.ProcessingResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.core.KafkaTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class KafkaProducerServiceTest {

    @Mock private KafkaTemplate<String, String> kafkaTemplate;

    private KafkaProducerService kafkaProducerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ObjectMapper objectMapper = new ObjectMapper();
        kafkaProducerService = new KafkaProducerService(kafkaTemplate, objectMapper);
    }

    @Test
    void sendMessage_shouldSerializeAndSendToKafka() throws Exception {
        // Given
        ProcessingResult result = new ProcessingResult("apple", "12.34", "3.210ms", "10ms");

        // When
        kafkaProducerService.sendMessage(result);

        // Then
        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);

        verify(kafkaTemplate).send(topicCaptor.capture(), keyCaptor.capture(), payloadCaptor.capture());

        assertThat(topicCaptor.getValue()).isEqualTo("words.processed");
        assertThat(keyCaptor.getValue()).isEqualTo("apple");

        String json = payloadCaptor.getValue();
        assertThat(json).contains("\"freq_word\":\"apple\"");
        assertThat(json).contains("\"avg_paragraph_size\":\"12.34\"");
        assertThat(json).contains("\"avg_paragraph_processing_time\":\"3.210ms\"");
    }

    @Test
    void sendMessage_shouldHandleSerializationException() throws Exception {
        // Given
        ProcessingResult result = new ProcessingResult("apple", "12.34", "3.210ms", "10ms");

        ObjectMapper mockMapper = mock(ObjectMapper.class);
        KafkaProducerService service = new KafkaProducerService(kafkaTemplate, mockMapper);

        when(mockMapper.writeValueAsString(any())).thenThrow(new RuntimeException("Serialization error"));

        // When
        service.sendMessage(result);

        // Then
        verifyNoInteractions(kafkaTemplate);
    }
}