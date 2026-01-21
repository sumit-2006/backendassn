package org.example.middleware;

import io.jsonwebtoken.Claims;
import io.vertx.ext.web.RoutingContext;
import org.example.repository.TokenRepository;
import org.example.utils.JwtUtil;

public class JwtAuthMiddleware {

    private final String secret;
    private final TokenRepository tokenRepo;

    public JwtAuthMiddleware(String secret, TokenRepository tokenRepo) {
        this.secret = secret;
        this.tokenRepo = tokenRepo;
    }

    public void handle(RoutingContext ctx) {
        try {
            String auth = ctx.request().getHeader("Authorization");

            if (auth == null || !auth.startsWith("Bearer ")) {
                ctx.response().setStatusCode(401).end("Missing token");
                return;
            }

            String token = auth.substring(7);
            String tokenHash = String.valueOf(token.hashCode());

            if (tokenRepo.isInvalidated(tokenHash)) {
                ctx.response().setStatusCode(401).end("Token invalidated");
                return;
            }

            Claims claims = JwtUtil.verify(token, secret);

            ctx.put("userId", Long.parseLong(claims.getSubject()));
            ctx.put("role", (String) claims.get("role"));

            ctx.next();

        } catch (Exception e) {
            ctx.response().setStatusCode(401).end("Invalid token");
        }
    }
}
