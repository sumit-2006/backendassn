package org.example.handlers;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.example.dto.OnboardRequest;
import org.example.entity.enums.UserStatus;
import org.example.service.AdminService;

public class AdminHandler {
    private final AdminService adminService;

    public AdminHandler(AdminService adminService) {
        this.adminService = adminService;
    }

    public void onboard(RoutingContext ctx) {
        try {
            JsonObject body = ctx.body().asJsonObject();

            OnboardRequest req = body.mapTo(OnboardRequest.class);


            adminService.onboardUserRx(req)
                    .subscribe(
                            () -> ctx.response().setStatusCode(201).end(new JsonObject()
                                    .put("message", "User onboarded successfully").encode()),
                            err -> ctx.response().setStatusCode(400).end(new JsonObject()
                                    .put("error", err.getMessage()).encode())
                    );
        } catch (Exception e) {
            ctx.response().setStatusCode(400).end("Invalid Request: " + e.getMessage());
        }
    }

    public void listUsers(RoutingContext ctx) {
        try {
            int page = Integer.parseInt(ctx.request().getParam("page", "0"));
            int size = Integer.parseInt(ctx.request().getParam("size", "10"));
            String roleStr = ctx.request().getParam("role");
            org.example.entity.enums.Role role = (roleStr != null) ? org.example.entity.enums.Role.valueOf(roleStr) : null;

            ctx.vertx().executeBlocking(() -> {
                // Use the repository method exposed via Service
                return adminService.getUserRepo().findPaged(page, size, role);
            }).onComplete(res -> {
                if (res.succeeded()) {
                    ctx.response()
                            .putHeader("content-type", "application/json")
                            .end(Json.encodePrettily(res.result())); // Use Json.encodePrettily for cleaner output
                } else {
                    ctx.response().setStatusCode(500).end(res.cause().getMessage());
                }
            });
        } catch (Exception e) {
            ctx.response().setStatusCode(400).end("Invalid parameters");
        }
    }




    public void bulkImport(RoutingContext ctx) {
        if (ctx.fileUploads().isEmpty()) {
            ctx.response().setStatusCode(400).end("CSV file is mandatory");
            return;
        }

        String uploadedFilePath = ctx.fileUploads().get(0).uploadedFileName();
        Long adminId = ctx.get("userId"); // From JWT

        adminService.initiateBulkImport(adminId, uploadedFilePath).subscribe(
                json -> ctx.response().setStatusCode(202).end(json.encode()), // 202 Accepted
                err -> ctx.response().setStatusCode(500).end(new JsonObject().put("error", err.getMessage()).encode())
        );
    }

    public void getBulkStatus(RoutingContext ctx) {
        try {
            Long uploadId = Long.parseLong(ctx.pathParam("id"));
            // This needs to be blocking as Ebean is blocking
            ctx.vertx().executeBlocking(() -> adminService.getBulkUploadRepo().findById(uploadId))
                    .onComplete(res -> {
                        if (res.succeeded() && res.result() != null) {
                            ctx.response().putHeader("content-type", "application/json").end(Json.encodePrettily(res.result()));
                        } else {
                            ctx.response().setStatusCode(404).end("Upload not found");
                        }
                    });
        } catch (Exception e) {
            ctx.response().setStatusCode(400).end("Invalid ID");
        }
    }

    public void changeUserStatus(RoutingContext ctx) {
        try {
            Long userId = Long.parseLong(ctx.pathParam("userId"));
            JsonObject body = ctx.body().asJsonObject();
            String statusStr = body.getString("status");
            UserStatus status = UserStatus.valueOf(statusStr);

            adminService.updateUserStatusRx(userId, status).subscribe(
                    json -> ctx.response().setStatusCode(200).end(json.encode()),
                    err -> ctx.response().setStatusCode(400).end(new JsonObject().put("error", err.getMessage()).encode())
            );
        } catch (Exception e) {
            ctx.response().setStatusCode(400).end("Invalid Request: Send { \"status\": \"ACTIVE\"/\"INACTIVE\" }");
        }
    }
    public void deleteUser(RoutingContext ctx) {
        try {
            Long userId = Long.parseLong(ctx.pathParam("userId"));
            adminService.deleteUserRx(userId).subscribe(
                    json -> ctx.response().setStatusCode(200).end(json.encode()),
                    err -> ctx.response().setStatusCode(400).end(new JsonObject().put("error", err.getMessage()).encode())
            );
        } catch (Exception e) {
            ctx.response().setStatusCode(400).end("Invalid User ID");
        }
    }
}