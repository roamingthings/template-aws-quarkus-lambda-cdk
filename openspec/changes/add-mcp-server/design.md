## Context

The template currently deploys two Quarkus Lambda handlers (`greeter`, `greeter-api`) behind a single API Gateway, all defined in `MyServiceStack`. There is no authentication layer and no MCP capability. The project uses a multi-project Gradle build under `my-service/` with shared build logic and a version catalog.

The reference implementation `zgf-sc-trade-tracker` at /Users/alxs/Development/Workspaces/roamingthings/zgf-sc-trade-tracker
 demonstrates the full pattern including Cognito OAuth2, CloudFront, and MCP server HTTP transport on Lambda.

## Goals / Non-Goals

**Goals:**
- Deploy a functional MCP server as a Lambda behind CloudFront with OAuth2 PKCE via Cognito
- Enable Claude Code and claude.ai to connect using `claude mcp add --transport http`
- Follow existing project conventions (BCE, QuarkusFunction construct, Gradle multi-project)

**Non-Goals:**
- DynamoDB or persistent state (greeting tool is stateless)
- Per-user identity mapping or session management
- SPA frontend or Hosted UI customization
- Dev-mode HTTP auth bypass
- SSE/streaming support (Lambda cannot maintain SSE connections)

## Decisions

### 1. Separate Lambda + API Gateway for MCP

MCP traffic gets its own Lambda function and API Gateway rather than sharing the existing greeter API Gateway. This keeps concerns isolated — the MCP API Gateway carries a Cognito authorizer while the greeter API does not.

**Alternative**: Add MCP routes to the existing API Gateway. Rejected because mixing authenticated and unauthenticated routes on one gateway adds authorizer complexity.

### 2. CloudFront as unified entry point for all traffic

A single CloudFront distribution fronts everything: the existing REST API as the default behavior (`/*`), MCP traffic via `/mcp*` routed to the MCP API Gateway, and `/.well-known/*` served from S3. Both the REST API and MCP server share the same CloudFront domain.

**Alternative**: Keep the REST API exposed directly via its API Gateway URL. Rejected because a single domain simplifies client configuration and is required for the OAuth `oauth-protected-resource` flow.

### 3. SSE blocking via Vert.x Router filter

A `McpSseBlockingFilter` intercepts GET `/mcp` and returns 405, forcing clients to use POST-only (Streamable HTTP) mode. Lambda cannot maintain SSE connections.

**Alternative**: Disable SSE at the Quarkus MCP server level. The library's `dummy-init` config partially handles this, but an explicit 405 with `Allow` header gives clients a clean signal.

### 4. Cognito identity extraction via LambdaIdentityProvider

A `CognitoAccessTokenIdentityProvider` extracts the `sub` claim from the API Gateway Cognito authorizer context. This integrates with Quarkus Security without requiring token validation in the Lambda itself (API Gateway already validates).

**Alternative**: Use a Quarkus OIDC extension to validate tokens directly. Rejected as redundant — API Gateway already validates via the Cognito authorizer.

### 5. CDK constructs as plain classes in boundary package

`OAuthFrontendConstruct` and `McpApiConstruct` follow the existing pattern of CDK constructs in `de.roamingthings.myservice.boundary`, matching `MyServiceStack`.

## Risks / Trade-offs

- **CloudFront caching on MCP routes**: POST requests are not cached by default, but CloudFront adds latency. Acceptable for MCP tool call patterns. → Mitigation: CloudFront behavior for `/mcp*` uses `ALLOW_ALL` for methods.
- **Cognito Hosted UI domain uniqueness**: The domain prefix must be globally unique. → Mitigation: Use `appName` as prefix, document that collisions require manual adjustment.
- **Cold start latency**: Additional Lambda adds another cold start surface. → Mitigation: Same as existing handlers — Quarkus native or SnapStart can be added later.
- **No rollback for Cognito resources**: User Pool deletion loses all user accounts. → Mitigation: DESTROY removal policy is acceptable for a template; production forks should adjust.
