package de.roamingthings;

import de.roamingthings.configuration.control.ZCfg;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.StackProps;

public record Configuration(String appName, String region, String awsApplicationTagValue, Boolean encryptWithCmk) {

    public static Configuration ofAppNamed(String appName) {
        ZCfg.load(appName);
        var region = ZCfg.string("stack.props.region");
        var awsApplicationTagValue = ZCfg.string("app.awsApplicationTagValue");
        var encryptWithCmk = ZCfg.bool("app.encryptWithCmk", false);
        return new Configuration(
                appName,
                region,
                awsApplicationTagValue,
                encryptWithCmk
        );
    }

    public StackProps stackProperties() {
        var env = Environment
                .builder()
                .region(region)
                .build();
        return StackProps
                .builder()
                .env(env)
                .build();
    }
}
