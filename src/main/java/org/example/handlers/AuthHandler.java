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

    public void login(RoutingContext ctx) {
        try {
            var json = ctx.body().asJsonObject();

            if (json == null) {
                ctx.response().setStatusCode(400).end("Request body must be JSON");
                return;
            }

            LoginRequest req = json.mapTo(LoginRequest.class);

            if (req.email == null || req.password == null) {
                ctx.response().setStatusCode(400).end("email and password are required");
                return;
            }

            String token = authService.login(req.email, req.password);

            ctx.response()
                    .putHeader("content-type", "application/json")
                    .end(new JsonObject()
                            .put("accessToken", token)
                            .put("message", "Login successful")
                            .encodePrettily());

        } catch (Exception e) {
            ctx.response().setStatusCode(401).end(e.getMessage());
        }
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
