package de.roamingthings.myservice.mcp.boundary;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@TestSecurity(user = "test-user", roles = {})
class GreetingMcpToolsIT {

    @Test
    void greetReturnsGreeting() {
        var sessionId = initializeMcpSession();

        var response = RestAssured.given()
                .contentType("application/json")
                .accept("application/json, text/event-stream")
                .header("Mcp-Session-Id", sessionId)
                .body("""
                        {"jsonrpc":"2.0","method":"tools/call","params":{"name":"greet","arguments":{"name":"World"}},"id":1}
                        """)
                .post("/mcp");

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.asString()).contains("Hello, World!");
    }

    String initializeMcpSession() {
        var initResponse = RestAssured.given()
                .contentType("application/json")
                .accept("application/json, text/event-stream")
                .body("""
                        {"jsonrpc":"2.0","method":"initialize","params":{"protocolVersion":"2025-03-26","capabilities":{},"clientInfo":{"name":"test","version":"1.0"}},"id":0}
                        """)
                .post("/mcp");

        assertThat(initResponse.statusCode()).isEqualTo(200);
        var sessionId = initResponse.header("Mcp-Session-Id");
        assertThat(sessionId).isNotBlank();

        RestAssured.given()
                .contentType("application/json")
                .accept("application/json, text/event-stream")
                .header("Mcp-Session-Id", sessionId)
                .body("""
                        {"jsonrpc":"2.0","method":"notifications/initialized"}
                        """)
                .post("/mcp");

        return sessionId;
    }
}
