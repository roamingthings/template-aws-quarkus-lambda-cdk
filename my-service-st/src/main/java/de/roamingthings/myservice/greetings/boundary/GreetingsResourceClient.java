package de.roamingthings.myservice.greetings.boundary;

import de.roamingthings.myservice.greetings.entity.GreetingMessage;
import de.roamingthings.myservice.greetings.entity.Item;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("greet")
@RegisterRestClient(configKey = "myservice")
public interface GreetingsResourceClient {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    GreetingMessage greet(Item input);

}
