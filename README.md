# Java 25 AWS Lambda Template with Quarkus and CDK

Template for building AWS Lambda functions using Java 25, Quarkus framework, and AWS CDK for infrastructure
provisioning.

> [!NOTE]
> Java 25 support in Quarkus is pending official release. This template uses Java 25 syntax and assumes compatibility.

## Purpose

Fork this repository to bootstrap serverless applications with:

- Lambda handlers using Quarkus (SnapStart enabled, ARM64 architecture)
- Infrastructure as code with AWS CDK in Java
- BCE/ECB architecture for maintainable business logic
- Independent modules for application, infrastructure, and system tests

## Requirements

### Development Environment

- Java 25 (use [SDKMAN](https://sdkman.io/): `sdk install java 25-open`)
- Maven 3.9+
- AWS CLI configured with credentials
- Node.js 24+ (for AWS CDK)
- AWS CDK Toolkit: `npm install -g aws-cdk`

### AWS Configuration

Create `~/.my-lambda/app.properties` with deployment configuration:

```properties
stack.props.region=eu-central-1
app.awsApplicationTagValue=arn:aws:resource-groups:eu-central-1:123456789012:group/my-app
```

Or provide a local `./app.properties` file in the project root (takes precedence over global config).

## Project Structure

Three independent Maven modules:

- **my-lambda/** - Application module with Quarkus handlers and shared libraries
- **cdk/** - Infrastructure module with AWS CDK stacks
- **my-lambda-st/** - System tests module for end-to-end testing

See [docs/project-structure.md](docs/project-structure.md) for detailed architecture.

## Build and Deploy

### Quick Deploy

```bash
./buildAndDeployDontAsk.sh
```

Executes:

1. `mvn clean package` in my-lambda/
2. `mvn clean package` in cdk/
3. `cdk deploy --all --require-approval=never`

### Manual Build Steps

**Build application:**

```bash
cd my-lambda
mvn clean package
```

**Synthesize CloudFormation template:**

```bash
cd cdk
mvn clean package
cdk synth
```

**Preview infrastructure changes:**

```bash
cdk diff
```

**Deploy to AWS:**

```bash
cdk deploy
```

**Run system tests:**

```bash
cd my-lambda-st
mvn verify
```

## Using This Template with Coding Agents

Fork this repository to create your application. After checkout, use a coding agent like Claude Code to customize the
template for your use case.

### Initial Setup Prompts

After forking and cloning your repository, use these prompts to configure the application:

**1. Set application name and Maven coordinates:**

```
Update the application name from "my-lambda" to "inventory-service".
Update all package names accordingly. Update the stack names and resource
names to use the new application name.
```

**2. Configure AWS deployment settings:**

```
Update the configuration to deploy to eu-central-1 region. The AWS Application
tag should be "arn:aws:resource-groups:eu-central-1:987654321098:group/inventory".
```

**3. Rename handler and adjust infrastructure:**

```
Rename the "item" handler to "product-catalog" handler. Update the Lambda
function description to "Manages product catalog operations". Update all
references in the CDK stack.
```

**4. Add business logic:**

```
The product-catalog handler should accept a Product record with fields:
id (String), name (String), price (BigDecimal), category (enum: ELECTRONICS,
BOOKS, CLOTHING). Implement validation logic to ensure price is positive and
name is not empty. Return a result type with Success or ValidationError cases.
```

**5. Add infrastructure components:**

```
Add a DynamoDB table to MyLambdaStack for storing products. Table name should
follow the naming convention using ConventionalDefaults. Use "id" as partition
key. Grant the Lambda function read/write permissions.
```

### Example Customization Workflow

1. **Fork** this repository on GitHub
2. **Clone** your fork locally
3. **Start Claude Code** in the project directory
4. **Provide context** by referencing key files:
    - `@cdk/src/main/java/de/roamingthings/CdkApp.java` - Application entry point
    - `@cdk/src/main/java/de/roamingthings/mylambda/boundary/MyLambdaStack.java` - Infrastructure
    - `@AGENTS.md` - Coding guidelines (automatically used by Claude Code)
5. **Execute prompts** from the "Initial Setup Prompts" section above
6. **Iterate** on your business requirements

## Key Infrastructure Features

The template provisions:

- **Lambda Function** with Java 25 runtime (ARM64)
- **SnapStart** enabled for faster cold starts
- **CloudWatch Logs** with JSON format and 1-day retention
- **X-Ray Tracing** active for distributed tracing
- **Lambda Insights** enabled for enhanced monitoring
- **Live Alias** for version management
- **1769 MB memory** allocation (1 vCPU equivalent)

Function naming follows: `{appName}-{resourceName}` convention
Stack naming follows: `{appName}-{stackName}-stack` convention

## Development Guidelines

This project follows strict coding conventions defined in [AGENTS.md](AGENTS.md). Key principles:

- BCE/ECB architecture pattern
- Java 25 modern syntax (var, records, pattern matching)
- Package-private visibility by default
- Static factory methods over constructors
- System.Logger for logging
- Simplicity over patterns (KISS, YAGNI)

Review [AGENTS.md](AGENTS.md) before contributing.
