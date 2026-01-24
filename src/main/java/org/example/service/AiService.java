package org.example.service;

import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
// ✅ CORRECT IMPORTS
import io.vertx.rxjava3.core.Vertx;
import io.vertx.core.buffer.Buffer;

import io.vertx.rxjava3.ext.web.client.WebClient;
import org.example.dto.AiAnalysisResult;
import org.example.entity.KycRecord;
import java.util.Base64;

public class AiService {

    private final WebClient webClient;
    private final String apiKey;
    private final String model;
    private final Vertx vertx;

    public AiService(Vertx vertx, String apiKey, String model) {
        this.vertx = vertx;
        this.webClient = WebClient.create(vertx);
        this.apiKey = apiKey;
        this.model = model;
    }

    public Single<AiAnalysisResult> analyzeDocument(KycRecord record) {
        String filePath = "file-uploads/" + record.getDocumentPath();


        return vertx.fileSystem().rxExists(filePath)
                .flatMap(exists -> {
                    if (!exists) return Single.error(new RuntimeException("File not found at: " + filePath));
                    return vertx.fileSystem().rxReadFile(filePath);
                })
                .map(buffer -> Base64.getEncoder().encodeToString(buffer.getBytes()))
                .flatMap(base64Image -> {


                    String promptText = String.format(
                            "You are a KYC Expert. VISUALLY ANALYZE THIS ID CARD IMAGE.\\n\" +\n" +
                                    "                    \"User Claims:\\n\" +\n" +
                                    "                    \"- Name: '%s'\\n\" +\n" +
                                    "                    \"- ID Number: '%s'\\n\" +\n" +
                                    "                    \"- ID Type: '%s'\\n\\n\" +\n" +
                                    "                    \n" +
                                    "                    \"Task:\\n\" +\n" +
                                    "                    \"1. OCR Check: Does the text in the image match the User Claims? (Ignore minor typos).\\n\" +\n" +
                                    "                    \"2. Fraud Check: Is the font consistent? Does it look photoshopped or fake?\\n\" +\n" +
                                    "                    \"3. Photo Check: Is there a clear photo on the ID?\\n\" +\n" +
                                    "                    \"4. Quality Check: Is the image too blurry to read?\\n\\n\" +\n" +
                                    "\n" +
                                    "                    \"Output JSON Format:\\n\" +\n" +
                                    "                    \"{ \\\"status\\\": \\\"AI_CLEAR\\\" (only if >80 confidence), \\\"confidenceScore\\\": (0-100), \" +\n" +
                                    "                    \"\\\"recommendation\\\": \\\"AUTO_APPROVE\\\" or \\\"MANUAL_REVIEW\\\", \" +\n" +
                                    "                    \"\\\"riskFlags\\\": [\\\"List\\\", \\\"Of\\\", \\\"Findings\\\"] }",
                            record.getUser().getFullName(),
                            record.getGovtIdNumber(),
                            record.getGovtIdType()
                    );

                    JsonObject userMessage = new JsonObject()
                            .put("role", "user")
                            .put("content", new JsonArray()
                                    .add(new JsonObject().put("type", "text").put("text", promptText))
                                    .add(new JsonObject().put("type", "image_url").put("image_url", new JsonObject()
                                            .put("url", "data:image/jpeg;base64," + base64Image)))
                            );

                    JsonObject payload = new JsonObject()
                            .put("model", model)
                            .put("messages", new JsonArray().add(userMessage));

                    // 3. Send Request (Non-blocking)
                    return webClient.postAbs("https://openrouter.ai/api/v1/chat/completions")
                            .putHeader("Authorization", "Bearer " + apiKey)
                            .putHeader("Content-Type", "application/json")
                            .rxSendJsonObject(payload)
                            .map(response -> {
                                if (response.statusCode() != 200) {
                                    System.err.println("❌ AI API Error: " + response.bodyAsString());
                                    throw new RuntimeException("AI API Error: " + response.statusMessage());
                                }

                                JsonObject body = response.bodyAsJsonObject();
                                String aiContent = body.getJsonArray("choices").getJsonObject(0).getJsonObject("message").getString("content");

                                if (aiContent.contains("```json")) {
                                    aiContent = aiContent.replace("```json", "").replace("```", "");
                                }

                                return new JsonObject(aiContent).mapTo(AiAnalysisResult.class);
                            });
                });
    }
}