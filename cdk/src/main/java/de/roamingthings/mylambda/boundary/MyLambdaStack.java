package de.roamingthings.mylambda.boundary;

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

public class MyLambdaStack extends Stack {

    public MyLambdaStack(Construct scope, String id, MyLambdaStackProps props) {
        super(scope, id, props.stackProps);

        var encryptionKey = (TRUE.equals(props.encryptWithCmk)) ?
                ApplicationEncryptionKey.create(this, "ApplicationEncryptionKey", List.of()) : null;

        var itemHandlerFunctionName = ConventionalDefaults.resourceName(props.appName, "ItemHandler");

        var itemFunction = new QuarkusFunction(
                this,
                "ItemHandlerFunction",
                QuarkusFunction.QuarkusLambdaFunctionProps.builder()
                        .functionName(itemHandlerFunctionName)
                        .description("A sample handler for sample items")
                        .modulePath("../my-lambda/handlers/item")
                        .buildTool(QuarkusFunction.BuildTool.MAVEN)
                        .applicationLogLevel(ApplicationLogLevel.INFO)
                        .systemLogLevel(SystemLogLevel.DEBUG)
                        .encryptionKey(encryptionKey)
                        .build());
        var liveAlias = itemFunction.getLiveAlias();

        createStackOutputs(liveAlias);

        Aspects.of(this).add(new ComplianceStackAspect());
    }

    private void createStackOutputs(Alias liveAlias) {
        CfnOutput.Builder.create(this, "ItemHandlerFunctionArn")
                .value(liveAlias.getFunctionArn())
                .build();
    }

    public record MyLambdaStackProps(
            String appName,
            StackProps stackProps,
            Boolean encryptWithCmk
    ) {
        public MyLambdaStackProps {
            Objects.requireNonNull(appName, "appName is required");
            Objects.requireNonNull(stackProps, "stackProps is required");
        }
    }
}
