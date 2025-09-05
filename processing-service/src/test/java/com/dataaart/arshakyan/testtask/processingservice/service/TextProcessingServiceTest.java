package com.dataaart.arshakyan.testtask.processingservice.service;

import com.dataaart.arshakyan.testtask.processingservice.dto.ProcessingResult;
import com.dataaart.arshakyan.testtask.processingservice.kafka.KafkaProducerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TextProcessingServiceTest {

    private RestTemplate restTemplate;
    private KafkaProducerService kafkaProducerService;
    private TextProcessingService service;

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);
        kafkaProducerService = mock(KafkaProducerService.class);
        service = new TextProcessingService(restTemplate, kafkaProducerService);
    }

    @Test
    void process_multipleParagraphs_shouldComputeCorrectStats() throws ExecutionException, InterruptedException {
        // Given
        String[] paragraph1 = {"Man bun asymmetrical Man tofu mlkshk. Man bun coffee chillwave."};
        String[] paragraph2 = {"Man bun is great. Coffee coffee coffee man bun."};
        when(restTemplate.getForObject(any(String.class), eq(String[].class)))
                .thenReturn(paragraph1)
                .thenReturn(paragraph2);

        // When
        ProcessingResult result = service.process(2);

        // Then
        assertThat(result).isNotNull();

        assertThat(result.mostFrequentWord()).isEqualTo("man");
        assertThat(result.avgParagraphSize()).isNotBlank();
        assertThat(result.avgProcessingTime()).endsWith("ms");
        assertThat(result.totalProcessingTime()).endsWith("ms");
        ArgumentCaptor<ProcessingResult> captor = ArgumentCaptor.forClass(ProcessingResult.class);
        verify(kafkaProducerService).sendMessage(captor.capture());
        assertThat(captor.getValue().mostFrequentWord()).isEqualTo("man");
    }

    @Test
    void process_singleParagraph_shouldWork() throws ExecutionException, InterruptedException {
        // Given
        String[] paragraph = {"Coffee coffee coffee origin. Coffee rules!"};
        when(restTemplate.getForObject(any(String.class), eq(String[].class)))
                .thenReturn(paragraph);

        // When
        ProcessingResult result = service.process(1);

        // Then
        assertThat(result.mostFrequentWord()).isEqualTo("coffee");
        verify(kafkaProducerService).sendMessage(any());
    }

    @Test
    void process_shouldReturnNAWhenNoWords() throws ExecutionException, InterruptedException {
        // Given
        String[] paragraph = {""};
        when(restTemplate.getForObject(any(String.class), eq(String[].class)))
                .thenReturn(paragraph);

        // When
        ProcessingResult result = service.process(1);

        // Then
        assertThat(result.mostFrequentWord()).isEqualTo("N/A");
    }

    @Test
    void process_shouldHandlePunctuationAndCase() throws ExecutionException, InterruptedException {
        // Given
        String[] paragraph = {"Hello, HELLO! hello-world test."};
        when(restTemplate.getForObject(any(String.class), eq(String[].class)))
                .thenReturn(paragraph);

        // When
        ProcessingResult result = service.process(1);

        // Then
        assertThat(result.mostFrequentWord()).isEqualTo("hello");
    }

    @Test
    void process_multipleParagraphs_fixedProcessingTime() throws ExecutionException, InterruptedException {
        // Given
        TextProcessingService spyService = Mockito.spy(service);

        TextProcessingService.ParagraphAnalysis p1 =
                new TextProcessingService.ParagraphAnalysis(2_000_000L, "apple apple banana");
        TextProcessingService.ParagraphAnalysis p2 =
                new TextProcessingService.ParagraphAnalysis(4_000_000L, "banana apple fruit");

        doReturn(p1, p2).when(spyService).fetchAndAnalyzeParagraph();

        // When
        ProcessingResult result = spyService.process(2);

        // Then
        assertThat(result.mostFrequentWord()).isEqualTo("apple");
        int size1 = p1.text().length();
        int size2 = p2.text().length();
        double expectedAvgSize = (size1 + size2) / 2.0;
        assertThat(Double.parseDouble(result.avgParagraphSize()))
                .isEqualTo(expectedAvgSize);

        assertThat(result.avgProcessingTime()).isEqualTo("3.000ms");

        ArgumentCaptor<ProcessingResult> captor = ArgumentCaptor.forClass(ProcessingResult.class);
        verify(kafkaProducerService).sendMessage(captor.capture());
        assertThat(captor.getValue().mostFrequentWord()).isEqualTo("apple");
    }

    @Test
    void process_singleParagraph_fixedSizeAndTime() throws ExecutionException, InterruptedException {
        TextProcessingService spyService = Mockito.spy(service);

        TextProcessingService.ParagraphAnalysis p1 =
                new TextProcessingService.ParagraphAnalysis(5_000_000L, "coffee coffee latte");

        doReturn(p1).when(spyService).fetchAndAnalyzeParagraph();

        ProcessingResult result = spyService.process(1);

        assertThat(result.mostFrequentWord()).isEqualTo("coffee");
        assertThat(Double.parseDouble(result.avgParagraphSize()))
                .isEqualTo(p1.text().length());
        assertThat(result.avgProcessingTime()).isEqualTo("5.000ms");
    }

    @Test
    void process_shouldReturnNAWhenEmpty() throws ExecutionException, InterruptedException {
        TextProcessingService spyService = Mockito.spy(service);

        TextProcessingService.ParagraphAnalysis p1 =
                new TextProcessingService.ParagraphAnalysis(1_000_000L, "");

        doReturn(p1).when(spyService).fetchAndAnalyzeParagraph();

        ProcessingResult result = spyService.process(1);

        assertThat(result.mostFrequentWord()).isEqualTo("N/A");
        assertThat(result.avgParagraphSize()).isEqualTo("0.00");
    }
}
