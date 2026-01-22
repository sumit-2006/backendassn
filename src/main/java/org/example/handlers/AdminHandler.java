package org.example.handlers;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.example.entity.User;
import org.example.service.AdminService;

public class AdminHandler {
    private final AdminService adminService;

    public AdminHandler(AdminService adminService) {
        this.adminService = adminService;
    }

    public void onboard(RoutingContext ctx) {
        JsonObject body = ctx.body().asJsonObject();

        // Map to DTO instead of Entity
        org.example.dto.OnboardRequest req = body.mapTo(org.example.dto.OnboardRequest.class);

        // Manually build the User entity
        User newUser = new User();
        newUser.setFullName(req.fullName);
        newUser.setEmail(req.email);
        newUser.setMobileNumber(req.mobileNumber);
        newUser.setRole(req.role);

        ctx.vertx().executeBlocking(() -> {
            // Pass the raw password from the DTO separately
            adminService.onboardUser(newUser, req.initialPassword);
            return null;
        }).onComplete(res -> {
            if (res.succeeded()) {
                ctx.response().setStatusCode(201).end(new JsonObject()
                        .put("message", "User onboarded successfully").encode());
            } else {
                ctx.response().setStatusCode(400).end(res.cause().getMessage());
            }
        });
    }
}