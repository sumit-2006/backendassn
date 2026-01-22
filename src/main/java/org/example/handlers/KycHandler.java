package org.example.handlers;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.example.entity.KycRecord;
import org.example.entity.User;
import org.example.entity.enums.GovtIdType;
import org.example.repository.UserRepository; // Add this import
import org.example.service.KycService;
import java.time.LocalDate;

public class KycHandler {
    private final KycService kycService;
    private final UserRepository userRepo; // Add this field

    // Update constructor to accept UserRepository
    public KycHandler(KycService kycService, UserRepository userRepo) {
        this.kycService = kycService;
        this.userRepo = userRepo;
    }

    public void submit(RoutingContext ctx) {
        try {
            // Validation: Verify a file was actually uploaded
            if (ctx.fileUploads().isEmpty()) {
                ctx.response().setStatusCode(400).end("Document upload is mandatory [cite: 131]");
                return;
            }

            var file = ctx.fileUploads().get(0);

            // Validation: Mandatory file type check (PDF, JPG, PNG)
            if (!file.contentType().matches("application/pdf|image/jpeg|image/png")) {
                ctx.response().setStatusCode(400).end("Allowed file types: PDF, JPG, PNG ");
                return;
            }

            // Map request to Entity
            KycRecord record = new KycRecord();
            record.setAddress(ctx.request().getFormAttribute("address"));
            record.setDateOfBirth(LocalDate.parse(ctx.request().getFormAttribute("dateOfBirth")));
            record.setGovtIdType(GovtIdType.valueOf(ctx.request().getFormAttribute("govtIdType")));
            record.setGovtIdNumber(ctx.request().getFormAttribute("govtIdNumber"));
            record.setDocumentPath(file.uploadedFileName());

            // Get the User Reference from the token ID [cite: 23, 216]
            Long currentUserId = ctx.get("userId");
            record.setUser(userRepo.getReference(currentUserId));

            // Use RxJava3 subscription for reactive submission [cite: 11, 311]
            kycService.submitKycRx(record).subscribe(
                    () -> ctx.response().setStatusCode(201).end(new JsonObject()
                            .put("message", "KYC submitted successfully [cite: 139]").encode()),
                    err -> ctx.response().setStatusCode(400).end(err.getMessage())
            );

        } catch (Exception e) {
            ctx.response().setStatusCode(400).end("Invalid submission: " + e.getMessage());
        }
    }
}