package org.example;

import io.vertx.core.AbstractVerticle;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import java.io.InputStream;
import java.util.Properties;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule; // ✅ Import this
import io.vertx.core.json.jackson.DatabindCodec; // ✅ Import this
import io.vertx.ext.web.handler.StaticHandler; // ✅ Import this

public class MainVerticle extends AbstractVerticle {

    @Override
    public void start() {
        DatabindCodec.mapper().registerModule(new JavaTimeModule());
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        router.route("/uploads/*").handler(StaticHandler.create("file-uploads"));

        // 1. Load Configuration
        Properties props = new Properties();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (is == null) throw new RuntimeException("application.properties not found");
            props.load(is);
        } catch (Exception e) {
            System.err.println("❌ Failed to load config: " + e.getMessage());
            return;
        }

        String jwtSecret = props.getProperty("jwt.secret");
        long expiryMs = Long.parseLong(props.getProperty("jwt.expiryMs"));

        // 2. Initialize Repositories [cite: 188]
        var userRepo = new org.example.repository.UserRepository();
        var tokenRepo = new org.example.repository.TokenRepository();
        var kycRepo = new org.example.repository.KycRepository();

        // 3. Initialize Services [cite: 187]
        var authService = new org.example.service.AuthService(userRepo, jwtSecret, expiryMs);
        var adminService = new org.example.service.AdminService(userRepo);
        var kycService = new org.example.service.KycService(kycRepo);

        // 4. Initialize Handlers [cite: 186]
        var authHandler = new org.example.handlers.AuthHandler(authService, tokenRepo, jwtSecret);
        var adminHandler = new org.example.handlers.AdminHandler(adminService);
        var kycHandler = new org.example.handlers.KycHandler(kycService, userRepo);

        // 5. Initialize Middleware [cite: 25, 26]
        var jwtMiddleware = new org.example.middleware.JwtAuthMiddleware(jwtSecret, tokenRepo);

        // 6. Register Routes [cite: 190]
        org.example.routes.AuthRoutes.register(router, authHandler);
        org.example.routes.AdminRoutes.register(router, adminHandler, jwtMiddleware);
        org.example.routes.KycRoutes.register(router, kycHandler, jwtMiddleware);

        // 7. Start Server [cite: 150]
        vertx.createHttpServer()
                .requestHandler(router)
                .listen(8080)
                .onSuccess(server -> System.out.println("✅ HTTP Server started on port " + server.actualPort()))
                .onFailure(err -> System.out.println("❌ Failed to start server: " + err.getMessage()));
    }
}