package de.roamingthings.myservice.boundary;

import de.roamingthings.ConventionalDefaults;
import de.roamingthings.function.control.QuarkusFunction;
import org.jspecify.annotations.Nullable;
import software.amazon.awscdk.services.apigateway.AccessLogFormat;
import software.amazon.awscdk.services.apigateway.AuthorizationType;
import software.amazon.awscdk.services.apigateway.CognitoUserPoolsAuthorizer;
import software.amazon.awscdk.services.apigateway.GatewayResponseOptions;
import software.amazon.awscdk.services.apigateway.LambdaRestApi;
import software.amazon.awscdk.services.apigateway.LogGroupLogDestination;
import software.amazon.awscdk.services.apigateway.MethodOptions;
import software.amazon.awscdk.services.apigateway.ResponseType;
import software.amazon.awscdk.services.apigateway.StageOptions;
import software.amazon.awscdk.services.cognito.IUserPool;
import software.amazon.awscdk.services.cognito.OAuthScope;
import software.amazon.awscdk.services.cognito.UserPoolClient;
import software.amazon.awscdk.services.kms.IKey;
import software.amazon.awscdk.services.lambda.Alias;
import software.amazon.awscdk.services.lambda.ApplicationLogLevel;
import software.amazon.awscdk.services.lambda.SystemLogLevel;
import software.amazon.awscdk.services.logs.LogGroup;
import software.amazon.awscdk.services.logs.RetentionDays;
import software.constructs.Construct;

import java.util.List;
import java.util.Map;

class McpApiConstruct extends Construct {

    LambdaRestApi api;
    Alias apiHandlerLiveAlias;

    McpApiConstruct(Construct scope, String id, McpApiConstructProps props) {
        super(scope, id);

        var mcpHandlerFunction = new QuarkusFunction(
                this,
                "McpHandlerFunction",
                QuarkusFunction.QuarkusLambdaFunctionProps.builder()
                        .functionName(ConventionalDefaults.resourceName(props.appName, "McpHandler"))
                        .description("MCP server handler")
                        .modulePath("../my-service/handlers/mcp")
                        .buildTool(QuarkusFunction.BuildTool.GRADLE)
                        .applicationLogLevel(ApplicationLogLevel.DEBUG)
                        .systemLogLevel(SystemLogLevel.INFO)
                        .encryptionKey(props.encryptionKey)
                        .build());

        apiHandlerLiveAlias = mcpHandlerFunction.getLiveAlias();

        var authorizer = CognitoUserPoolsAuthorizer.Builder.create(this, "CognitoAuthorizer")
                .cognitoUserPools(List.of(props.userPool))
                .authorizerName("CognitoAuthorizer")
                .build();

        var apiAccessLogs = LogGroup.Builder.create(this, "McpApiGatewayAccessLogs")
                .retention(RetentionDays.ONE_WEEK)
                .build();

        api = LambdaRestApi.Builder.create(this, "McpApi")
                .handler(apiHandlerLiveAlias)
                .restApiName(ConventionalDefaults.resourceName(props.appName, "McpApi"))
                .proxy(true)
                .defaultMethodOptions(MethodOptions.builder()
                        .authorizationType(AuthorizationType.COGNITO)
                        .authorizer(authorizer)
                        .authorizationScopes(List.of(props.mcpScope.getScopeName()))
                        .build())
                .deployOptions(StageOptions.builder()
                        .accessLogDestination(new LogGroupLogDestination(apiAccessLogs))
                        .accessLogFormat(AccessLogFormat.jsonWithStandardFields())
                        .build())
                .build();

        configureUnauthorizedGatewayResponse(props.cloudFrontDomainName);
    }

    private void configureUnauthorizedGatewayResponse(String cloudFrontDomainName) {
        var resourceMetadataUrl = "https://" + cloudFrontDomainName
                + "/.well-known/oauth-protected-resource";
        var headerValue = "'Bearer resource_metadata=\"" + resourceMetadataUrl + "\"'";
        api.addGatewayResponse("UnauthorizedMcpResponse",
                GatewayResponseOptions.builder()
                        .type(ResponseType.UNAUTHORIZED)
                        .responseHeaders(Map.of("WWW-Authenticate", headerValue))
                        .build());
    }

    LambdaRestApi api() {
        return api;
    }

    Alias apiHandlerLiveAlias() {
        return apiHandlerLiveAlias;
    }

    record McpApiConstructProps(
            String appName,
            @Nullable IKey encryptionKey,
            IUserPool userPool,
            UserPoolClient userPoolClient,
            OAuthScope mcpScope,
            String cloudFrontDomainName
    ) {
    }
}
