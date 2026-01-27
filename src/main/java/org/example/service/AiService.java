package org.example.service;

import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.rxjava3.ext.web.client.WebClient;
import org.example.dto.AiAnalysisResult;
import org.example.entity.KycRecord;
import java.util.Base64;
import java.util.Collections;

public class AiService {

    private final WebClient webClient;
    private final String apiKey;
    private final String model; // ✅ Added model field
    private final Vertx vertx;

    public AiService(Vertx vertx, String apiKey, String model) {
        this.vertx = vertx;
        this.webClient = WebClient.create(vertx);
        this.apiKey = apiKey;
        this.model = model; // ✅ Store it
    }

    public Single<AiAnalysisResult> analyzeDocument(KycRecord record) {
        String filePath = "file-uploads/" + record.getDocumentPath();

        return vertx.fileSystem().rxExists(filePath)
                .flatMap(exists -> {
                    if (!exists) return Single.error(new RuntimeException("File not found"));
                    return vertx.fileSystem().rxReadFile(filePath);
                })
                .map(buffer -> Base64.getEncoder().encodeToString(buffer.getBytes()))
                .flatMap(base64Image -> {
                    String mimeType = "image/jpeg";
                    String pathLower = record.getDocumentPath().toLowerCase();

                    if (pathLower.endsWith(".png")) {
                        mimeType = "image/png";
                    } else if (pathLower.endsWith(".webp")) {
                        mimeType = "image/webp";
                    }

                    String promptText = String.format(
                            "You are a KYC Expert. VISUALLY ANALYZE THIS ID CARD IMAGE.\n" +
                                    "User Claims: Name='%s', ID='%s', Type='%s'.\n" +
                                    "Task: Check for photo mismatch, fake fonts, blurriness.\n" +
                                    "Output JSON: { \"status\": \"AI_CLEAR\"/\"AI_FLAGGED\", \"confidenceScore\": 0-100, \"recommendation\": \"...\", \"riskFlags\": [...] }",
                            record.getUser().getFullName(), record.getGovtIdNumber(), record.getGovtIdType()
                    );

                    JsonObject payload = new JsonObject()
                            .put("contents", new JsonArray().add(new JsonObject()
                                    .put("parts", new JsonArray()
                                            .add(new JsonObject().put("text", promptText))
                                            .add(new JsonObject()
                                                    .put("inline_data", new JsonObject()
                                                            .put("mime_type", "image/jpeg")
                                                            .put("data", base64Image)
                                                    )
                                            )
                                    )
                            ));

                    // ✅ DYNAMIC URL: Uses 'this.model' instead of hardcoding
                    String url = "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent?key=" + apiKey;

                    return webClient.postAbs(url)
                            .putHeader("Content-Type", "application/json")
                            .rxSendJsonObject(payload)
                            .map(response -> {
                                if (response.statusCode() != 200) {
                                    // Print detailed error for debugging
                                    System.err.println("❌ Google API Error: " + response.bodyAsString());
                                    throw new RuntimeException("Google API Error: " + response.statusMessage());
                                }

                                JsonObject body = response.bodyAsJsonObject();

                                // Safe parsing for Google's response structure
                                try {
                                    String text = body.getJsonArray("candidates").getJsonObject(0)
                                            .getJsonObject("content").getJsonArray("parts").getJsonObject(0)
                                            .getString("text");

                                    if (text.contains("```json")) text = text.replace("```json", "").replace("```", "");
                                    return new JsonObject(text).mapTo(AiAnalysisResult.class);
                                } catch (Exception e) {
                                    throw new RuntimeException("Failed to parse AI response: " + body.encode());
                                }
                            });
                })
                .onErrorReturn(err -> {
                    System.err.println("⚠️ AI Service Failed: " + err.getMessage());
                    AiAnalysisResult fallback = new AiAnalysisResult();
                    fallback.setStatus("AI_FLAGGED");
                    fallback.setRecommendation("MANUAL_REVIEW");
                    fallback.setRiskFlags(Collections.singletonList("AI_UNAVAILABLE: " + err.getMessage()));
                    return fallback;
                });
    }
}