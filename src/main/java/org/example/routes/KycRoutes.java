package org.example.routes;

import io.vertx.ext.web.Router;
import org.example.handlers.KycHandler;
import org.example.middleware.JwtAuthMiddleware;
import org.example.middleware.RoleGuard;

public class KycRoutes {
    public static void register(Router router, KycHandler handler, JwtAuthMiddleware auth) {
        // User KYC Submissions
        router.post("/kyc/submit").handler(auth::handle).handler(handler::submit);
        router.get("/kyc/status")
                .handler(auth::handle)
                .handler(handler::getStatus);
        router.get("/admin/kyc/review")
                .handler(auth::handle)
                .handler(RoleGuard.only("ADMIN")::handle) // Add RoleGuard if needed
                .handler(handler::getPendingReviews);

        router.patch("/admin/kyc/:id/review")
                .handler(auth::handle)
                .handler(RoleGuard.only("ADMIN")::handle)
                .handler(handler::reviewKyc);
        // Admin KYC Review Endpoints
        // router.get("/admin/kyc/review").handler(auth::handle)...
    }
}