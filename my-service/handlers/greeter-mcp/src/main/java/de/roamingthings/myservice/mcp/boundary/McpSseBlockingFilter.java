package de.roamingthings.myservice.mcp.boundary;

import de.roamingthings.myservice.mcp.Requirement;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

import static de.roamingthings.myservice.mcp.Requirement.Rn.R2_1;
import static de.roamingthings.myservice.mcp.Requirement.Rn.R2_2;

@ApplicationScoped
class McpSseBlockingFilter {

    @Requirement({R2_1, R2_2})
    void blockMcpGet(@Observes Router router) {
        router.route(HttpMethod.GET, "/mcp")
                .order(Integer.MIN_VALUE)
                .handler(this::refuseEventStream);
    }

    @Requirement(R2_1)
    void refuseEventStream(RoutingContext ctx) {
        ctx.response()
                .setStatusCode(405)
                .putHeader("Allow", "POST, DELETE")
                .end();
    }
}
