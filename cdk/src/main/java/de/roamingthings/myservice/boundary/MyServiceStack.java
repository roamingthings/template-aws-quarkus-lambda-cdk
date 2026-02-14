package de.roamingthings.myservice.boundary;

import de.roamingthings.ConventionalDefaults;
import de.roamingthings.compliance.control.ComplianceStackAspect;
import de.roamingthings.encryption.control.ApplicationEncryptionKey;
import de.roamingthings.function.control.QuarkusFunction;
import software.amazon.awscdk.Aspects;
import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.apigateway.AccessLogFormat;
import software.amazon.awscdk.services.apigateway.LambdaRestApi;
import software.amazon.awscdk.services.apigateway.LogGroupLogDestination;
import software.amazon.awscdk.services.apigateway.StageOptions;
import software.amazon.awscdk.services.kms.IKey;
import software.amazon.awscdk.services.lambda.Alias;
import software.amazon.awscdk.services.lambda.ApplicationLogLevel;
import software.amazon.awscdk.services.lambda.IFunction;
import software.amazon.awscdk.services.lambda.SystemLogLevel;
import software.amazon.awscdk.services.logs.LogGroup;
import software.amazon.awscdk.services.logs.RetentionDays;
import software.constructs.Construct;

import java.util.List;
import java.util.Objects;

import static java.lang.Boolean.TRUE;

public class MyServiceStack extends Stack {

    public MyServiceStack(Construct scope, String id, MyServiceStackProps props) {
        super(scope, id, props.stackProps);

        var encryptionKey = (TRUE.equals(props.encryptWithCmk)) ?
                ApplicationEncryptionKey.create(this, "ApplicationEncryptionKey", List.of()) : null;

        var greeterHandlerFunction = createGreeterHandler(props.appName, encryptionKey);
        var greeterApiHandlerFunction = createGreeterApiHandler(props.appName, encryptionKey);
        var greeterHandlerLiveAlias = greeterHandlerFunction.getLiveAlias();
        var greeterApiHandlerLiveAlias = greeterApiHandlerFunction.getLiveAlias();

        var api = createApiGateway(greeterApiHandlerLiveAlias, props.appName);
        createStackOutputs(greeterHandlerLiveAlias, greeterApiHandlerLiveAlias, api);

        Aspects.of(this).add(new ComplianceStackAspect());
    }

    private QuarkusFunction createGreeterHandler(String appName, IKey encryptionKey) {
        return new QuarkusFunction(
                this,
                "GreeterHandlerFunction",
                QuarkusFunction.QuarkusLambdaFunctionProps.builder()
                        .functionName(ConventionalDefaults.resourceName(appName, "GreeterHandler"))
                        .description("A sample handler that greets")
                        .modulePath("../my-service/handlers/greeter")
                        .buildTool(QuarkusFunction.BuildTool.GRADLE)
                        .applicationLogLevel(ApplicationLogLevel.DEBUG)
                        .systemLogLevel(SystemLogLevel.INFO)
                        .encryptionKey(encryptionKey)
                        .build());
    }

    private QuarkusFunction createGreeterApiHandler(String appName, IKey encryptionKey) {
        return new QuarkusFunction(
                this,
                "GreeterApiHandlerFunction",
                QuarkusFunction.QuarkusLambdaFunctionProps.builder()
                        .functionName(ConventionalDefaults.resourceName(appName, "GreeterApiHandler"))
                        .description("A sample API handler that greets")
                        .modulePath("../my-service/handlers/greeter-api")
                        .buildTool(QuarkusFunction.BuildTool.GRADLE)
                        .applicationLogLevel(ApplicationLogLevel.DEBUG)
                        .systemLogLevel(SystemLogLevel.INFO)
                        .encryptionKey(encryptionKey)
                        .build());
    }

    LambdaRestApi createApiGateway(Alias liveAlias, String appName) {
        var apiAccessLogs = LogGroup.Builder.create(this, "ApiGatewayAccessLogs")
                .retention(RetentionDays.ONE_WEEK)
                .build();

        return LambdaRestApi.Builder.create(this, "MyServiceGreeterApi")
                .handler(liveAlias)
                .restApiName(ConventionalDefaults.resourceName(appName, "Api"))
                .proxy(true)
                .deployOptions(StageOptions.builder()
                        .accessLogDestination(new LogGroupLogDestination(apiAccessLogs))
                        .accessLogFormat(AccessLogFormat.jsonWithStandardFields())
                        .build())
                .build();
    }

    private void createStackOutputs(IFunction greeterHandlerFunction, IFunction greeterApiHandlerFunction, LambdaRestApi api) {
        CfnOutput.Builder.create(this, "GreeterHandlerFunctionArn")
                .value(greeterHandlerFunction.getFunctionArn())
                .build();
        CfnOutput.Builder.create(this, "GreeterApiHandlerFunctionArn")
                .value(greeterApiHandlerFunction.getFunctionArn())
                .build();
        CfnOutput.Builder.create(this, "ApiGatewayUrl")
                .value(api.getUrl())
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
