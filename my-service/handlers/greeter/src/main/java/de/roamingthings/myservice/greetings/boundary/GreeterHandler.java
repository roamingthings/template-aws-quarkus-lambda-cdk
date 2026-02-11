package de.roamingthings.myservice.greetings.boundary;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import de.roamingthings.myservice.greetings.control.Greeter;
import de.roamingthings.shared.model.entity.Item;

public class GreeterHandler implements RequestHandler<Item, GreetingMessage> {

    Greeter greeter;

    public GreeterHandler(Greeter greeter) {
        this.greeter = greeter;
    }

    @Override
    public GreetingMessage handleRequest(Item input, Context context) {
        this.greeter.greetings(input.name());
        return new GreetingMessage(this.greeter.greetings());
    }
}
