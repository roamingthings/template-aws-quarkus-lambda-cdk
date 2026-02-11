package de.roamingthings.myservice.greetings.boundary;

import de.roamingthings.shared.model.entity.Item;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;

@QuarkusTest
class GreeterHandlerTest {

    @Test
    void testSimpleLambdaSuccess() {
        // you test your lambdas by invoking on http://localhost:8081
        // this works in dev mode too

        var in = new Item("72fee59e-3812-4dd6-be49-6ab638ee5a5e", "Test Item", "Test Description");
        given()
                .contentType("application/json")
                .accept("application/json")
                .body(in)
                .when()
                .post()
                .then()
                .statusCode(200)
                .body(containsString("{\"message\":\"hello, Quarkus on BCE\"}"));
    }
}