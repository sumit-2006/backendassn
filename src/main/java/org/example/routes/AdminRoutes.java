package org.example.routes;

import io.vertx.ext.web.Router;
import org.example.handlers.AdminHandler;
import org.example.middleware.JwtAuthMiddleware;
import org.example.middleware.RoleGuard;

public class AdminRoutes {
    public static void register(Router router, AdminHandler handler, JwtAuthMiddleware auth) {
        router.post("/admin/onboard")
                .handler(auth::handle)
                .handler(RoleGuard.only("ADMIN")::handle)
                .handler(handler::onboard);
        router.get("/admin/users")
                .handler(auth::handle)
                .handler(RoleGuard.only("ADMIN")::handle)
                .handler(handler::listUsers);
        /*router.post("/admin/users/bulk")
                .handler(auth::handle)
                .handler(RoleGuard.only("ADMIN")::handle)
                .handler(handler::bulkImport);*/

        router.put("/admin/users/:userId/status")
                .handler(auth::handle)
                .handler(RoleGuard.only("ADMIN")::handle)
                .handler(handler::changeUserStatus);


        router.post("/admin/users/bulk")
                .handler(auth::handle)
                .handler(RoleGuard.only("ADMIN")::handle)
                .handler(handler::bulkImport);


        router.get("/admin/uploads/:id/status")
                .handler(auth::handle)
                .handler(RoleGuard.only("ADMIN")::handle)
                .handler(handler::getBulkStatus);


        router.delete("/admin/users/:userId")
                .handler(auth::handle)
                .handler(RoleGuard.only("ADMIN")::handle)
                .handler(handler::deleteUser);
    }
}