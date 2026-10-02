package de.roamingthings.myservice.mcp.boundary;

import de.roamingthings.myservice.mcp.Requirement;
import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;
import jakarta.enterprise.context.ApplicationScoped;

import static de.roamingthings.myservice.mcp.Requirement.Rn.R1_1;

@ApplicationScoped
class GreetingMcpTools {

    @Tool(name = "greet", description = "Returns a greeting for the given name")
    @Requirement(R1_1)
    String greet(@ToolArg(description = "Name to greet") String name) {
        return "Hello, " + name + "!";
    }
}
