package org.example;

import io.vertx.core.AbstractVerticle;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.vertx.core.json.jackson.DatabindCodec;
import org.example.config.EnvironmentConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(MainVerticle.class);

    @Override
    public void start() {
        DatabindCodec.mapper().registerModule(new JavaTimeModule());

        try {
            // Validate required environment variables
            EnvironmentConfig.validateRequiredProperties();

            // Get configuration values
            String jwtSecret = EnvironmentConfig.get("JWT_SECRET");
            String dbUser = EnvironmentConfig.get("DB_USERNAME", "root");
            String dbPass = EnvironmentConfig.get("DB_PASSWORD");
            String aiKey = EnvironmentConfig.get("AI_API_KEY");

            // Parse numeric values with proper error handling
            long expiryMs;
            try {
                String expiryStr = EnvironmentConfig.get("JWT_EXPIRY_MS", "86400000");
                expiryMs = Long.parseLong(expiryStr);
                if (expiryMs <= 0) {
                    throw new IllegalArgumentException("JWT expiry must be positive");
                }
            } catch (NumberFormatException e) {
                logger.warn("Invalid JWT_EXPIRY_MS format, using default: {}", e.getMessage());
                expiryMs = 86400000L; // 24 hours default
            }

            String aiModel = EnvironmentConfig.get("AI_MODEL", "meta-llama/llama-3.3-70b-instruct:free");

            // Get configurable upload directory
            String uploadDir = EnvironmentConfig.get("UPLOAD_DIRECTORY", "file-uploads");

            // Final validation
            if (jwtSecret == null || jwtSecret.trim().isEmpty()) {
                throw new IllegalStateException("JWT_SECRET cannot be empty");
            }
            if (dbPass == null || dbPass.trim().isEmpty()) {
                throw new IllegalStateException("DB_PASSWORD cannot be empty");
            }

            // Initialize router with configurable upload directory
            Router router = Router.router(vertx);
            router.route().handler(BodyHandler.create().setUploadsDirectory(uploadDir));
            router.route("/uploads/*").handler(StaticHandler.create(uploadDir));

            // Initialize repositories
            var userRepo = new org.example.repository.UserRepository();
            var tokenRepo = new org.example.repository.TokenRepository();
            var kycRepo = new org.example.repository.KycRepository();
            var studentRepo = new org.example.repository.StudentProfileRepository();
            var teacherRepo = new org.example.repository.TeacherProfileRepository();

            // Initialize services
            var authService = new org.example.service.AuthService(userRepo, jwtSecret, expiryMs, studentRepo, teacherRepo);
            var adminService = new org.example.service.AdminService(userRepo, studentRepo, teacherRepo);

            io.vertx.rxjava3.core.Vertx rxVertx = io.vertx.rxjava3.core.Vertx.newInstance(vertx);
            var aiService = new org.example.service.AiService(rxVertx, aiKey, aiModel);
            var kycService = new org.example.service.KycService(kycRepo, aiService);

            // Initialize handlers
            var authHandler = new org.example.handlers.AuthHandler(authService, tokenRepo, jwtSecret);
            var adminHandler = new org.example.handlers.AdminHandler(adminService);
            var kycHandler = new org.example.handlers.KycHandler(kycService, userRepo);
            var jwtMiddleware = new org.example.middleware.JwtAuthMiddleware(jwtSecret, tokenRepo);

            // Register routes
            org.example.routes.AuthRoutes.register(router, authHandler, jwtMiddleware);
            org.example.routes.AdminRoutes.register(router, adminHandler, jwtMiddleware);
            org.example.routes.KycRoutes.register(router, kycHandler, jwtMiddleware);

            // Start server with proper error handling
            vertx.createHttpServer()
                    .requestHandler(router)
                    .listen(8080)
                    .onSuccess(server -> {
                        logger.info("✅ Server started successfully on port {}", server.actualPort());
                        logger.info("✅ Upload directory: {}", uploadDir);
                        logger.info("✅ AI Model: {}", aiModel);
                    })
                    .onFailure(err -> {
                        logger.error("❌ Failed to start server", err);
                        vertx.close();
                    });

        } catch (IllegalStateException e) {
            logger.error("❌ Configuration error: {}", e.getMessage());
            logger.error("Please set required environment variables or create .env file");
            vertx.close();
        } catch (IllegalArgumentException e) {
            logger.error("❌ Invalid configuration: {}", e.getMessage());
            vertx.close();
        } catch (Exception e) {
            logger.error("❌ Startup failed", e);
            vertx.close();
        }
    }
}
