package de.roamingthings.myservice.json.control;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import java.util.List;
import java.util.function.Function;

public interface JsonFields {

    static String stringValue(JsonObject json, String key) {
        if (!json.containsKey(key) || json.isNull(key)) {
            return null;
        }
        var value = json.get(key);
        if (value.getValueType() == JsonValue.ValueType.STRING) {
            return ((JsonString) value).getString();
        }
        if (value.getValueType() == JsonValue.ValueType.NUMBER) {
            return ((JsonNumber) value).toString();
        }
        return value.toString();
    }

    static Integer integerValue(JsonObject json, String key) {
        var number = numberValue(json, key);
        if (number == null) {
            return null;
        }
        return number.intValue();
    }

    static Long longValue(JsonObject json, String key) {
        var number = numberValue(json, key);
        if (number == null) {
            return null;
        }
        return number.longValue();
    }

    static Double doubleValue(JsonObject json, String key) {
        var number = numberValue(json, key);
        if (number == null) {
            return null;
        }
        return number.doubleValue();
    }

    static Boolean booleanValue(JsonObject json, String key) {
        if (!json.containsKey(key) || json.isNull(key)) {
            return null;
        }
        var value = json.get(key);
        if (value == JsonValue.TRUE) {
            return Boolean.TRUE;
        }
        if (value == JsonValue.FALSE) {
            return Boolean.FALSE;
        }
        return null;
    }

    static List<String> stringListValue(JsonObject json, String key) {
        if (!json.containsKey(key) || json.isNull(key)) {
            return List.of();
        }
        var array = json.getJsonArray(key);
        if (array == null) {
            return List.of();
        }
        return array.stream()
                .map(JsonFields::stringFromValue)
                .toList();
    }

    static JsonObjectBuilder addString(JsonObjectBuilder builder, String key, String value) {
        if (value == null) {
            return builder;
        }
        return builder.add(key, value);
    }

    static JsonObjectBuilder addInteger(JsonObjectBuilder builder, String key, Integer value) {
        if (value == null) {
            return builder;
        }
        return builder.add(key, value);
    }

    static JsonObjectBuilder addLong(JsonObjectBuilder builder, String key, Long value) {
        if (value == null) {
            return builder;
        }
        return builder.add(key, value);
    }

    static JsonObjectBuilder addDouble(JsonObjectBuilder builder, String key, Double value) {
        if (value == null) {
            return builder;
        }
        return builder.add(key, value);
    }

    static JsonObjectBuilder addBoolean(JsonObjectBuilder builder, String key, Boolean value) {
        if (value == null) {
            return builder;
        }
        return builder.add(key, value);
    }

    static JsonObjectBuilder addStringArray(JsonObjectBuilder builder, String key, List<String> values) {
        if (values == null) {
            return builder;
        }
        var arrayBuilder = Json.createArrayBuilder();
        values.stream().reduce(
                arrayBuilder,
                (current, value) -> current.add(value),
                (left, right) -> left
        );
        return builder.add(key, arrayBuilder);
    }

    static <T> List<T> listValue(JsonObject json, String key, Function<JsonObject, T> mapper) {
        if (!json.containsKey(key) || json.isNull(key)) {
            return List.of();
        }
        var array = json.getJsonArray(key);
        if (array == null) {
            return List.of();
        }
        return array.getValuesAs(JsonObject.class)
                .stream()
                .map(mapper)
                .toList();
    }

    static <T> JsonArrayBuilder addObjectArray(List<T> values, Function<T, JsonObject> mapper) {
        var builder = Json.createArrayBuilder();
        if (values == null) {
            return builder;
        }
        values.stream()
                .map(mapper)
                .reduce(builder, (current, value) -> current.add(value), (left, right) -> left);
        return builder;
    }

    static JsonObjectBuilder addArray(JsonObjectBuilder builder, String key, JsonValue values) {
        if (values == null) {
            return builder;
        }
        return builder.add(key, values);
    }

    static JsonNumber numberValue(JsonObject json, String key) {
        if (!json.containsKey(key) || json.isNull(key)) {
            return null;
        }
        return json.getJsonNumber(key);
    }

    private static String stringFromValue(JsonValue value) {
        if (value == null) {
            return null;
        }
        if (value.getValueType() == JsonValue.ValueType.STRING) {
            return ((JsonString) value).getString();
        }
        if (value.getValueType() == JsonValue.ValueType.NUMBER) {
            return ((JsonNumber) value).toString();
        }
        return value.toString();
    }
}
