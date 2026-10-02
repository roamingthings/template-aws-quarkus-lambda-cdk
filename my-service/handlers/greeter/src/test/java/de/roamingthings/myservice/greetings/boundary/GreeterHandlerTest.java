package de.roamingthings.myservice.greetings.boundary;

import de.roamingthings.myservice.greetings.Requirement;
import de.roamingthings.shared.model.entity.Item;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static de.roamingthings.myservice.greetings.Requirement.Rn.R1_1;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
class GreeterHandlerTest {

    @Test
    @Requirement(R1_1)
    void greetWithDefaultGreeting() {
        var message = greet(new Item("72fee59e-3812-4dd6-be49-6ab638ee5a5e", "Duke", "Java mascot"));

        assertThat(message).as(R1_1 + " — " + R1_1.statement()).isEqualTo("hello, Quarkus on BCE");
    }

    static String greet(Item item) {
        return given()
                .contentType("application/json")
                .accept("application/json")
                .body(item)
                .when()
                .post()
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getString("message");
    }
}
