package de.roamingthings.myservice.systemtest.greetings.boundary;

import de.roamingthings.myservice.greetings.boundary.GreetingsResourceClient;
import de.roamingthings.myservice.greetings.entity.Item;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
class GreetingsResourceIT {

    @Inject
    @RestClient
    GreetingsResourceClient rut;

    @Inject
    @ConfigProperty(name = "quarkus.rest-client.myservice.url")
    String baseURI;


    @Test
    void hello() {
        var item = new Item("72fee59e-3812-4dd6-be49-6ab638ee5a5e", "Test Item", "Test Description");

        var message = this.rut.greet(item);
        assertNotNull(message);
        IO.println("baseURI: %s message: %s".formatted(baseURI, message));
    }
}
