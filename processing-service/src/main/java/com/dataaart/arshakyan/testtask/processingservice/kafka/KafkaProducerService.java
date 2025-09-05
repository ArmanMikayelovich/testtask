package com.dataaart.arshakyan.testtask.processingservice.kafka;

import com.dataaart.arshakyan.testtask.processingservice.dto.ProcessingResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private static final String TOPIC_NAME = "words.processed";

    public void sendMessage(ProcessingResult result) {
        try {
            log.info("Sending message to Kafka topic '{}': {}", TOPIC_NAME, result);
            String messagePayload = objectMapper.writeValueAsString(result);
            kafkaTemplate.send(TOPIC_NAME, result.mostFrequentWord(), messagePayload);
        } catch (Exception e) {
            log.error("Error sending message to Kafka", e);
        }
    }
}