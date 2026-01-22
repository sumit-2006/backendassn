package org.example;

import io.vertx.core.AbstractVerticle;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

public class MainVerticle extends AbstractVerticle {

    @Override
    public void start() {

        Router router = Router.router(vertx);

        // ✅ BodyHandler required for JSON bodies
        router.route().handler(BodyHandler.create());

        // ✅ Health check
        router.get("/").handler(ctx -> {
            ctx.response().end("✅ Role Based Learning Platform Backend Running");
        });
        router.get("/health/db").handler(ctx -> {
            try {
                var db = org.example.config.DbConfig.getDatabase();
                db.sqlQuery("select 1").findOne();

                ctx.response()
                        .putHeader("content-type", "application/json")
                        .end(new io.vertx.core.json.JsonObject()
                                .put("status", "UP")
                                .put("db", "CONNECTED")
                                .encodePrettily());

            } catch (Exception e) {
                ctx.response().setStatusCode(500).end("❌ DB NOT CONNECTED: " + e.getMessage());
            }
        });

        // TODO Phase-2: Auth routes
        // AuthRoutes.register(router, services...);
        String jwtSecret = "role-learning-secret-role-learning-secret-12345";
        long expiryMs = 86400000L;

        var userRepo = new org.example.repository.UserRepository();
        var tokenRepo = new org.example.repository.TokenRepository();

        var authService = new org.example.service.AuthService(userRepo, jwtSecret, expiryMs);
        var authHandler = new org.example.handlers.AuthHandler(authService, tokenRepo, jwtSecret);

        org.example.routes.AuthRoutes.register(router, authHandler);

// ✅ Test protected endpoint
        var jwtMiddleware = new org.example.middleware.JwtAuthMiddleware(jwtSecret, tokenRepo);

        router.get("/admin/test")
                .handler(jwtMiddleware::handle)
                .handler(org.example.middleware.RoleGuard.only("ADMIN")::handle)
                .handler(ctx -> ctx.response().end("✅ ADMIN route working"));

        vertx.createHttpServer()
                .requestHandler(router)
                .listen(8080)
                .onSuccess(server ->
                        System.out.println("✅ HTTP Server started on port " + server.actualPort()))
                .onFailure(err ->
                        System.out.println("❌ Failed to start server: " + err.getMessage()));
    }
}
