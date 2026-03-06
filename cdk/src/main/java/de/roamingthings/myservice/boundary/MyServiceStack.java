package de.roamingthings.myservice.boundary;

import de.roamingthings.ConventionalDefaults;
import de.roamingthings.compliance.control.ComplianceStackAspect;
import de.roamingthings.encryption.control.ApplicationEncryptionKey;
import de.roamingthings.function.control.QuarkusFunction;
import software.amazon.awscdk.Aspects;
import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.apigateway.AccessLogFormat;
import software.amazon.awscdk.services.apigateway.LambdaRestApi;
import software.amazon.awscdk.services.apigateway.LogGroupLogDestination;
import software.amazon.awscdk.services.apigateway.StageOptions;
import software.amazon.awscdk.services.cloudfront.AddBehaviorOptions;
import software.amazon.awscdk.services.cloudfront.AllowedMethods;
import software.amazon.awscdk.services.cloudfront.CachePolicy;
import software.amazon.awscdk.services.cloudfront.Distribution;
import software.amazon.awscdk.services.cloudfront.OriginRequestPolicy;
import software.amazon.awscdk.services.cloudfront.origins.HttpOrigin;
import software.amazon.awscdk.services.cognito.AutoVerifiedAttrs;
import software.amazon.awscdk.services.cognito.CfnUserPoolUser;
import software.amazon.awscdk.services.cognito.Mfa;
import software.amazon.awscdk.services.cognito.OAuthFlows;
import software.amazon.awscdk.services.cognito.OAuthScope;
import software.amazon.awscdk.services.cognito.OAuthSettings;
import software.amazon.awscdk.services.cognito.ResourceServerScope;
import software.amazon.awscdk.services.cognito.ResourceServerScopeProps;
import software.amazon.awscdk.services.cognito.SignInAliases;
import software.amazon.awscdk.services.cognito.UserPool;
import software.amazon.awscdk.services.cognito.UserPoolClient;
import software.amazon.awscdk.services.cognito.UserPoolClientOptions;
import software.amazon.awscdk.services.cognito.UserPoolDomain;
import software.amazon.awscdk.services.cognito.UserPoolResourceServer;
import software.amazon.awscdk.services.kms.IKey;
import software.amazon.awscdk.services.lambda.Alias;
import software.amazon.awscdk.services.lambda.ApplicationLogLevel;
import software.amazon.awscdk.services.lambda.IFunction;
import software.amazon.awscdk.services.lambda.SystemLogLevel;
import software.amazon.awscdk.services.logs.LogGroup;
import software.amazon.awscdk.services.logs.RetentionDays;
import software.amazon.awscdk.services.s3.deployment.BucketDeployment;
import software.amazon.awscdk.services.s3.deployment.Source;
import software.constructs.Construct;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.lang.Boolean.TRUE;

public class MyServiceStack extends Stack {

    public static final List<String> MCP_CALLBACK_URLS = List.of(
            "http://localhost:9876/callback",
            "https://claude.ai/api/mcp/auth_callback",
            "https://chatgpt.com/connector_platform_oauth_redirect"
    );

    public MyServiceStack(Construct scope, String id, MyServiceStackProps props) {
        super(scope, id, props.stackProps);

        var encryptionKey = (TRUE.equals(props.encryptWithCmk)) ?
                ApplicationEncryptionKey.create(this, "ApplicationEncryptionKey", List.of()) : null;

        var greeterHandlerFunction = createGreeterHandler(props.appName, encryptionKey);
        var greeterApiHandlerFunction = createGreeterApiHandler(props.appName, encryptionKey);
        var greeterHandlerLiveAlias = greeterHandlerFunction.getLiveAlias();
        var greeterApiHandlerLiveAlias = greeterApiHandlerFunction.getLiveAlias();

        var api = createApiGateway(greeterApiHandlerLiveAlias, props.appName);

        var frontend = new OAuthFrontendConstruct(this, "OAuthFrontend");
        var cloudFrontDomainName = frontend.distribution().getDistributionDomainName();

        var userPool = createUserPool(props.appName);
        createTestUser(userPool);
        var mcpScopes = createMcpResourceServerScopes(userPool, cloudFrontDomainName, props.appName);
        var agentClient = createAgentUserPoolClient(userPool, mcpScopes, props.appName);
        var userPoolDomain = createUserPoolDomain(userPool, props.appName);

        var mcpApi = new McpApiConstruct(this, "McpApi", new McpApiConstruct.McpApiConstructProps(
                props.appName,
                encryptionKey,
                userPool,
                agentClient,
                mcpScopes,
                cloudFrontDomainName
        ));

        addApiGatewayBehavior(frontend.distribution(), api);
        addMcpApiGatewayBehavior(frontend.distribution(), mcpApi.api());
        deployOAuthServerMetadata(userPool, frontend, cloudFrontDomainName);

        createStackOutputs(greeterHandlerLiveAlias, greeterApiHandlerLiveAlias, api,
                userPool, agentClient, frontend, userPoolDomain, mcpApi);

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

    UserPool createUserPool(String appName) {
        return UserPool.Builder.create(this, "UserPool")
                .userPoolName(ConventionalDefaults.resourceName(appName, "UserPool"))
                .selfSignUpEnabled(false)
                .signInAliases(SignInAliases.builder().email(true).build())
                .autoVerify(AutoVerifiedAttrs.builder().email(true).build())
                .mfa(Mfa.OPTIONAL)
                .removalPolicy(RemovalPolicy.DESTROY)
                .build();
    }

    List<OAuthScope> createMcpResourceServerScopes(UserPool userPool, String cloudFrontDomainName, String appName) {
        // Agents uses this scope to connect to the MCP Server
        var connectScope = new ResourceServerScope(ResourceServerScopeProps.builder()
                .scopeName("connect")
                .scopeDescription("MCP server access")
                .build()
        );
        var resourceServerIdentifier = "https://" + cloudFrontDomainName;
        var mcpResourceServer = UserPoolResourceServer.Builder.create(this, "McpResourceServer")
                .userPoolResourceServerName(appName + "McpResource")
                .identifier(resourceServerIdentifier)
                .userPool(userPool)
                .scopes(List.of(connectScope))
                .build();
        return List.of(OAuthScope.resourceServer(mcpResourceServer, connectScope));
    }

    UserPoolClient createAgentUserPoolClient(UserPool userPool, List<OAuthScope> mcpScopes, String appName) {
        var scopes = new ArrayList<>(List.of(
                OAuthScope.OPENID,
                OAuthScope.PROFILE,
                OAuthScope.EMAIL
        ));
        scopes.addAll(mcpScopes);
        return userPool.addClient("AgentUserPoolClient", UserPoolClientOptions.builder()
                .userPoolClientName(ConventionalDefaults.resourceName(appName, "AgentUserPoolClient"))
                .generateSecret(false)
                .oAuth(OAuthSettings.builder()
                        .flows(OAuthFlows.builder().authorizationCodeGrant(true).build())
                        .scopes(scopes)
                        .callbackUrls(MCP_CALLBACK_URLS)
                        .build())
                .accessTokenValidity(Duration.hours(1))
                .refreshTokenValidity(Duration.days(30))
                .build());
    }

    void createTestUser(UserPool userPool) {
        CfnUserPoolUser.Builder.create(this, "TestUser")
                .userPoolId(userPool.getUserPoolId())
                .username("demo@example.com")
                .userAttributes(List.of(
                        Map.of("name", "email", "value", "demo@example.com"),
                        Map.of("name", "email_verified", "value", "true")
                ))
                .messageAction("SUPPRESS")
                .build();
    }

    UserPoolDomain createUserPoolDomain(UserPool userPool, String appName) {
        return userPool.addDomain("CognitoDomain", software.amazon.awscdk.services.cognito.UserPoolDomainOptions.builder()
                .cognitoDomain(software.amazon.awscdk.services.cognito.CognitoDomainOptions.builder()
                        .domainPrefix(appName)
                        .build())
                .build());
    }

    void addApiGatewayBehavior(Distribution distribution, LambdaRestApi api) {
        var apiDomain = api.getRestApiId() + ".execute-api." + this.getRegion() + ".amazonaws.com";
        var apiOrigin = HttpOrigin.Builder.create(apiDomain)
                .originPath("/prod")
                .build();
        var apiBehaviorOptions = AddBehaviorOptions.builder()
                .allowedMethods(AllowedMethods.ALLOW_ALL)
                .cachePolicy(CachePolicy.CACHING_DISABLED)
                .originRequestPolicy(OriginRequestPolicy.ALL_VIEWER_EXCEPT_HOST_HEADER)
                .build();
        distribution.addBehavior("/api/*", apiOrigin, apiBehaviorOptions);
    }

    void addMcpApiGatewayBehavior(Distribution distribution, LambdaRestApi mcpApi) {
        var apiDomain = mcpApi.getRestApiId() + ".execute-api." + this.getRegion() + ".amazonaws.com";
        var apiOrigin = HttpOrigin.Builder.create(apiDomain)
                .originPath("/prod")
                .build();
        var apiBehaviorOptions = AddBehaviorOptions.builder()
                .allowedMethods(AllowedMethods.ALLOW_ALL)
                .cachePolicy(CachePolicy.CACHING_DISABLED)
                .originRequestPolicy(OriginRequestPolicy.ALL_VIEWER_EXCEPT_HOST_HEADER)
                .build();
        distribution.addBehavior("/mcp*", apiOrigin, apiBehaviorOptions);
    }

    void deployOAuthServerMetadata(UserPool userPool, OAuthFrontendConstruct frontend, String cloudFrontDomainName) {
        var issuerUrl = "https://cognito-idp." + this.getRegion() + ".amazonaws.com/" + userPool.getUserPoolId();
        var resourceUrl = "https://" + cloudFrontDomainName;
        var metadata = Map.<String, Object>of(
                "resource", resourceUrl,
                "authorization_servers", List.of(issuerUrl),
                "scopes_supported", List.of(resourceUrl + "/connect")
        );
        BucketDeployment.Builder.create(this, "OAuthServerMetadata")
                .sources(List.of(Source.jsonData("oauth-protected-resource", metadata)))
                .destinationBucket(frontend.bucket())
                .destinationKeyPrefix(".well-known")
                .prune(false)
                .build();
    }

    private void createStackOutputs(IFunction greeterHandlerFunction, IFunction greeterApiHandlerFunction,
                                    LambdaRestApi api, UserPool userPool, UserPoolClient agentClient,
                                    OAuthFrontendConstruct frontend, UserPoolDomain userPoolDomain,
                                    McpApiConstruct mcpApi) {
        CfnOutput.Builder.create(this, "GreeterHandlerFunctionArn")
                .value(greeterHandlerFunction.getFunctionArn())
                .build();
        CfnOutput.Builder.create(this, "GreeterApiHandlerFunctionArn")
                .value(greeterApiHandlerFunction.getFunctionArn())
                .build();
        CfnOutput.Builder.create(this, "ApiGatewayUrl")
                .value(api.getUrl())
                .build();
        CfnOutput.Builder.create(this, "UserPoolId")
                .value(userPool.getUserPoolId())
                .build();
        CfnOutput.Builder.create(this, "AgentUserPoolClientId")
                .value(agentClient.getUserPoolClientId())
                .build();
        CfnOutput.Builder.create(this, "CloudFrontUrl")
                .value("https://" + frontend.distribution().getDistributionDomainName())
                .build();
        CfnOutput.Builder.create(this, "CloudFrontDistributionId")
                .value(frontend.distribution().getDistributionId())
                .build();
        CfnOutput.Builder.create(this, "CognitoHostedUiDomain")
                .value(userPoolDomain.baseUrl())
                .build();
        CfnOutput.Builder.create(this, "McpApiHandlerFunctionArn")
                .value(mcpApi.apiHandlerLiveAlias().getFunctionArn())
                .build();
        CfnOutput.Builder.create(this, "McpApiGatewayUrl")
                .value(mcpApi.api().getUrl())
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
