package de.roamingthings.myservice.mcp.boundary;

import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
class GreetingMcpTools {

    @Tool(name = "greet", description = "Returns a greeting for the given name")
    String greet(@ToolArg(description = "Name to greet") String name) {
        return "Hello, " + name + "!";
    }
}
