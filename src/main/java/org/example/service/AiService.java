package org.example.service;

import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.web.client.WebClient;
import org.example.dto.AiAnalysisResult;
import org.example.entity.KycRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AiService {

    private final WebClient webClient;
    private final String apiKey;
    private final String model;
    public AiService(Vertx vertx, String apiKey, String model) {
        this.webClient = WebClient.create(vertx);
        this.apiKey = apiKey;
        this.model = model;
    }
    // Simulates an AI analysis (Use this for assignment submission if no API Key)
    public Single<AiAnalysisResult> analyzeDocument(KycRecord record) {
        // 1. Construct the Prompt
        String promptText = String.format(
                "Role: You are a generic KYC Verification Assistant.\n" +
                        "Task: Analyze the uploaded KYC document metadata and return confidence-based insights without making final decisions.\n\n" +

                        "--- Context ---\n" +
                        "User Role: %s\n" + // [Req: User Role]
                        "Document Type: %s\n" + // [Req: Document Type]
                        "Extracted Metadata: Name='%s', ID Number='%s'\n\n" + // [Req: Metadata]

                        "--- Instructions ---\n" +
                        "1. Validate Plausibility: Does the ID format match standard regex for this document type?\n" +
                        "2. Highlight Risks: Flag issues like 'Test Data', 'Mismatched Name', or 'Suspicious Number sequences'.\n" +
                        "3. Avoid Decisions: Do NOT say 'Approved' or 'Rejected'. Only provide a confidence score and flags.\n\n" +

                        "--- Output Format (JSON Only) ---\n" +
                        "{ \"status\": \"AI_CLEAR\" (if >80 confidence) or \"AI_FLAGGED\", " +
                        "\"confidenceScore\": (0-100), " +
                        "\"recommendation\": \"AUTO_APPROVE\" or \"MANUAL_REVIEW\", " +
                        "\"riskFlags\": [\"List\", \"Of\", \"Risks\"] }",

                record.getUser().getRole(), // Passed Role
                record.getGovtIdType(),     // Passed Doc Type
                record.getUser().getFullName(),
                record.getGovtIdNumber()
        );

        JsonObject payload = new JsonObject()
                .put("model", model)
                .put("messages", new JsonArray().add(new JsonObject().put("role", "user").put("content", promptText)));

        // 2. Call OpenRouter / OpenAI API
        return webClient.postAbs("https://openrouter.ai/api/v1/chat/completions")
                .putHeader("Authorization", "Bearer " + apiKey)
                .putHeader("Content-Type", "application/json")
                .rxSendJsonObject(payload)
                .map(response -> {
                    if (response.statusCode() != 200) {
                        throw new RuntimeException("AI API Error: " + response.statusMessage());
                    }

                    // 3. Parse Response
                    JsonObject body = response.bodyAsJsonObject();
                    String aiContent = body.getJsonArray("choices").getJsonObject(0).getJsonObject("message").getString("content");

                    // Clean markdown code blocks if present
                    if (aiContent.contains("```json")) {
                        aiContent = aiContent.replace("```json", "").replace("```", "");
                    }

                    return new JsonObject(aiContent).mapTo(AiAnalysisResult.class);
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