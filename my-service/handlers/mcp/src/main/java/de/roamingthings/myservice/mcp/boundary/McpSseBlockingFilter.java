package de.roamingthings.myservice.mcp.boundary;

import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

@ApplicationScoped
class McpSseBlockingFilter {

    void blockMcpGet(@Observes Router router) {
        router.route(HttpMethod.GET, "/mcp")
                .order(Integer.MIN_VALUE)
                .handler(this::rejectSseRequest);
    }

    void rejectSseRequest(RoutingContext ctx) {
        ctx.response()
                .setStatusCode(405)
                .putHeader("Allow", "POST, DELETE")
                .end();
    }
}
