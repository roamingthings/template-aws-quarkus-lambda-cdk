package de.roamingthings.myservice.greetings.control;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import static java.lang.System.Logger.Level.INFO;

@ApplicationScoped
public class Greeter {

    static System.Logger LOG = System.getLogger(Greeter.class.getName());

    @Inject
    @ConfigProperty(defaultValue = "hello, %s Quarkus on BCE", name="message")
    String message;

    public String greetings() {
        LOG.log(INFO, "returning: " + this.message);
        return this.message;
    }

    public String greetings(String userMessage) {
        LOG.log(INFO, "received: " + userMessage);
        return message.formatted(userMessage);
    }
}
