package com.dataaart.arshakyan.testtask.repositoryservice.persistance.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
public class ProcessingResultEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String freqWord;
    private String avgParagraphSize;
    private String avgParagraphProcessingTime;
    private String totalProcessingTime;

    public ProcessingResultEntity(String freqWord, String avgParagraphSize, String avgParagraphProcessingTime,
                                  String totalProcessingTime) {
        this.freqWord = freqWord;
        this.avgParagraphSize = avgParagraphSize;
        this.avgParagraphProcessingTime = avgParagraphProcessingTime;
        this.totalProcessingTime = totalProcessingTime;
    }

}
