package de.roamingthings.myservice.greetings.boundary;

import de.roamingthings.myservice.greetings.Requirement;
import de.roamingthings.shared.model.entity.Item;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static de.roamingthings.myservice.greetings.Requirement.Rn.R1_2;
import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@TestProfile(GreeterHandlerCustomGreetingTest.CustomGreeting.class)
class GreeterHandlerCustomGreetingTest {

    public static class CustomGreeting implements QuarkusTestProfile {

        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.of("message", "Bonjour, Duke");
        }
    }

    @Test
    @Requirement(R1_2)
    void greetWithCustomGreeting() {
        var message = GreeterHandlerTest.greet(new Item("72fee59e-3812-4dd6-be49-6ab638ee5a5e", "Duke", "Java mascot"));

        assertThat(message).as(R1_2 + " — " + R1_2.statement()).isEqualTo("Bonjour, Duke");
    }
}
