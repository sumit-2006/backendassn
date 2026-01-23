package org.example;

import io.vertx.core.AbstractVerticle;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.vertx.core.json.jackson.DatabindCodec;
import java.io.InputStream;
import java.util.Properties;

public class MainVerticle extends AbstractVerticle {

    @Override
    public void start() {
        // 1. Setup Jackson for Dates
        DatabindCodec.mapper().registerModule(new JavaTimeModule());

        // 2. Setup Router & BodyHandler (Required for JSON/File Uploads)
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        router.route("/uploads/*").handler(StaticHandler.create("file-uploads"));

        // 3. Load Configuration
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

        // 4. Initialize ALL Repositories
        var userRepo = new org.example.repository.UserRepository();
        var tokenRepo = new org.example.repository.TokenRepository();
        var kycRepo = new org.example.repository.KycRepository();

        // ✅ NEW: Initialize the missing repositories
        var studentRepo = new org.example.repository.StudentProfileRepository();
        var teacherRepo = new org.example.repository.TeacherProfileRepository();

        // 5. Initialize Services
        var authService = new org.example.service.AuthService(userRepo, jwtSecret, expiryMs,studentRepo,teacherRepo);

        // ✅ FIX: Pass all 3 repositories to AdminService (Matches the new Constructor)
        var adminService = new org.example.service.AdminService(userRepo, studentRepo, teacherRepo);

        var kycService = new org.example.service.KycService(kycRepo);
        // 6. Initialize Handlers
        var authHandler = new org.example.handlers.AuthHandler(authService, tokenRepo, jwtSecret);
        var adminHandler = new org.example.handlers.AdminHandler(adminService);
        var kycHandler = new org.example.handlers.KycHandler(kycService, userRepo);

        // 7. Initialize Middleware
        var jwtMiddleware = new org.example.middleware.JwtAuthMiddleware(jwtSecret, tokenRepo);

        // 8. Register Routes
        org.example.routes.AuthRoutes.register(router, authHandler,jwtMiddleware);
        org.example.routes.AdminRoutes.register(router, adminHandler, jwtMiddleware);
        org.example.routes.KycRoutes.register(router, kycHandler, jwtMiddleware);

        // 9. Start Server
        vertx.createHttpServer()
                .requestHandler(router)
                .listen(8080)
                .onSuccess(server -> System.out.println("✅ HTTP Server started on port " + server.actualPort()))
                .onFailure(err -> System.out.println("❌ Failed to start server: " + err.getMessage()));
    }
}