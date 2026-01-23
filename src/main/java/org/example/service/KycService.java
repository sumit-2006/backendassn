package org.example.service;

import io.ebean.PagedList;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.example.entity.KycRecord;
import org.example.entity.enums.KycStatus;
import org.example.repository.KycRepository;
// ✅ Import AI classes
import org.example.dto.AiAnalysisResult;
import org.example.service.AiService;

public class KycService {
    private final KycRepository kycRepo;
    private final AiService aiService;

    public KycService(KycRepository kycRepo, AiService aiService) {
        this.kycRepo = kycRepo;
        this.aiService = aiService;
    }

    public Completable submitKycRx(KycRecord record) {
        return Completable.fromAction(() -> {
            validateIdFormat(record.getGovtIdType().name(), record.getGovtIdNumber());
            record.setStatus(KycStatus.SUBMITTED);
            kycRepo.save(record);
            triggerAiAnalysis(record);
        });
    }

    // New Helper for AI Trigger
    private void triggerAiAnalysis(KycRecord record) {
        aiService.analyzeDocument(record)
                .subscribeOn(Schedulers.io()) // Run in background
                .subscribe(
                        result -> {
                            // Update DB with AI Results
                            // Note: We fetch a fresh record or update the existing one attached to context
                            record.setAiStatus(result.getStatus()); // Ensure you added this field to Entity!
                            record.setAiConfidenceScore(result.getConfidenceScore());
                            record.setAiRecommendation(result.getRecommendation());
                            record.setAiRiskFlags(result.getRiskFlags()); // Ensure Entity has List<String> or JSON support

                            kycRepo.save(record);
                        },
                        err -> {
                            System.err.println("❌ AI Service Failed: " + err.getMessage());
                            record.setAiStatus("AI_FAILED");
                            kycRepo.save(record);
                        }
                );
    }

    private void validateIdFormat(String type, String number) {
        switch (type) {
            case "PAN":
                if (!number.matches("[A-Z]{5}[0-9]{4}[A-Z]{1}"))
                    throw new RuntimeException("Invalid PAN format [cite: 121]");
                break;
            case "AADHAAR":
                if (!number.matches("^[2-9]{1}[0-9]{11}$"))
                    throw new RuntimeException("Invalid Aadhaar format [cite: 124]");
                break;
            case "PASSPORT":
                if (!number.matches("^[A-Z][0-9]{7}$"))
                    throw new RuntimeException("Invalid Passport format [cite: 127]");
                break;
        }
    }

    public Single<KycRecord> getStatusRx(Long userId) {
        return Single.fromCallable(() -> {
            KycRecord record = kycRepo.findByUserId(userId);
            if (record == null) throw new RuntimeException("No KYC record found");
            return record;
        });
    }
    public Single<PagedList<KycRecord>> getPendingReviewsRx(int page, int size) {
        return Single.fromCallable(() -> kycRepo.findPending(page, size));
    }
    public Completable reviewKycRx(Long kycId, KycStatus status, String remarks) {
        return Completable.fromAction(() -> {
            KycRecord record = kycRepo.findById(kycId);
            if (record == null) throw new RuntimeException("KYC Record not found");

            record.setStatus(status);
            record.setAdminRemarks(remarks);
            kycRepo.save(record);
        });
    }
}