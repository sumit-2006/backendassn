package org.example.handlers;

import io.jsonwebtoken.Claims;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.example.dto.LoginRequest;
import org.example.repository.TokenRepository;
import org.example.service.AuthService;
import org.example.utils.JwtUtil;
import io.vertx.core.Promise; // ✅ Required Import
import  io.vertx.core.Handler;


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
        try{
            JsonObject reqBody=ctx.body().asJsonObject();
            String email=reqBody.getString("email");
            String password=reqBody.getString("password");

            org.example.utils.ValidationUtil.validateLogin(email, password);

        LoginRequest req = ctx.body().asJsonObject().mapTo(LoginRequest.class);

        authService.loginRx(req.email, req.password)
                .subscribe(
                        token -> ctx.response().setStatusCode(200)
                                .end(new JsonObject().put("accessToken", token).encode()),
                        err -> ctx.response().setStatusCode(401).end(err.getMessage())
                );
    } catch (IllegalArgumentException e) {
        ctx.response().setStatusCode(400).end(new JsonObject().put("error", e.getMessage()).encode());
    } catch (Exception e) {
        ctx.response().setStatusCode(400).end("Invalid Login Request");
    }
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
    /*public void logout(RoutingContext ctx) {
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
    }*/

    // ... imports
// Remove: import io.vertx.core.Promise; (No longer needed for this method)

    public void logout(RoutingContext ctx) {
        String auth = ctx.request().getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            ctx.response().setStatusCode(400).end("Missing token");
            return;
        }

        // ✅ FIX: Use a Callable ( () -> { ... return value; } )
        ctx.vertx().executeBlocking(() -> {
                    String token = auth.substring(7);

                    // 1. Verify Token (Blocking CPU work)
                    Claims claims = JwtUtil.verify(token, secret);

                    // 2. Invalidate in DB (Blocking I/O)
                    if (claims != null) {
                        String tokenHash = String.valueOf(token.hashCode());
                        tokenRepo.invalidate(tokenHash, claims.getExpiration().toInstant());
                    }

                    return null; // ✅ Must return null (or a value) to satisfy Callable<T>
                })
                .onSuccess(v -> ctx.response().setStatusCode(200).end("✅ Logged out"))
                .onFailure(err -> ctx.response().setStatusCode(401).end("Invalid token: " + err.getMessage()));
    }


    public void updateProfile(RoutingContext ctx) {
        Long userId = ctx.get("userId");

        try {
            org.example.dto.UpdateProfileRequest req = ctx.body().asJsonObject().mapTo(org.example.dto.UpdateProfileRequest.class);

            authService.updateProfileRx(userId, req).subscribe(
                    json -> ctx.response().setStatusCode(200).end(json.encode()),
                    err -> ctx.response().setStatusCode(400).end(new JsonObject().put("error", err.getMessage()).encode())
            );
        } catch (Exception e) {
            ctx.response().setStatusCode(400).end("Invalid Request JSON");
        }
    }

}
