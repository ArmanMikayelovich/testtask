package com.dataaart.arshakyan.testtask.repositoryservice.kafka;

import com.dataaart.arshakyan.testtask.repositoryservice.dto.ProcessingResult;
import com.dataaart.arshakyan.testtask.repositoryservice.persistance.entity.ProcessingResultEntity;
import com.dataaart.arshakyan.testtask.repositoryservice.persistance.repository.ProcessingResultRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumerService {

    private final ProcessingResultRepository repository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "words.processed", groupId = "history-group")
    public void consume(String messagePayload) {
        log.info("Consumed message payload: {}", messagePayload);
        try {
            ProcessingResult message = objectMapper.readValue(messagePayload, ProcessingResult.class);

            ProcessingResultEntity entity = new ProcessingResultEntity(
                message.mostFrequentWord(),
                message.avgParagraphSize(),
                message.avgProcessingTime(),
                message.totalProcessingTime()
            );
            repository.save(entity);
        } catch (Exception e) {
            log.error("Error deserializing or processing message", e);
        }
    }

}