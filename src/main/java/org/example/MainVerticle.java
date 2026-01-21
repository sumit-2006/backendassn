package org.example;

import io.vertx.core.AbstractVerticle;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

public class MainVerticle extends AbstractVerticle {

    @Override
    public void start() {

        Router router = Router.router(vertx);

        // ✅ BodyHandler required for JSON bodies
        router.route().handler(BodyHandler.create());

        // ✅ Health check
        router.get("/").handler(ctx -> {
            ctx.response().end("✅ Role Based Learning Platform Backend Running");
        });
        router.get("/health/db").handler(ctx -> {
            try {
                var db = org.example.config.DbConfig.getDatabase();
                db.sqlQuery("select 1").findOne();

                ctx.response()
                        .putHeader("content-type", "application/json")
                        .end(new io.vertx.core.json.JsonObject()
                                .put("status", "UP")
                                .put("db", "CONNECTED")
                                .encodePrettily());

            } catch (Exception e) {
                ctx.response().setStatusCode(500).end("❌ DB NOT CONNECTED: " + e.getMessage());
            }
        });

        // TODO Phase-2: Auth routes
        // AuthRoutes.register(router, services...);

        vertx.createHttpServer()
                .requestHandler(router)
                .listen(8080)
                .onSuccess(server ->
                        System.out.println("✅ HTTP Server started on port " + server.actualPort()))
                .onFailure(err ->
                        System.out.println("❌ Failed to start server: " + err.getMessage()));
    }
}
