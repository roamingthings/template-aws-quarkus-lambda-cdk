package de.roamingthings.myservice.mcp.boundary;

import de.roamingthings.myservice.mcp.Requirement;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;

import static de.roamingthings.myservice.mcp.Requirement.Rn.R2_1;
import static de.roamingthings.myservice.mcp.Requirement.Rn.R2_2;
import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@TestSecurity(user = "test-user", roles = {})
class McpSseBlockingFilterIT {

    @Test
    @Requirement(R2_1)
    void getMcpReturns405() {
        var response = RestAssured.given()
                .accept("text/event-stream")
                .get("/mcp");

        assertThat(response.statusCode()).as(R2_1 + " — " + R2_1.statement()).isEqualTo(405);
        assertThat(response.header("Allow")).as(R2_1 + " — " + R2_1.statement()).isEqualTo("POST, DELETE");
    }

    @Test
    @Requirement(R2_2)
    void postMcpIsNotBlocked() {
        var response = RestAssured.given()
                .contentType("application/json")
                .accept("application/json, text/event-stream")
                .body("""
                        {"jsonrpc":"2.0","method":"initialize","params":{"protocolVersion":"2025-03-26","capabilities":{},"clientInfo":{"name":"test","version":"1.0"}},"id":0}
                        """)
                .post("/mcp");

        assertThat(response.statusCode()).as(R2_2 + " — " + R2_2.statement()).isEqualTo(200);
    }
}
