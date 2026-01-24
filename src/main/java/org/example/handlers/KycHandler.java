package org.example.handlers;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.example.entity.KycRecord;
import org.example.entity.enums.GovtIdType;
import org.example.entity.enums.KycStatus;
import org.example.repository.UserRepository;
import org.example.service.KycService;
import java.io.File;
import java.time.LocalDate;

public class KycHandler {
    private final KycService kycService;
    private final UserRepository userRepo;

    public KycHandler(KycService kycService, UserRepository userRepo) {
        this.kycService = kycService;
        this.userRepo = userRepo;
    }


    public void submit(RoutingContext ctx) {
        try {
            // 1. Validation: Verify file upload
            if (ctx.fileUploads().isEmpty()) {
                ctx.response().setStatusCode(400).end("Document upload is mandatory");
                return;
            }

            var file = ctx.fileUploads().get(0);


            if (!file.contentType().matches("application/pdf|image/jpeg|image/png")) {
                ctx.response().setStatusCode(400).end("Allowed file types: PDF, JPG, PNG");
                return;
            }


            Long currentUserId = ctx.get("userId"); // Get the User ID from Token

            String originalName = file.fileName();
            String extension = "";
            int i = originalName.lastIndexOf('.');
            if (i > 0) {
                extension = originalName.substring(i); // e.g. ".png"
            }


            String newFileName = "user_" + currentUserId + "_kyc" + extension;
            String newPath = "file-uploads/" + newFileName;
            if (ctx.vertx().fileSystem().existsBlocking(newPath)) {
                ctx.vertx().fileSystem().deleteBlocking(newPath);
            }

            ctx.vertx().fileSystem().moveBlocking(file.uploadedFileName(), newPath);



            KycRecord record = new KycRecord();


            String address = ctx.request().getFormAttribute("address");
            if (address == null || address.isBlank()) throw new RuntimeException("Address is required");
            record.setAddress(address.trim());

            String dobRaw = ctx.request().getFormAttribute("dateOfBirth");
            if (dobRaw == null || dobRaw.isBlank()) throw new RuntimeException("Date of Birth is required");
            record.setDateOfBirth(LocalDate.parse(dobRaw.trim()));

            String typeRaw = ctx.request().getFormAttribute("govtIdType");
            if (typeRaw == null || typeRaw.isBlank()) throw new RuntimeException("Govt ID Type is required");
            record.setGovtIdType(GovtIdType.valueOf(typeRaw.trim()));

            String idNumRaw = ctx.request().getFormAttribute("govtIdNumber");
            if (idNumRaw == null || idNumRaw.isBlank()) throw new RuntimeException("Govt ID Number is required");
            record.setGovtIdNumber(idNumRaw.trim());


            record.setDocumentPath(newFileName);

            record.setUser(userRepo.getReference(currentUserId));


            kycService.submitKycRx(record).subscribe(
                    () -> ctx.response().setStatusCode(201).end(new JsonObject()
                            .put("message", "KYC submitted successfully").encode()),
                    err -> ctx.response().setStatusCode(400).end("Error: " + err.getMessage())
            );

        } catch (Exception e) {
            ctx.response().setStatusCode(400).end("Invalid submission: " + e.getMessage());
        }
    }


    public void getStatus(RoutingContext ctx) {
        Long userId = ctx.get("userId");
        kycService.getStatusRx(userId).subscribe(
                record -> ctx.response().putHeader("content-type", "application/json").end(JsonObject.mapFrom(record).encode()),
                err -> ctx.response().setStatusCode(404).putHeader("content-type", "application/json").end(new JsonObject().put("message", "No KYC submitted yet").encode())
        );
    }

    public void getPendingReviews(RoutingContext ctx) {
        try {
            int page = Integer.parseInt(ctx.request().getParam("page", "0"));
            int size = Integer.parseInt(ctx.request().getParam("size", "10"));
            kycService.getPendingReviewsRx(page, size).subscribe(
                    pagedList -> ctx.response().putHeader("content-type", "application/json").end(Json.encodePrettily(pagedList.getList())),
                    err -> ctx.response().setStatusCode(500).end(new JsonObject().put("error", err.getMessage()).encode())
            );
        } catch (NumberFormatException e) {
            ctx.response().setStatusCode(400).end("Invalid parameters");
        }
    }

    public void reviewKyc(RoutingContext ctx) {
        try {
            Long kycId = Long.parseLong(ctx.pathParam("id"));
            JsonObject body = ctx.body().asJsonObject();
            String statusStr = body.getString("status");
            String remarks = body.getString("adminRemarks");

            if (statusStr == null || (!statusStr.equals("APPROVED") && !statusStr.equals("REJECTED"))) {
                ctx.response().setStatusCode(400).end("Status must be APPROVED or REJECTED");
                return;
            }

            kycService.reviewKycRx(kycId, KycStatus.valueOf(statusStr), remarks).subscribe(
                    () -> ctx.response().setStatusCode(200).end(new JsonObject().put("message", "KYC Updated").encode()),
                    err -> ctx.response().setStatusCode(400).end(new JsonObject().put("error", err.getMessage()).encode())
            );
        } catch (Exception e) {
            ctx.response().setStatusCode(400).end("Invalid Request");
        }
    }
}