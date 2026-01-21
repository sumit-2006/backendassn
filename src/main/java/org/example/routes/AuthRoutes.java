package org.example.routes;

import io.vertx.ext.web.Router;
import org.example.handlers.AuthHandler;

public class AuthRoutes {
    public static void register(Router router, AuthHandler handler) {
        router.post("/auth/login").handler(handler::login);
        router.post("/auth/logout").handler(handler::logout);
    }
}
