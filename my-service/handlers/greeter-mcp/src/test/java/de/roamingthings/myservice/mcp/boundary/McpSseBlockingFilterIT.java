package de.roamingthings.myservice.mcp.boundary;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@TestSecurity(user = "test-user", roles = {})
class McpSseBlockingFilterIT {

    @Test
    void getMcpReturns405() {
        var response = RestAssured.given()
                .accept("text/event-stream")
                .get("/mcp");

        assertThat(response.statusCode()).isEqualTo(405);
        assertThat(response.header("Allow")).isEqualTo("POST, DELETE");
    }

    @Test
    void postMcpIsNotBlocked() {
        var response = RestAssured.given()
                .contentType("application/json")
                .accept("application/json, text/event-stream")
                .body("""
                        {"jsonrpc":"2.0","method":"initialize","params":{"protocolVersion":"2025-03-26","capabilities":{},"clientInfo":{"name":"test","version":"1.0"}},"id":0}
                        """)
                .post("/mcp");

        assertThat(response.statusCode()).isEqualTo(200);
    }
}
