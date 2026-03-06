# MCP Server Setup

## Prerequisites

- Deployed `MyServiceStack` via `cdk deploy`
- A Cognito user account (create one in the AWS Console under the deployed User Pool)

## Retrieve Stack Outputs

```bash
aws cloudformation describe-stacks \
  --stack-name MyServiceStack \
  --query "Stacks[0].Outputs" \
  --output table
```

Note the values for `AgentUserPoolClientId` and `CloudFrontUrl`.

## Connect Claude Code

```bash
claude mcp add --transport http \
  --client-id <AgentUserPoolClientId> \
  --callback-port 9876 \
  my-service-mcp <CloudFrontUrl>/mcp
```

Replace `<AgentUserPoolClientId>` and `<CloudFrontUrl>` with the actual stack output values.

On first use, Claude Code opens a browser window for Cognito authentication. After sign-in, the MCP server is available in your session.
