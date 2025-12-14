package vn.edu.iuh.fit.innovationmanagementsystem_be.service.similarity;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class TFIDFCalculator {

    public Map<String, Double> calculateTF(List<String> words) {
        Map<String, Double> tf = new HashMap<>();
        int totalWords = words.size();

        if (totalWords == 0) {
            return tf;
        }

        Map<String, Long> wordCount = words.stream()
                .collect(Collectors.groupingBy(w -> w, Collectors.counting()));

        for (Map.Entry<String, Long> entry : wordCount.entrySet()) {
            tf.put(entry.getKey(), (double) entry.getValue() / totalWords);
        }

        return tf;
    }

    public Map<String, Double> calculateIDF(List<List<String>> documents) {
        Map<String, Double> idf = new HashMap<>();
        int totalDocs = documents.size();

        if (totalDocs == 0) {
            return idf;
        }

        Set<String> allWords = documents.stream()
                .flatMap(List::stream)
                .collect(Collectors.toSet());

        for (String word : allWords) {
            long docsContainingWord = documents.stream()
                    .filter(doc -> doc.contains(word))
                    .count();
            idf.put(word, Math.log((double) totalDocs / (1 + docsContainingWord)));
        }

        return idf;
    }

    public Map<String, Double> calculateTFIDF(List<String> document, Map<String, Double> idf) {
        Map<String, Double> tf = calculateTF(document);
        Map<String, Double> tfidf = new HashMap<>();

        for (Map.Entry<String, Double> entry : tf.entrySet()) {
            String word = entry.getKey();
            double tfValue = entry.getValue();
            double idfValue = idf.getOrDefault(word, 0.0);
            tfidf.put(word, tfValue * idfValue);
        }

        return tfidf;
    }

    public double[] toVector(Map<String, Double> tfidf, List<String> vocabulary) {
        double[] vector = new double[vocabulary.size()];
        for (int i = 0; i < vocabulary.size(); i++) {
            vector[i] = tfidf.getOrDefault(vocabulary.get(i), 0.0);
        }
        return vector;
    }

    public double cosineSimilarity(double[] vec1, double[] vec2) {
        if (vec1.length != vec2.length || vec1.length == 0) {
            return 0.0;
        }

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < vec1.length; i++) {
            dotProduct += vec1[i] * vec2[i];
            norm1 += vec1[i] * vec1[i];
            norm2 += vec2[i] * vec2[i];
        }

        if (norm1 == 0 || norm2 == 0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    public List<String> buildVocabulary(List<List<String>> documents) {
        return documents.stream()
                .flatMap(List::stream)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }
}
