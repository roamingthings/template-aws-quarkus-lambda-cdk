package de.roamingthings;

import de.roamingthings.mylambda.boundary.MyLambdaStack;
import software.amazon.awscdk.Annotations;
import software.amazon.awscdk.App;
import software.amazon.awscdk.Tags;

public interface CdkApp {

    String APP_NAME = "my-service";

    static void main(String... args) {
        var app = new App();

        var configuration = Configuration.ofAppNamed(APP_NAME);
        var stackProps = configuration.stackProperties();

        new MyLambdaStack(app, "MyLambdaStack", new MyLambdaStack.MyLambdaStackProps(
                APP_NAME,
                stackProps,
                configuration.encryptWithCmk()
        ));

        Annotations.of(app).acknowledgeWarning("@aws-cdk/aws-lambda:snapStartRequirePublish");
        Tags.of(app).add("project", "template-aws-quarkus-lambda-cdk");
        Tags.of(app).add("environment", "templates");
        Tags.of(app).add("application", APP_NAME);
        var awsApplicationTagValue = configuration.awsApplicationTagValue();
        if (awsApplicationTagValue != null) {
            Tags.of(app).add("awsApplication", awsApplicationTagValue);
        }
        app.synth();
    }
}
