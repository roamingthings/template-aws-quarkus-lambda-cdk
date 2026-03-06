## ADDED Requirements

### Requirement: MCP greeting tool
The system SHALL expose an MCP tool named `greet` that accepts a `name` argument and returns `Hello, <name>!`.

#### Scenario: Greet with a name
- **WHEN** an MCP client calls the `greet` tool with name `"World"`
- **THEN** the tool returns `"Hello, World!"`

### Requirement: SSE blocking filter
The system SHALL reject HTTP GET requests to `/mcp` with status 405 and an `Allow: POST, DELETE` header, forcing clients to use POST-only (Streamable HTTP) mode.

#### Scenario: GET /mcp returns 405
- **WHEN** a client sends GET `/mcp`
- **THEN** the response status is 405
- **THEN** the response contains header `Allow: POST, DELETE`

#### Scenario: POST /mcp is not blocked
- **WHEN** a client sends POST `/mcp`
- **THEN** the request is not intercepted by the SSE blocking filter

### Requirement: Cognito identity extraction
The system SHALL extract the Cognito `sub` claim from the API Gateway authorizer context and make it available as the Quarkus SecurityIdentity principal.

#### Scenario: Authenticated request with Cognito sub
- **WHEN** a Lambda event contains authorizer claims with a `sub` value
- **THEN** the SecurityIdentity principal name equals the `sub` claim value

#### Scenario: Request without authorizer context
- **WHEN** a Lambda event has no authorizer or claims
- **THEN** the identity provider returns null

### Requirement: Handler module build configuration
The handler module SHALL use Quarkus plugins (`io.quarkus`, `nullability-conventions`) and depend on `quarkus-mcp-server-http`, `quarkus-amazon-lambda-rest`, `quarkus-arc`, `quarkus-logging-json`, `quarkus-jsonp`, and `jspecify`.

#### Scenario: Module builds and produces function.zip
- **WHEN** the `handlers:mcp` Gradle module is built
- **THEN** a deployable `function.zip` artifact is produced

### Requirement: Application configuration
The handler SHALL configure `quarkus.package.jar.type=legacy-jar`, `quarkus.mcp.server.http.streamable.dummy-init=true`, `quarkus.security.jaxrs.deny-unannotated-endpoints=false`, `quarkus.lambda-http.enable-security=true`, and log level from `AWS_LAMBDA_LOG_LEVEL` environment variable defaulting to `INFO`.

#### Scenario: Properties are applied
- **WHEN** the application starts
- **THEN** the configured properties are active
