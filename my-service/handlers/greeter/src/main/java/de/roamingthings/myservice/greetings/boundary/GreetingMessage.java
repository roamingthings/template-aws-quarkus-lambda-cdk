package de.roamingthings.myservice.greetings.boundary;

import de.roamingthings.myservice.json.control.JsonFields;
import jakarta.json.Json;
import jakarta.json.JsonObject;

public record GreetingMessage(String message) {

    public JsonObject toJson() {
        var builder = Json.createObjectBuilder();
        JsonFields.addString(builder, "message", this.message);
        return builder.build();
    }

    public static GreetingMessage fromJson(JsonObject json) {
        var message1 = json.getString("message");
        return new GreetingMessage(message1);
    }
}
