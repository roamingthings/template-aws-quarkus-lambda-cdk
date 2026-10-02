package de.roamingthings.myservice.boundary;

import de.roamingthings.myservice.Requirement;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import software.amazon.awscdk.App;
import software.amazon.awscdk.AppProps;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.assertions.Match;
import software.amazon.awscdk.assertions.Template;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.roamingthings.myservice.Requirement.Rn.R1_1;
import static de.roamingthings.myservice.Requirement.Rn.R1_2;
import static de.roamingthings.myservice.Requirement.Rn.R2_1;
import static de.roamingthings.myservice.Requirement.Rn.R2_2;
import static de.roamingthings.myservice.Requirement.Rn.R3_1;
import static de.roamingthings.myservice.Requirement.Rn.R3_2;
import static de.roamingthings.myservice.Requirement.Rn.R4_1;
import static de.roamingthings.myservice.Requirement.Rn.R4_2;
import static de.roamingthings.myservice.Requirement.Rn.R4_3;
import static de.roamingthings.myservice.Requirement.Rn.R4_4;
import static de.roamingthings.myservice.Requirement.Rn.R5_1;
import static de.roamingthings.myservice.Requirement.Rn.R6_1;
import static de.roamingthings.myservice.Requirement.Rn.R6_2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class MyServiceStackTest {

    static final String APP_NAME = "my-service";
    static final String QUARKUS_HANDLER = "io.quarkus.amazon.lambda.runtime.QuarkusStreamHandler::handleRequest";
    static final List<String> HANDLER_NAMES = List.of(
            "my-service-GreeterHandler", "my-service-GreeterApiHandler", "my-service-McpHandler");
    static final String CACHING_DISABLED_POLICY_ID = "4135ea2d-6df8-44a3-9df3-4b5a84be39ad";

    static Template template;
    static Template encryptedTemplate;
    static Path assemblyDirectory;

    @BeforeAll
    static void provisionService() throws IOException {
        assemblyDirectory = Files.createTempDirectory("cdk.out");
        template = provision(false, assemblyDirectory);
        encryptedTemplate = provision(true, Files.createTempDirectory("cdk.out"));
    }

    static Template provision(boolean encryptWithCmk, Path outdir) {
        var app = new App(AppProps.builder().outdir(outdir.toString()).build());
        var stack = new MyServiceStack(app, "MyServiceStack", new MyServiceStack.MyServiceStackProps(
                APP_NAME, StackProps.builder().build(), encryptWithCmk));
        var stackTemplate = Template.fromStack(stack);
        app.synth();
        return stackTemplate;
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("runHandlersCases")
    void runHandlers(Requirement.Rn requirement, Consumer<String> check) {
        check.accept(description(requirement));
    }

    static Stream<Arguments> runHandlersCases() {
        return Stream.of(
                arguments(R1_1, (Consumer<String>) MyServiceStackTest::handlersBehindLiveAlias),
                arguments(R1_2, (Consumer<String>) MyServiceStackTest::handlerLogsEncryptedWithApplicationKey)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("publicEntryPointCases")
    void publicEntryPoint(Requirement.Rn requirement, Consumer<String> check) {
        check.accept(description(requirement));
    }

    static Stream<Arguments> publicEntryPointCases() {
        return Stream.of(
                arguments(R2_1, (Consumer<String>) MyServiceStackTest::pathsRoutedToOrigins),
                arguments(R2_2, (Consumer<String>) MyServiceStackTest::apiResponsesNeverCached)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("protectMcpCases")
    void protectMcp(Requirement.Rn requirement, Consumer<String> check) {
        check.accept(description(requirement));
    }

    static Stream<Arguments> protectMcpCases() {
        return Stream.of(
                arguments(R3_1, (Consumer<String>) MyServiceStackTest::mcpRejectsWithoutConnectScope),
                arguments(R3_2, (Consumer<String>) MyServiceStackTest::protectedResourceMetadataPublished)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("identityCases")
    void identity(Requirement.Rn requirement, Consumer<String> check) {
        check.accept(description(requirement));
    }

    static Stream<Arguments> identityCases() {
        return Stream.of(
                arguments(R4_1, (Consumer<String>) MyServiceStackTest::userDirectoryByEmailWithoutSignUp),
                arguments(R4_2, (Consumer<String>) MyServiceStackTest::publicAgentClient),
                arguments(R4_3, (Consumer<String>) MyServiceStackTest::agentTokenValidity),
                arguments(R4_4, (Consumer<String>) MyServiceStackTest::demoUser)
        );
    }

    @Test
    @Requirement(R5_1)
    void publishCoordinates() {
        var outputs = template.findOutputs("*");

        assertThat(outputs.keySet()).as(description(R5_1)).contains(
                "GreeterHandlerFunctionArn", "GreeterApiHandlerFunctionArn", "ApiGatewayUrl", "UserPoolId",
                "AgentUserPoolClientId", "CloudFrontUrl", "CloudFrontDistributionId", "CognitoHostedUiDomain",
                "McpApiHandlerFunctionArn", "McpApiGatewayUrl");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("observabilityCases")
    void observability(Requirement.Rn requirement, Consumer<String> check) {
        check.accept(description(requirement));
    }

    static Stream<Arguments> observabilityCases() {
        return Stream.of(
                arguments(R6_1, (Consumer<String>) MyServiceStackTest::logRetention),
                arguments(R6_2, (Consumer<String>) MyServiceStackTest::handlersTraced)
        );
    }

    static void handlersBehindLiveAlias(String description) {
        var handlers = handlerFunctions(template);
        var aliasTargets = template.findResources("AWS::Lambda::Alias", properties(Map.of("Name", "live")))
                .values().stream()
                .map(MyServiceStackTest::aliasTarget)
                .toList();

        assertThat(handlers).as(description).hasSize(3);
        assertThat(aliasTargets).as(description).containsExactlyInAnyOrderElementsOf(handlers.keySet());
        assertThat(functionNames(handlers)).as(description).containsExactlyInAnyOrderElementsOf(HANDLER_NAMES);
    }

    static void handlerLogsEncryptedWithApplicationKey(String description) {
        var keyIds = encryptedTemplate.findResources("AWS::KMS::Key").keySet();
        var handlerLogGroups = handlerLogGroups(encryptedTemplate);
        var logKeys = handlerLogGroups.stream()
                .map(logGroup -> keyReference(logGroup))
                .toList();

        assertThat(keyIds).as(description).hasSize(1);
        assertThat(logKeys).as(description).hasSize(3).containsOnly(keyIds.iterator().next());
    }

    static void pathsRoutedToOrigins(String description) {
        var distribution = distributionConfig();
        var behaviors = cacheBehaviors(distribution);
        var restApi = restApiLogicalId("my-service-Api");
        var mcpApi = restApiLogicalId("my-service-McpApi");
        var bucket = singleLogicalId("AWS::S3::Bucket");
        var defaultOrigin = (String) ((Map<?, ?>) distribution.get("DefaultCacheBehavior")).get("TargetOriginId");

        assertThat(behaviors.keySet()).as(description).containsExactlyInAnyOrder("/api/*", "/mcp*");
        assertThat(originDomain(distribution, behaviors.get("/api/*"))).as(description).contains(restApi);
        assertThat(originDomain(distribution, behaviors.get("/mcp*"))).as(description).contains(mcpApi);
        assertThat(originDomain(distribution, defaultOrigin)).as(description).contains(bucket);
        assertThat(apiHandler(restApi)).as(description).isEqualTo("my-service-GreeterApiHandler");
        assertThat(apiHandler(mcpApi)).as(description).isEqualTo("my-service-McpHandler");
    }

    static void apiResponsesNeverCached(String description) {
        var cachePolicies = ((List<?>) distributionConfig().get("CacheBehaviors")).stream()
                .map(behavior -> (Object) ((Map<?, ?>) behavior).get("CachePolicyId"))
                .toList();

        assertThat(cachePolicies).as(description).hasSize(2).containsOnly(CACHING_DISABLED_POLICY_ID);
    }

    static void mcpRejectsWithoutConnectScope(String description) {
        var mcpApi = restApiLogicalId("my-service-McpApi");
        var mcpMethods = template.findResources("AWS::ApiGateway::Method",
                properties(Map.of("RestApiId", Map.of("Ref", mcpApi)))).values();
        var unauthorizedResponse = template.findResources("AWS::ApiGateway::GatewayResponse",
                properties(Map.of("ResponseType", "UNAUTHORIZED"))).values();

        var authorizationTypes = mcpMethods.stream()
                .map(method -> property(method, "AuthorizationType"))
                .toList();
        var authorizationScopes = mcpMethods.stream()
                .map(method -> String.valueOf(property(method, "AuthorizationScopes")))
                .toList();

        assertThat(authorizationTypes).as(description).isNotEmpty().containsOnly("COGNITO_USER_POOLS");
        assertThat(authorizationScopes).as(description).allMatch(scopes -> scopes.contains("/connect"));
        assertThat(unauthorizedResponse).as(description).singleElement()
                .extracting(response -> String.valueOf(property(response, "ResponseParameters")))
                .asString()
                .contains("gatewayresponse.header.WWW-Authenticate")
                .contains("Bearer resource_metadata=")
                .contains("/.well-known/oauth-protected-resource");
    }

    static void protectedResourceMetadataPublished(String description) {
        var deployment = template.findResources("Custom::CDKBucketDeployment").values().iterator().next();
        var markerValues = String.valueOf(property(deployment, "SourceMarkers"));
        var metadata = stagedMetadata();

        assertThat(property(deployment, "DestinationBucketKeyPrefix")).as(description).isEqualTo(".well-known");
        assertThat(metadata).as(description)
                .contains("\"resource\"")
                .contains("\"authorization_servers\"")
                .contains("\"scopes_supported\"");
        assertThat(markerValues).as(description)
                .contains("https://cognito-idp.")
                .contains("/connect");
    }

    static void userDirectoryByEmailWithoutSignUp(String description) {
        template.hasResourceProperties("AWS::Cognito::UserPool", Map.of(
                "UsernameAttributes", List.of("email"),
                "AdminCreateUserConfig", Map.of("AllowAdminCreateUserOnly", true),
                "MfaConfiguration", "OPTIONAL"));
    }

    static void publicAgentClient(String description) {
        template.hasResourceProperties("AWS::Cognito::UserPoolClient", Map.of(
                "GenerateSecret", false,
                "AllowedOAuthFlows", List.of("code"),
                "CallbackURLs", List.of(
                        "http://localhost:9876/callback",
                        "https://claude.ai/api/mcp/auth_callback",
                        "https://chatgpt.com/connector_platform_oauth_redirect")));
    }

    static void agentTokenValidity(String description) {
        template.hasResourceProperties("AWS::Cognito::UserPoolClient", Map.of(
                "AccessTokenValidity", 60,
                "RefreshTokenValidity", 43200,
                "TokenValidityUnits", Map.of("AccessToken", "minutes", "RefreshToken", "minutes")));
    }

    static void demoUser(String description) {
        template.hasResourceProperties("AWS::Cognito::UserPoolUser", Map.of("Username", "demo@example.com"));
    }

    static void logRetention(String description) {
        var functionLogRetention = handlerLogGroups(template).stream()
                .map(logGroup -> property(logGroup, "RetentionInDays"))
                .toList();
        var accessLogRetention = template.findResources("AWS::ApiGateway::Stage").values().stream()
                .map(stage -> accessLogRetention(stage))
                .toList();

        assertThat(functionLogRetention).as(description).hasSize(3).containsOnly(14);
        assertThat(accessLogRetention).as(description).hasSize(2).containsOnly(7);
    }

    static void handlersTraced(String description) {
        var tracing = handlerFunctions(template).values().stream()
                .map(function -> property(function, "TracingConfig"))
                .toList();

        assertThat(tracing).as(description).hasSize(3).containsOnly(Map.of("Mode", "Active"));
    }

    static String description(Requirement.Rn requirement) {
        return requirement + " — " + requirement.statement();
    }

    static Map<String, Object> properties(Map<String, Object> properties) {
        return Map.of("Properties", Match.objectLike(properties));
    }

    static Object property(Object resource, String name) {
        var properties = (Map<?, ?>) ((Map<?, ?>) resource).get("Properties");
        return properties.get(name);
    }

    static Map<String, Map<String, Object>> handlerFunctions(Template stackTemplate) {
        return stackTemplate.findResources("AWS::Lambda::Function", properties(Map.of("Handler", QUARKUS_HANDLER)));
    }

    static List<Object> functionNames(Map<String, Map<String, Object>> functions) {
        return functions.values().stream()
                .map(function -> property(function, "FunctionName"))
                .toList();
    }

    static String aliasTarget(Map<String, Object> alias) {
        var functionName = (Map<?, ?>) property(alias, "FunctionName");
        return (String) functionName.get("Ref");
    }

    static List<Map<String, Object>> handlerLogGroups(Template stackTemplate) {
        return HANDLER_NAMES.stream()
                .map(name -> "/aws/lambda/" + name)
                .flatMap(logGroupName -> stackTemplate.findResources("AWS::Logs::LogGroup",
                        properties(Map.of("LogGroupName", logGroupName))).values().stream())
                .toList();
    }

    static String keyReference(Map<String, Object> logGroup) {
        var kmsKeyId = (Map<?, ?>) property(logGroup, "KmsKeyId");
        var getAtt = (List<?>) kmsKeyId.get("Fn::GetAtt");
        return (String) getAtt.getFirst();
    }

    static Object accessLogRetention(Map<String, Object> stage) {
        var accessLogSetting = (Map<?, ?>) property(stage, "AccessLogSetting");
        var destination = (Map<?, ?>) accessLogSetting.get("DestinationArn");
        var logGroupId = (String) ((List<?>) destination.get("Fn::GetAtt")).getFirst();
        var logGroup = template.findResources("AWS::Logs::LogGroup").get(logGroupId);
        return property(logGroup, "RetentionInDays");
    }

    static Map<?, ?> distributionConfig() {
        var distribution = template.findResources("AWS::CloudFront::Distribution").values().iterator().next();
        return (Map<?, ?>) property(distribution, "DistributionConfig");
    }

    static Map<String, String> cacheBehaviors(Map<?, ?> distribution) {
        return ((List<?>) distribution.get("CacheBehaviors")).stream()
                .map(behavior -> (Map<?, ?>) behavior)
                .collect(Collectors.toMap(
                        behavior -> (String) behavior.get("PathPattern"),
                        behavior -> (String) behavior.get("TargetOriginId")));
    }

    static String originDomain(Map<?, ?> distribution, String originId) {
        return ((List<?>) distribution.get("Origins")).stream()
                .map(origin -> (Map<?, ?>) origin)
                .filter(origin -> originId.equals(origin.get("Id")))
                .map(origin -> String.valueOf(origin.get("DomainName")))
                .findFirst()
                .orElseThrow();
    }

    static String restApiLogicalId(String name) {
        return template.findResources("AWS::ApiGateway::RestApi", properties(Map.of("Name", name)))
                .keySet().iterator().next();
    }

    static String singleLogicalId(String type) {
        return template.findResources(type).keySet().iterator().next();
    }

    static Object apiHandler(String restApiLogicalId) {
        var proxyMethod = template.findResources("AWS::ApiGateway::Method", properties(Map.of(
                "RestApiId", Map.of("Ref", restApiLogicalId),
                "HttpMethod", "ANY"))).values().iterator().next();
        var integrationUri = String.valueOf(((Map<?, ?>) property(proxyMethod, "Integration")).get("Uri"));
        var handlers = handlerFunctions(template);
        return template.findResources("AWS::Lambda::Alias").entrySet().stream()
                .filter(alias -> integrationUri.contains(alias.getKey()))
                .map(alias -> handlers.get(aliasTarget(alias.getValue())))
                .map(function -> property(function, "FunctionName"))
                .findFirst()
                .orElseThrow();
    }

    static String stagedMetadata() {
        try (var files = Files.walk(assemblyDirectory)) {
            var metadataFile = files
                    .filter(file -> file.getFileName().toString().equals("oauth-protected-resource"))
                    .findFirst()
                    .orElseThrow();
            return Files.readString(metadataFile);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
