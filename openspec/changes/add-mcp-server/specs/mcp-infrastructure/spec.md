## ADDED Requirements

### Requirement: OAuth frontend construct
The CDK SHALL create an `OAuthFrontendConstruct` containing an S3 bucket (auto-delete objects, DESTROY removal policy) and a CloudFront distribution with S3 origin using OAC. The construct SHALL expose `bucket()` and `distribution()` accessors.

#### Scenario: Construct creates S3 and CloudFront
- **WHEN** `OAuthFrontendConstruct` is instantiated in a stack
- **THEN** an S3 bucket and CloudFront distribution are created
- **THEN** the distribution serves content from the S3 bucket via OAC

### Requirement: MCP API construct
The CDK SHALL create an `McpApiConstruct` containing a QuarkusFunction for the `handlers:mcp` module, an API Gateway with Cognito authorizer requiring the MCP scope, CloudWatch access logs, and a 401 gateway response with `WWW-Authenticate` header pointing to the resource metadata URL.

#### Scenario: Construct creates Lambda and API Gateway
- **WHEN** `McpApiConstruct` is instantiated with userPool, userPoolClient, mcpScope, and cloudFrontDomainName
- **THEN** a Lambda function and API Gateway are created
- **THEN** the API Gateway uses a Cognito authorizer

#### Scenario: Unauthenticated request returns 401 with WWW-Authenticate
- **WHEN** a request reaches the API Gateway without a valid token
- **THEN** the response status is 401
- **THEN** the response contains `WWW-Authenticate: Bearer resource_metadata="https://<cf-domain>/.well-known/oauth-protected-resource"`

### Requirement: Cognito User Pool setup
The stack SHALL create a Cognito User Pool with email sign-in, self-signup disabled, optional MFA, and DESTROY removal policy.

#### Scenario: User Pool is created
- **WHEN** `MyServiceStack` is deployed
- **THEN** a Cognito User Pool exists with email sign-in and self-signup disabled

### Requirement: MCP resource server scope
The stack SHALL create a Cognito resource server with identifier `https://<cloudfront-domain>` and a `connect` scope.

#### Scenario: Resource server scope is registered
- **WHEN** the stack is deployed
- **THEN** a resource server with scope `connect` exists on the User Pool

### Requirement: Claude User Pool client
The stack SHALL create a public User Pool client (no secret) supporting authorization code grant with PKCE. Callback URLs SHALL include `http://localhost:9876/oauth/callback` for Claude Code and `https://claude.ai/api/mcp/auth/callback` for claude.ai. The client SHALL request the MCP `connect` scope.

#### Scenario: Client is created with PKCE support
- **WHEN** the stack is deployed
- **THEN** a User Pool client exists with auth code grant and no client secret
- **THEN** callback URLs include localhost:9876 and claude.ai

### Requirement: CloudFront routing behaviors
The CloudFront distribution SHALL route traffic to multiple origins: `/mcp*` to the MCP API Gateway, `/api*` to the existing REST API Gateway, and the default behavior (`/.well-known/*` etc.) to S3.

#### Scenario: MCP requests are routed through CloudFront
- **WHEN** a client sends a request to `https://<cf-domain>/mcp`
- **THEN** the request is forwarded to the MCP API Gateway

#### Scenario: REST API requests are routed through CloudFront
- **WHEN** a client sends a request to `https://<cf-domain>/api/greetings`
- **THEN** the request is forwarded to the existing REST API Gateway

#### Scenario: OAuth metadata is served from S3
- **WHEN** a client requests `https://<cf-domain>/.well-known/oauth-protected-resource`
- **THEN** the request is served from the S3 bucket

### Requirement: OAuth server metadata deployment
The stack SHALL deploy a `.well-known/oauth-protected-resource` JSON document to S3, served via CloudFront. The document SHALL contain the Cognito issuer URL and required scopes.

#### Scenario: OAuth metadata is accessible
- **WHEN** a client requests `https://<cf-domain>/.well-known/oauth-protected-resource`
- **THEN** a JSON document is returned with the Cognito issuer URL

### Requirement: Stack outputs
The stack SHALL export: `UserPoolId`, `ClaudeUserPoolClientId`, `CloudFrontUrl`, `CloudFrontDistributionId`, `CognitoHostedUiDomain`, `McpApiHandlerFunctionArn`, `McpApiGatewayUrl`.

#### Scenario: Outputs are available after deployment
- **WHEN** the stack is deployed
- **THEN** all seven outputs are present in the CloudFormation outputs

### Requirement: REST API path prefix
The greeter-api handler SHALL configure `quarkus.http.root-path=/api` in its `application.properties` so that all REST endpoints are served under the `/api` prefix when routed through CloudFront.

#### Scenario: REST endpoints are prefixed with /api
- **WHEN** the greeter-api handler receives a request
- **THEN** all JAX-RS resources are available under the `/api` path prefix

### Requirement: MCP setup documentation
A `docs/mcp-setup.md` document SHALL describe how to connect Claude Code to the deployed MCP server. It SHALL include steps to retrieve CloudFormation outputs and the `claude mcp add --transport http` command with `--client-id` and `--callback-port` flags.

#### Scenario: Developer follows documentation to connect Claude Code
- **WHEN** a developer reads `docs/mcp-setup.md` after deployment
- **THEN** the document provides the `aws cloudformation describe-stacks` command to retrieve outputs
- **THEN** the document provides the `claude mcp add` command with placeholders for `ClaudeUserPoolClientId` and `CloudFrontDomain`

### Requirement: Gradle settings include MCP module
The `my-service/settings.gradle.kts` SHALL include `handlers:mcp` in the build.

#### Scenario: MCP module is part of the build
- **WHEN** Gradle evaluates the `my-service` build
- **THEN** the `handlers:mcp` module is included
