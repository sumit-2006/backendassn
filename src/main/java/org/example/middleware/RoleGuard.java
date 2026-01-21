package org.example.middleware;

import io.vertx.ext.web.RoutingContext;
import java.util.Set;

public class RoleGuard {

    private final Set<String> allowed;

    public RoleGuard(Set<String> allowed) {
        this.allowed = allowed;
    }

    public void handle(RoutingContext ctx) {
        String role = ctx.get("role");
        if (role == null || !allowed.contains(role)) {
            ctx.response().setStatusCode(403).end("Forbidden");
            return;
        }
        ctx.next();
    }

    public static RoleGuard only(String... roles) {
        return new RoleGuard(Set.of(roles));
    }
}
