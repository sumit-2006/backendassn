package org.example.dto;

import lombok.Data;
import java.util.List;

@Data
public class AiAnalysisResult {
    private String status; // AI_CLEAR, AI_FLAGGED
    private int confidenceScore;
    private String recommendation;
    private List<String> riskFlags;

    // Factory method for failure cases
    public static AiAnalysisResult failure() {
        AiAnalysisResult res = new AiAnalysisResult();
        res.setStatus("AI_FAILED");
        res.setConfidenceScore(0);
        res.setRecommendation("MANUAL_REVIEW");
        res.setRiskFlags(List.of("AI_SERVICE_UNAVAILABLE"));
        return res;
    }
}