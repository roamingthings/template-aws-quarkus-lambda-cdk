## Why

The template currently deploys a Quarkus Lambda with a REST API but has no MCP server capability. Adding an MCP handler module enables Claude Code and claude.ai to connect as MCP clients, secured via OAuth2 PKCE through Cognito. This extends the template's usefulness as a reference for building AI-integrated serverless applications.

## What Changes

- New Gradle handler module `my-service/handlers/mcp/` with Quarkus MCP Server HTTP, SSE blocking filter, Cognito identity provider, and a greeting tool
- New CDK construct `OAuthFrontendConstruct` for S3 + CloudFront distribution that serves as the unified entry point for both the existing REST API and the MCP server, plus OAuth metadata
- New CDK construct `McpApiConstruct` for dedicated API Gateway + Lambda with Cognito authorizer
- Modified `MyServiceStack` to wire CloudFront behaviors for both the existing REST API (`/*` default) and MCP (`/mcp*`), Cognito User Pool, OAuth metadata deployment, and new stack outputs
- Updated `my-service/settings.gradle.kts` to include `handlers:mcp` module

## Capabilities

### New Capabilities
- `mcp-handler`: MCP server Lambda handler with SSE blocking, Cognito identity extraction, and greeting tool
- `mcp-infrastructure`: CDK constructs for OAuth frontend, MCP API Gateway, Cognito setup, and CloudFront routing

### Modified Capabilities

(none)

## Impact

- **Code**: New handler module under `my-service/handlers/mcp/`, new CDK constructs in `cdk/`
- **Dependencies**: `quarkus-mcp-server-http`, `quarkus-amazon-lambda-rest`, `quarkus-arc`, `quarkus-logging-json`, `quarkus-jsonp`
- **Infrastructure**: New Lambda function, API Gateway, Cognito User Pool, S3 bucket, CloudFront distribution fronting both REST API and MCP server
- **Build**: New Gradle submodule included in `my-service` multi-project build
