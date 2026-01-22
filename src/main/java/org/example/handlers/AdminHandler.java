package org.example.handlers;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.example.entity.User;
import org.example.service.AdminService;
import org.example.dto.OnboardRequest;

public class AdminHandler {
    private final AdminService adminService;

    public AdminHandler(AdminService adminService) {
        this.adminService = adminService;
    }

    public void onboard(RoutingContext ctx) {
        JsonObject body = ctx.body().asJsonObject();
        org.example.dto.OnboardRequest req = body.mapTo(org.example.dto.OnboardRequest.class);

        User newUser = new User();
        newUser.setFullName(req.fullName);
        newUser.setEmail(req.email);
        newUser.setMobileNumber(req.mobileNumber);
        newUser.setRole(req.role);

        adminService.onboardUserRx(newUser, req.initialPassword)
                .subscribe(
                        () -> ctx.response().setStatusCode(201).end(new JsonObject()
                                .put("message", "User onboarded successfully").encode()),
                        err -> ctx.response().setStatusCode(400).end(err.getMessage())
                );
    }
}