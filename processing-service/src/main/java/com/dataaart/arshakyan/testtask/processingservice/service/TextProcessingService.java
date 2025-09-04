package com.dataaart.arshakyan.testtask.processingservice.service;

import com.dataaart.arshakyan.testtask.processingservice.dto.ProcessingResult;
import com.dataaart.arshakyan.testtask.processingservice.kafka.KafkaProducerService;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class TextProcessingService {

    private final RestTemplate restTemplate;
    private final KafkaProducerService kafkaProducerService;
    private static final String HIPSUM_API_URL = "https://hipsum.co/api/?type=hipster-centric&paras=1";

    private record ParagraphAnalysis(long processingTimeNanos, String text) {}

    public TextProcessingService(RestTemplate restTemplate, KafkaProducerService kafkaProducerService) {
        this.restTemplate = restTemplate;
        this.kafkaProducerService = kafkaProducerService;
    }

    public ProcessingResult process(int paragraphCount) throws InterruptedException, ExecutionException {
        Instant totalStartTime = Instant.now();
        List<Future<ParagraphAnalysis>> futures;

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Callable<ParagraphAnalysis>> tasks = IntStream.range(0, paragraphCount)
                .mapToObj(i -> (Callable<ParagraphAnalysis>) this::fetchAndAnalyzeParagraph)
                .toList();
            futures = executor.invokeAll(tasks);
        }

        List<ParagraphAnalysis> results = new ArrayList<>();
        for (Future<ParagraphAnalysis> future : futures) {
            results.add(future.get());
        }

        String mostFrequentWord = findMostFrequentWord(results);
        double avgParagraphSize = calculateAverageParagraphSize(results);
        double avgProcessingTime = calculateAverageProcessingTime(results);

        long totalProcessingTime = Duration.between(totalStartTime, Instant.now()).toMillis();

        ProcessingResult finalResult = new ProcessingResult(
            mostFrequentWord,
            String.format("%.2f", avgParagraphSize),
            String.format("%.3fms", avgProcessingTime),
            String.format("%dms", totalProcessingTime)
        );

        kafkaProducerService.sendMessage(finalResult);
        return finalResult;
    }

    private ParagraphAnalysis fetchAndAnalyzeParagraph() {
        Instant paraStartTime = Instant.now();
        String text = Objects.requireNonNull(restTemplate.getForObject(HIPSUM_API_URL, String[].class))[0];
        Instant paraEndTime = Instant.now();
        return new ParagraphAnalysis(Duration.between(paraStartTime, paraEndTime).toNanos(), text);
    }

    private String findMostFrequentWord(List<ParagraphAnalysis> results) {
        return results.stream()
            .flatMap(result -> Arrays.stream(result.text().toLowerCase().replaceAll("[^a-zA-Z ]", "").split("\\s+")))
            .filter(word -> !word.isEmpty())
            .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
            .entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("N/A");
    }

    private double calculateAverageParagraphSize(List<ParagraphAnalysis> results) {
        return results.stream()
            .mapToInt(result -> result.text().length())
            .average()
            .orElse(0.0);
    }

    private double calculateAverageProcessingTime(List<ParagraphAnalysis> results) {
        return results.stream()
                   .mapToLong(ParagraphAnalysis::processingTimeNanos)
                   .average()
                   .orElse(0.0) / 1_000_000.0; // Convert nanoseconds to milliseconds
    }

}