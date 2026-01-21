package org.example;

import io.vertx.core.Vertx;

public class Main {
    public static void main(String[] args) {
        System.out.println(org.example.utils.PasswordUtil.hash("123456"));

        Vertx vertx = Vertx.vertx();

        vertx.deployVerticle(new MainVerticle())
                .onSuccess(id -> System.out.println("✅ MainVerticle deployed: " + id))
                .onFailure(err -> System.out.println("❌ Deploy failed: " + err.getMessage()));
    }
}
