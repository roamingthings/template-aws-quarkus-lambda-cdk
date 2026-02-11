package de.roamingthings.myservice.boundary;

import de.roamingthings.ConventionalDefaults;
import de.roamingthings.compliance.control.ComplianceStackAspect;
import de.roamingthings.encryption.control.ApplicationEncryptionKey;
import de.roamingthings.function.control.QuarkusFunction;
import software.amazon.awscdk.Aspects;
import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.lambda.Alias;
import software.amazon.awscdk.services.lambda.ApplicationLogLevel;
import software.amazon.awscdk.services.lambda.SystemLogLevel;
import software.constructs.Construct;

import java.util.List;
import java.util.Objects;

import static java.lang.Boolean.TRUE;

public class MyServiceStack extends Stack {

    public MyServiceStack(Construct scope, String id, MyServiceStackProps props) {
        super(scope, id, props.stackProps);

        var encryptionKey = (TRUE.equals(props.encryptWithCmk)) ?
                ApplicationEncryptionKey.create(this, "ApplicationEncryptionKey", List.of()) : null;

        var greeterHandlerFunctionName = ConventionalDefaults.resourceName(props.appName, "GreeterHandler");

        var itemFunction = new QuarkusFunction(
                this,
                "GreeterHandlerFunction",
                QuarkusFunction.QuarkusLambdaFunctionProps.builder()
                        .functionName(greeterHandlerFunctionName)
                        .description("A sample handler that greets")
                        .modulePath("../my-service/handlers/greeter")
                        .buildTool(QuarkusFunction.BuildTool.GRADLE)
                        .applicationLogLevel(ApplicationLogLevel.INFO)
                        .systemLogLevel(SystemLogLevel.DEBUG)
                        .encryptionKey(encryptionKey)
                        .build());
        var liveAlias = itemFunction.getLiveAlias();

        createStackOutputs(liveAlias);

        Aspects.of(this).add(new ComplianceStackAspect());
    }

    private void createStackOutputs(Alias liveAlias) {
        CfnOutput.Builder.create(this, "GreeterHandlerFunctionArn")
                .value(liveAlias.getFunctionArn())
                .build();
    }

    public record MyServiceStackProps(
            String appName,
            StackProps stackProps,
            Boolean encryptWithCmk
    ) {
        public MyServiceStackProps {
            Objects.requireNonNull(appName, "appName is required");
            Objects.requireNonNull(stackProps, "stackProps is required");
        }
    }
}
