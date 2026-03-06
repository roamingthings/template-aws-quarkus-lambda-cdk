package de.roamingthings.myservice.greetings.boundary;

import de.roamingthings.myservice.greetings.control.Greeter;
import de.roamingthings.shared.model.entity.Item;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/greet")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class GreeterResource {

    Greeter greeter;

    public GreeterResource(Greeter greeter) {
        this.greeter = greeter;
    }

    @POST
    public GreetingMessage greet(Item input) {
        this.greeter.greetings();
        return new GreetingMessage(this.greeter.greetings(input.name()));
    }
}
