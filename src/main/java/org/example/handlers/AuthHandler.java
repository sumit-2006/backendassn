package org.example.handlers;

import io.jsonwebtoken.Claims;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.example.dto.LoginRequest;
import org.example.repository.TokenRepository;
import org.example.service.AuthService;
import org.example.utils.JwtUtil;

public class AuthHandler {

    private final AuthService authService;
    private final TokenRepository tokenRepo;
    private final String secret;

    public AuthHandler(AuthService authService, TokenRepository tokenRepo, String secret) {
        this.authService = authService;
        this.tokenRepo = tokenRepo;
        this.secret = secret;
    }

    // Example for AuthHandler.java
    public void login(RoutingContext ctx) {
        LoginRequest req = ctx.body().asJsonObject().mapTo(LoginRequest.class);

        authService.loginRx(req.email, req.password)
                .subscribe(
                        token -> ctx.response().setStatusCode(200)
                                .end(new JsonObject().put("accessToken", token).encode()),
                        err -> ctx.response().setStatusCode(401).end(err.getMessage())
                );
    }

    public void getProfile(io.vertx.ext.web.RoutingContext ctx) {
        Long userId = ctx.get("userId"); // Got from JwtAuthMiddleware

        authService.getProfileRx(userId).subscribe(
                json -> ctx.response()
                        .setStatusCode(200)
                        .putHeader("content-type", "application/json")
                        .end(json.encodePrettily()),
                err -> ctx.response().setStatusCode(404).end(new JsonObject().put("error", err.getMessage()).encode())
        );
    }
    public void logout(RoutingContext ctx) {
        try {
            String auth = ctx.request().getHeader("Authorization");
            if (auth == null || !auth.startsWith("Bearer ")) {
                ctx.response().setStatusCode(400).end("Missing token");
                return;
            }

            String token = auth.substring(7);
            String tokenHash = String.valueOf(token.hashCode());

            Claims claims = JwtUtil.verify(token, secret);
            tokenRepo.invalidate(tokenHash, claims.getExpiration().toInstant());

            ctx.response().end("✅ Logged out");

        } catch (Exception e) {
            ctx.response().setStatusCode(401).end("Invalid token");
        }
    }

}
