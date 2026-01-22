package org.example.service;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import org.example.entity.KycRecord;
import org.example.entity.enums.KycStatus;
import org.example.repository.KycRepository;

public class KycService {
    private final KycRepository kycRepo;

    public KycService(KycRepository kycRepo) {
        this.kycRepo = kycRepo;
    }

    public Completable submitKycRx(KycRecord record) {
        return Completable.fromAction(() -> {
            validateIdFormat(record.getGovtIdType().name(), record.getGovtIdNumber());
            record.setStatus(KycStatus.SUBMITTED);
            kycRepo.save(record);
        });
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
}