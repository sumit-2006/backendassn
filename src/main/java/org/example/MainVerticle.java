package org.example;

import io.vertx.core.AbstractVerticle;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.vertx.core.json.jackson.DatabindCodec;
import java.io.InputStream;
import java.util.Properties;

//
public class MainVerticle extends AbstractVerticle {

    @Override
    public void start() {
        DatabindCodec.mapper().registerModule(new JavaTimeModule());

        Properties props = new Properties();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (is != null) props.load(is);
        } catch (Exception e) {
            System.err.println("Warning: application.properties not found.");
        }

        String jwtSecret = System.getenv("JWT_SECRET") != null ? System.getenv("JWT_SECRET") : props.getProperty("jwt.secret");
        String dbUser = System.getenv("DB_USER") != null ? System.getenv("DB_USER") : props.getProperty("datasource.db.username");
        String dbPass = System.getenv("DB_PASSWORD") != null ? System.getenv("DB_PASSWORD") : props.getProperty("datasource.db.password");
        String aiKey = System.getenv("AI_API_KEY") != null ? System.getenv("AI_API_KEY") : props.getProperty("ai.apiKey");

        if (jwtSecret == null || dbPass == null) {
            System.err.println("CRITICAL: Missing Security Credentials (JWT_SECRET or DB_PASSWORD). Exiting.");
            vertx.close();
            return;
        }

        long expiryMs = Long.parseLong(props.getProperty("jwt.expiryMs", "86400000"));
        String aiModel = props.getProperty("ai.model", "mistralai/mistral-7b-instruct:free");

        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create().setUploadsDirectory("file-uploads")); // Secure upload dir
        router.route("/uploads/*").handler(StaticHandler.create("file-uploads"));

        var userRepo = new org.example.repository.UserRepository();
        var tokenRepo = new org.example.repository.TokenRepository();
        var kycRepo = new org.example.repository.KycRepository();
        var studentRepo = new org.example.repository.StudentProfileRepository();
        var teacherRepo = new org.example.repository.TeacherProfileRepository();

        var authService = new org.example.service.AuthService(userRepo, jwtSecret, expiryMs, studentRepo, teacherRepo);
        var adminService = new org.example.service.AdminService(userRepo, studentRepo, teacherRepo);

        io.vertx.rxjava3.core.Vertx rxVertx = io.vertx.rxjava3.core.Vertx.newInstance(vertx);
        var aiService = new org.example.service.AiService(rxVertx, aiKey, aiModel);
        var kycService = new org.example.service.KycService(kycRepo, aiService);


        var authHandler = new org.example.handlers.AuthHandler(authService, tokenRepo, jwtSecret);
        var adminHandler = new org.example.handlers.AdminHandler(adminService);
        var kycHandler = new org.example.handlers.KycHandler(kycService, userRepo);
        var jwtMiddleware = new org.example.middleware.JwtAuthMiddleware(jwtSecret, tokenRepo);


        org.example.routes.AuthRoutes.register(router, authHandler, jwtMiddleware);
        org.example.routes.AdminRoutes.register(router, adminHandler, jwtMiddleware);
        org.example.routes.KycRoutes.register(router, kycHandler, jwtMiddleware);


        vertx.createHttpServer()
                .requestHandler(router)
                .listen(8080)
                .onSuccess(server -> System.out.println("HTTP Server secure started on port " + server.actualPort()))
                .onFailure(err -> System.err.println("Failed to start server: " + err.getMessage()));
    }
}