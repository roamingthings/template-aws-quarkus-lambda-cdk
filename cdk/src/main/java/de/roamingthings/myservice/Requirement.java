package de.roamingthings.myservice;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// Generated from the capability spec in [de.roamingthings.myservice] — do not edit.
/// Marks the boundary method or test that realizes the given requirement statements.
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Requirement {

    /// One constant per statement id in the spec's `## Requirements`.
    enum Rn {
        /// When the service is provisioned, the BC shall deploy the Lambda greeter, the REST greeter and the MCP handler as functions, each reachable through a `live` alias.
        R1_1("R1.1", "When the service is provisioned, the BC shall deploy the Lambda greeter, the REST greeter and the MCP handler as functions, each reachable through a `live` alias."),
        /// Where encryption with a customer-managed key is enabled, the BC shall encrypt the handlers' logs with an application key.
        R1_2("R1.2", "Where encryption with a customer-managed key is enabled, the BC shall encrypt the handlers' logs with an application key."),
        /// When the service is provisioned, the BC shall expose one public entry point that routes `/api/*` to the REST greeter, `/mcp*` to the MCP handler and every other path to static content.
        R2_1("R2.1", "When the service is provisioned, the BC shall expose one public entry point that routes `/api/*` to the REST greeter, `/mcp*` to the MCP handler and every other path to static content."),
        /// The BC shall never cache REST or MCP responses at the entry point.
        R2_2("R2.2", "The BC shall never cache REST or MCP responses at the entry point."),
        /// If an MCP request lacks a valid access token carrying the `connect` scope, then the BC shall reject it as unauthorized and point the caller to the protected-resource metadata.
        R3_1("R3.1", "If an MCP request lacks a valid access token carrying the `connect` scope, then the BC shall reject it as unauthorized and point the caller to the protected-resource metadata."),
        /// When the service is provisioned, the BC shall publish protected-resource metadata naming the resource, its authorization server and the `connect` scope at `/.well-known/oauth-protected-resource`.
        R3_2("R3.2", "When the service is provisioned, the BC shall publish protected-resource metadata naming the resource, its authorization server and the `connect` scope at `/.well-known/oauth-protected-resource`."),
        /// The BC shall provision a user directory that signs users in by email, forbids self sign-up and offers optional MFA.
        R4_1("R4.1", "The BC shall provision a user directory that signs users in by email, forbids self sign-up and offers optional MFA."),
        /// The BC shall register a public agent client without a secret that uses the authorization-code flow with callbacks for Claude Code, claude.ai and ChatGPT.
        R4_2("R4.2", "The BC shall register a public agent client without a secret that uses the authorization-code flow with callbacks for Claude Code, claude.ai and ChatGPT."),
        /// The BC shall issue agent access tokens valid for one hour and refresh tokens valid for 30 days.
        R4_3("R4.3", "The BC shall issue agent access tokens valid for one hour and refresh tokens valid for 30 days."),
        /// The BC shall provision the demo user `demo@example.com`.
        R4_4("R4.4", "The BC shall provision the demo user `demo@example.com`."),
        /// When the service is provisioned, the BC shall publish `GreeterHandlerFunctionArn`, `GreeterApiHandlerFunctionArn`, `ApiGatewayUrl`, `UserPoolId`, `AgentUserPoolClientId`, `CloudFrontUrl`, `CloudFrontDistributionId`, `CognitoHostedUiDomain`, `McpApiHandlerFunctionArn` and `McpApiGatewayUrl`.
        R5_1("R5.1", "When the service is provisioned, the BC shall publish `GreeterHandlerFunctionArn`, `GreeterApiHandlerFunctionArn`, `ApiGatewayUrl`, `UserPoolId`, `AgentUserPoolClientId`, `CloudFrontUrl`, `CloudFrontDistributionId`, `CognitoHostedUiDomain`, `McpApiHandlerFunctionArn` and `McpApiGatewayUrl`."),
        /// The BC shall retain gateway access logs for one week and function logs for two weeks.
        R6_1("R6.1", "The BC shall retain gateway access logs for one week and function logs for two weeks."),
        /// The BC shall trace every handler invocation.
        R6_2("R6.2", "The BC shall trace every handler invocation.");

        String id;
        String statement;

        Rn(String id, String statement) {
            this.id = id;
            this.statement = statement;
        }

        public String statement() {
            return this.statement;
        }

        @Override
        public String toString() {
            return this.id;
        }
    }

    Rn[] value();
}
