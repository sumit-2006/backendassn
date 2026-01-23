package org.example.service;

import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import org.example.dto.AiAnalysisResult;
import org.example.entity.KycRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AiService {

    // Simulates an AI analysis (Use this for assignment submission if no API Key)
    public Single<AiAnalysisResult> analyzeDocument(KycRecord record) {
        return Single.fromCallable(() -> {
            // Simulate network delay
            Thread.sleep(1000);

            AiAnalysisResult result = new AiAnalysisResult();
            List<String> risks = new ArrayList<>();
            Random rand = new Random();
            int score = 60 + rand.nextInt(40); // Random score 60-100

            // Simple Logic: If ID number is "12345", flag it (Example rule)
            if (record.getGovtIdNumber().equals("12345")) {
                score = 30;
                risks.add("INVALID_FORMAT");
                risks.add("SUSPICIOUS_NUMBER");
            }

            // Logic: Mismatch check (Simulated)
            // In a real Vision AI, we would extract name from image and compare
            if (score < 80) {
                result.setStatus("AI_FLAGGED");
                result.setRecommendation("MANUAL_REVIEW");
                if (risks.isEmpty()) risks.add("LOW_CONFIDENCE");
            } else {
                result.setStatus("AI_CLEAR");
                result.setRecommendation("AUTO_APPROVE");
            }

            result.setConfidenceScore(score);
            result.setRiskFlags(risks);

            System.out.println("🤖 AI Analysis Complete for User " + record.getUser().getId());
            return result;
        });
    }

    /* // --- REAL IMPLEMENTATION (For Bonus/Prod) ---
    // You would use Vert.x WebClient to call OpenRouter/OpenAI here.
    private WebClient webClient; // Inject this in constructor

    public Single<AiAnalysisResult> callOpenRouter(KycRecord record) {
        JsonObject prompt = new JsonObject()
            .put("model", "mistralai/mistral-7b-instruct")
            .put("messages", new JsonArray().add(new JsonObject()
                .put("role", "user")
                .put("content", "Analyze this KYC data: Name=" + record.getUser().getFullName() + "...")));

        return webClient.postAbs("https://openrouter.ai/api/v1/chat/completions")
            .putHeader("Authorization", "Bearer YOUR_KEY")
            .rxSendJsonObject(prompt)
            .map(resp -> {
                // Parse JSON response to AiAnalysisResult
                return new AiAnalysisResult();
            });
    }
    */
}