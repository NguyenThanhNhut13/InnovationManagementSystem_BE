package vn.edu.iuh.fit.innovationmanagementsystem_be.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.model.Innovation;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.requestDTO.CheckSimilarityRequest;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.CheckSimilarityResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO.SimilarInnovationResponse;
import vn.edu.iuh.fit.innovationmanagementsystem_be.repository.InnovationRepository;
import vn.edu.iuh.fit.innovationmanagementsystem_be.service.similarity.InnovationTextExtractor;
import vn.edu.iuh.fit.innovationmanagementsystem_be.service.similarity.TFIDFCalculator;

import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class InnovationSimilarityService {

    private final InnovationRepository innovationRepository;
    private final TFIDFCalculator tfidfCalculator;
    private final InnovationTextExtractor textExtractor;

    @Value("${innovation.similarity.threshold:0.5}")
    private double similarityThreshold;

    @Value("${innovation.similarity.max-results:10}")
    private int maxResults;

    public InnovationSimilarityService(
            InnovationRepository innovationRepository,
            TFIDFCalculator tfidfCalculator,
            InnovationTextExtractor textExtractor) {
        this.innovationRepository = innovationRepository;
        this.tfidfCalculator = tfidfCalculator;
        this.textExtractor = textExtractor;
    }

    public CheckSimilarityResponse checkSimilarity(CheckSimilarityRequest request) {
        log.info("Checking similarity for innovation: {}", request.getInnovationName());
        long startTime = System.currentTimeMillis();

        String newInnovationText = extractTextFromRequest(request);
        if (newInnovationText == null || newInnovationText.trim().isEmpty()) {
            log.warn("Empty innovation text, returning no similar results");
            return buildEmptyResponse();
        }

        List<String> newTokens = textExtractor.tokenize(newInnovationText);
        if (newTokens.isEmpty()) {
            log.warn("No tokens extracted from innovation text");
            return buildEmptyResponse();
        }

        List<Innovation> existingInnovations = innovationRepository.findAllWithDetails();
        if (existingInnovations.isEmpty()) {
            log.info("No existing innovations found");
            return buildEmptyResponse();
        }

        List<InnovationDocument> documents = prepareDocuments(existingInnovations);
        List<SimilarInnovationResponse> similarResults = calculateSimilarities(
                newTokens, documents);

        long elapsed = System.currentTimeMillis() - startTime;
        log.info("Similarity check completed in {}ms, found {} similar innovations",
                elapsed, similarResults.size());

        return CheckSimilarityResponse.builder()
                .hasSimilar(!similarResults.isEmpty())
                .totalFound(similarResults.size())
                .threshold(similarityThreshold)
                .similarInnovations(similarResults)
                .build();
    }

    private String extractTextFromRequest(CheckSimilarityRequest request) {
        StringBuilder text = new StringBuilder();

        if (request.getInnovationName() != null) {
            text.append(request.getInnovationName()).append(" ");
        }

        if (request.getTemplates() != null) {
            for (CheckSimilarityRequest.TemplateFormData template : request.getTemplates()) {
                if (template.getFormData() != null) {
                    for (Object value : template.getFormData().values()) {
                        String valueText = extractValueAsText(value);
                        if (valueText != null && !valueText.trim().isEmpty()) {
                            text.append(valueText).append(" ");
                        }
                    }
                }
            }
        }

        return text.toString().trim();
    }

    private String extractValueAsText(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof String) {
            return org.jsoup.Jsoup.parse((String) value).text();
        }

        if (value instanceof Number || value instanceof Boolean) {
            return value.toString();
        }

        if (value instanceof List) {
            StringBuilder listText = new StringBuilder();
            for (Object item : (List<?>) value) {
                String itemText = extractValueAsText(item);
                if (itemText != null) {
                    listText.append(itemText).append(" ");
                }
            }
            return listText.toString().trim();
        }

        if (value instanceof Map) {
            StringBuilder mapText = new StringBuilder();
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) value;
            for (Object v : map.values()) {
                String vText = extractValueAsText(v);
                if (vText != null) {
                    mapText.append(vText).append(" ");
                }
            }
            return mapText.toString().trim();
        }

        return value.toString();
    }

    private List<InnovationDocument> prepareDocuments(List<Innovation> innovations) {
        List<InnovationDocument> documents = new ArrayList<>();

        for (Innovation innovation : innovations) {
            // Fetch formData separately to avoid PostgreSQL JSON DISTINCT error
            Innovation innovationWithFormData = innovationRepository.findByIdWithFormData(innovation.getId());
            if (innovationWithFormData != null) {
                // Copy formDataList to original innovation
                innovation.setFormDataList(innovationWithFormData.getFormDataList());
            }

            String text = textExtractor.extractText(innovation);
            List<String> tokens = textExtractor.tokenize(text);
            if (!tokens.isEmpty()) {
                documents.add(new InnovationDocument(innovation, tokens));
            }
        }

        return documents;
    }

    private List<SimilarInnovationResponse> calculateSimilarities(
            List<String> newTokens,
            List<InnovationDocument> documents) {

        List<List<String>> allDocTokens = new ArrayList<>();
        allDocTokens.add(newTokens);
        for (InnovationDocument doc : documents) {
            allDocTokens.add(doc.tokens);
        }

        Map<String, Double> idf = tfidfCalculator.calculateIDF(allDocTokens);
        List<String> vocabulary = tfidfCalculator.buildVocabulary(allDocTokens);

        Map<String, Double> newTfidf = tfidfCalculator.calculateTFIDF(newTokens, idf);
        double[] newVector = tfidfCalculator.toVector(newTfidf, vocabulary);

        List<ScoredInnovation> scoredList = new ArrayList<>();
        DecimalFormat df = new DecimalFormat("#.##");

        for (InnovationDocument doc : documents) {
            Map<String, Double> docTfidf = tfidfCalculator.calculateTFIDF(doc.tokens, idf);
            double[] docVector = tfidfCalculator.toVector(docTfidf, vocabulary);
            double similarity = tfidfCalculator.cosineSimilarity(newVector, docVector);

            if (similarity >= similarityThreshold) {
                scoredList.add(new ScoredInnovation(doc.innovation, similarity));
            }
        }

        scoredList.sort((a, b) -> Double.compare(b.score, a.score));

        return scoredList.stream()
                .limit(maxResults)
                .map(scored -> {
                    Innovation inn = scored.innovation;
                    return SimilarInnovationResponse.builder()
                            .innovationId(inn.getId())
                            .innovationName(inn.getInnovationName())
                            .similarityScore(scored.score)
                            .similarityPercentage(df.format(scored.score * 100) + "%")
                            .departmentName(
                                    inn.getDepartment() != null ? inn.getDepartment().getDepartmentName() : null)
                            .authorName(inn.getUser() != null ? inn.getUser().getFullName() : null)
                            .innovationRoundName(
                                    inn.getInnovationRound() != null ? inn.getInnovationRound().getName() : null)
                            .status(inn.getStatus())
                            .createdAt(inn.getCreatedAt())
                            .build();
                })
                .collect(Collectors.toList());
    }

    private CheckSimilarityResponse buildEmptyResponse() {
        return CheckSimilarityResponse.builder()
                .hasSimilar(false)
                .totalFound(0)
                .threshold(similarityThreshold)
                .similarInnovations(Collections.emptyList())
                .build();
    }

    private static class InnovationDocument {
        final Innovation innovation;
        final List<String> tokens;

        InnovationDocument(Innovation innovation, List<String> tokens) {
            this.innovation = innovation;
            this.tokens = tokens;
        }
    }

    private static class ScoredInnovation {
        final Innovation innovation;
        final double score;

        ScoredInnovation(Innovation innovation, double score) {
            this.innovation = innovation;
            this.score = score;
        }
    }
}
