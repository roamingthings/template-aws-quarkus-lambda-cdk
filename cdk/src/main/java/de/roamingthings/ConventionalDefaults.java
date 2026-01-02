package de.roamingthings;

import java.nio.file.Path;
import java.nio.file.Paths;

public interface ConventionalDefaults {

    Path dockerFilePath = Paths.get("..", "mcp").toAbsolutePath();

    static String stackName(String appName, String stackName) {
        return "%s-%s-stack".formatted(appName, stackName);
    }

    static String resourceName(String appName, String resourceName) {
        return "%s-%s".formatted(appName, resourceName);
    }

    static String runtimeName(String appName, String resourceName) {
        return "%s-%s".formatted(appName, resourceName).replace("-", "_");
    }
}
