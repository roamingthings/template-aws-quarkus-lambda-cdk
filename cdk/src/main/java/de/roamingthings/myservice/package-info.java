/// # My Service
/// > Provision the greeting handlers behind one secured public entry point in a single AWS stack.
///
/// ## Boundary
/// - `provision-service` — deploy the handlers, the public entry point and the identity setup as one stack
///
/// ## Requirements
/// ### R1: Run the handlers
/// - R1.1 — When the service is provisioned, the BC shall deploy the Lambda greeter, the REST greeter and the MCP
///   handler as functions, each reachable through a `live` alias.
/// - R1.2 — Where encryption with a customer-managed key is enabled, the BC shall encrypt the handlers' logs with an
///   application key.
///
/// ### R2: Single public entry point
/// - R2.1 — When the service is provisioned, the BC shall expose one public entry point that routes `/api/*` to the
///   REST greeter, `/mcp*` to the MCP handler and every other path to static content.
/// - R2.2 — The BC shall never cache REST or MCP responses at the entry point.
///
/// ### R3: Protect MCP with OAuth
/// - R3.1 — If an MCP request lacks a valid access token carrying the `connect` scope, then the BC shall reject it as
///   unauthorized and point the caller to the protected-resource metadata. _(why: lets agents discover the
///   authorization server on their own)_
/// - R3.2 — When the service is provisioned, the BC shall publish protected-resource metadata naming the resource, its
///   authorization server and the `connect` scope at `/.well-known/oauth-protected-resource`.
///
/// ### R4: Identity
/// - R4.1 — The BC shall provision a user directory that signs users in by email, forbids self sign-up and offers
///   optional MFA.
/// - R4.2 — The BC shall register a public agent client without a secret that uses the authorization-code flow with
///   callbacks for Claude Code, claude.ai and ChatGPT.
/// - R4.3 — The BC shall issue agent access tokens valid for one hour and refresh tokens valid for 30 days.
/// - R4.4 — The BC shall provision the demo user `demo@example.com`.
///
/// ### R5: Publish coordinates
/// - R5.1 — When the service is provisioned, the BC shall publish `GreeterHandlerFunctionArn`,
///   `GreeterApiHandlerFunctionArn`, `ApiGatewayUrl`, `UserPoolId`, `AgentUserPoolClientId`, `CloudFrontUrl`,
///   `CloudFrontDistributionId`, `CognitoHostedUiDomain`, `McpApiHandlerFunctionArn` and `McpApiGatewayUrl`.
///   _(why: system tests and agent setup read them)_
///
/// ### R6: Observability
/// - R6.1 — The BC shall retain gateway access logs for one week and function logs for two weeks.
/// - R6.2 — The BC shall trace every handler invocation.
///
/// ## References
/// - [RFC 9728 — OAuth 2.0 Protected Resource Metadata](https://www.rfc-editor.org/rfc/rfc9728) — shape and location
///   of the published metadata and the unauthorized challenge (governs R3)
///
/// ## Out of scope
/// - SnapStart for the handlers.
/// - Retaining data when the stack is deleted.
package de.roamingthings.myservice;
