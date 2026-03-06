## 1. MCP Handler Module Setup

- [x] 1.1 Create `my-service/handlers/mcp/build.gradle.kts` with Quarkus plugins, MCP server HTTP, Lambda REST, Arc, logging-json, jsonp, jspecify dependencies
- [x] 1.2 Create `src/main/resources/application.properties` with legacy-jar, dummy-init, security, and logging configuration
- [x] 1.3 Add `include("handlers:mcp")` to `my-service/settings.gradle.kts`
- [x] 1.4 Create `package-info.java` with `@NullMarked` for `de.roamingthings.myservice.mcp`

## 2. MCP Handler Implementation

- [x] 2.1 Create `GreetingMcpTools` CDI bean with `@Tool greet(name)` method
- [x] 2.2 Create `McpSseBlockingFilter` Vert.x Router filter returning 405 on GET `/mcp`
- [x] 2.3 Create `CognitoAccessTokenIdentityProvider` implementing `LambdaIdentityProvider` to extract Cognito `sub` claim

## 3. MCP Handler Tests

- [x] 3.1 Create `McpSseBlockingFilterIT` verifying GET `/mcp` returns 405 and POST is not blocked
- [x] 3.2 Create `GreetingMcpToolsIT` with `@QuarkusTest` and `@TestSecurity`, calling greet tool via MCP JSON-RPC POST

## 4. CDK Constructs

- [x] 4.1 Create `OAuthFrontendConstruct` with S3 bucket (auto-delete, DESTROY) and CloudFront distribution (OAC)
- [x] 4.2 Create `McpApiConstruct` with QuarkusFunction, API Gateway, Cognito authorizer, access logs, and 401 WWW-Authenticate response

## 5. Stack Integration

- [x] 5.1 Add Cognito User Pool creation to `MyServiceStack` (email sign-in, no self-signup, optional MFA, DESTROY)
- [x] 5.2 Add MCP resource server scope (`connect`) and Claude User Pool client (PKCE, callback URLs)
- [x] 5.3 Add Cognito Hosted UI domain
- [x] 5.4 Instantiate `OAuthFrontendConstruct` and `McpApiConstruct` in `MyServiceStack`
- [x] 5.5 Add CloudFront behaviors: `/mcp*` to MCP API Gateway, `/api*` to existing REST API Gateway (default behavior serves S3)
- [x] 5.6 Deploy `.well-known/oauth-protected-resource` JSON to S3
- [x] 5.7 Add stack outputs (UserPoolId, AgentUserPoolClientId, CloudFrontUrl, CloudFrontDistributionId, CognitoHostedUiDomain, McpApiHandlerFunctionArn, McpApiGatewayUrl)

## 6. REST API Path Prefix

- [x] 6.1 Add `quarkus.http.root-path=/api` to `my-service/handlers/greeter-api/src/main/resources/application.properties`

## 7. Documentation

- [x] 7.1 Create `docs/mcp-setup.md` with instructions to retrieve CloudFormation outputs and connect Claude Code via `claude mcp add --transport http`

## 8. Verification

- [x] 8.1 Run full Gradle build to verify handler module compiles and produces function.zip
- [x] 8.2 Run CDK synth to verify stack synthesizes without errors
