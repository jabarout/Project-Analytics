package com.projectanalytics.analytics.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.projectanalytics.analytics.api.dto.ScoreFactorResponse;
import com.projectanalytics.analytics.domain.ScoreFactor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Persists score factors as JSON so Project Detail can show explainability after reload.
 */
@Component
public class ScoreFactorSerializer {

    private static final Logger log = LoggerFactory.getLogger(ScoreFactorSerializer.class);
    private static final TypeReference<List<ScoreFactorResponse>> FACTOR_LIST =
            new TypeReference<>() {
            };

    private final ObjectMapper objectMapper;

    public ScoreFactorSerializer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String serialize(List<ScoreFactor> factors) {
        if (factors == null || factors.isEmpty()) {
            return "[]";
        }
        List<ScoreFactorResponse> dto = factors.stream()
                .map(f -> new ScoreFactorResponse(
                        f.code(),
                        f.description(),
                        f.contribution(),
                        f.rawValue()
                ))
                .toList();
        try {
            return objectMapper.writeValueAsString(dto);
        } catch (JsonProcessingException exception) {
            log.warn("Failed to serialize score factors: {}", exception.getMessage());
            return "[]";
        }
    }

    public List<ScoreFactorResponse> deserialize(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            List<ScoreFactorResponse> factors = objectMapper.readValue(json, FACTOR_LIST);
            return factors == null ? List.of() : List.copyOf(factors);
        } catch (JsonProcessingException exception) {
            log.warn("Failed to deserialize score factors: {}", exception.getMessage());
            return Collections.emptyList();
        }
    }
}
