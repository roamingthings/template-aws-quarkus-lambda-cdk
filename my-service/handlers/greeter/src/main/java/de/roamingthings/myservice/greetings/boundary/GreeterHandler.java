package de.roamingthings.myservice.greetings.boundary;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import de.roamingthings.myservice.greetings.Requirement;
import de.roamingthings.myservice.greetings.control.Greeter;
import de.roamingthings.shared.model.entity.Item;

import static de.roamingthings.myservice.greetings.Requirement.Rn.R1_1;
import static de.roamingthings.myservice.greetings.Requirement.Rn.R1_2;

public class GreeterHandler implements RequestHandler<Item, GreetingMessage> {

    Greeter greeter;

    public GreeterHandler(Greeter greeter) {
        this.greeter = greeter;
    }

    @Override
    @Requirement({R1_1, R1_2})
    public GreetingMessage handleRequest(Item input, Context context) {
        this.greeter.greetings(input.name());
        return new GreetingMessage(this.greeter.greetings());
    }
}
