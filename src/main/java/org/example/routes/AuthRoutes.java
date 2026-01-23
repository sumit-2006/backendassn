package org.example.routes;

import io.vertx.ext.web.Router;
import org.example.handlers.AuthHandler;
import org.example.middleware.JwtAuthMiddleware; // ✅ Import this

public class AuthRoutes {
    public static void register(Router router, AuthHandler handler, JwtAuthMiddleware authMiddleware) {
        router.post("/auth/login").handler(handler::login);
        router.post("/auth/logout").handler(handler::logout);
        // ✅ NEW: Protected Profile Route
        router.get("/auth/me")
                .handler(authMiddleware::handle) // Require Login
                .handler(handler::getProfile);
        // ✅ NEW: UPDATE Profile
        router.put("/auth/me")
                .handler(authMiddleware::handle) // Protect
                .handler(handler::updateProfile);
    }
}
