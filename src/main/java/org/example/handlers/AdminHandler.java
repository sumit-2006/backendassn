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
        OnboardRequest req = body.mapTo(OnboardRequest.class);

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

    public void listUsers(RoutingContext ctx) {
        try {
            int page = Integer.parseInt(ctx.request().getParam("page", "0"));
            int size = Integer.parseInt(ctx.request().getParam("size", "10"));
            String roleStr = ctx.request().getParam("role");
            org.example.entity.enums.Role role = (roleStr != null) ? org.example.entity.enums.Role.valueOf(roleStr) : null;

            ctx.vertx().executeBlocking(() -> {
                // Use the repository method you already wrote
                return adminService.getUserRepo().findPaged(page, size, role);
            }).onComplete(res -> {
                if (res.succeeded()) {
                    ctx.response()
                            .putHeader("content-type", "application/json")
                            .end(io.vertx.core.json.JsonObject.mapFrom(res.result()).encodePrettily());
                } else {
                    ctx.response().setStatusCode(500).end(res.cause().getMessage());
                }
            });
        } catch (Exception e) {
            ctx.response().setStatusCode(400).end("Invalid parameters");
        }
    }
}