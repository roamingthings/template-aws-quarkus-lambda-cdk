# Java 25 Project Structure

## Project Overview

Three independent Maven projects for AWS Lambda application:

- Java 25
- Quarkus for Lambda handlers
- AWS CDK (in Java) for infrastructure
- Isolated system tests

## Directory Structure

```
project-root/
├── my-lambda/                # APPLICATION MODULE
│   ├── pom.xml                       # Independent POM with BOM imports
│   ├── shared/
│   │   ├── shared-model/            # Shared domain models library
│   │   │   ├── pom.xml
│   │   │   └── src/
│   │   │       ├── main/java/com/example/shared/model/
│   │   │       └── test/java/com/example/shared/model/
│   │   ├── shared-util/             # Shared utilities library (optional)
│   │   │   ├── pom.xml
│   │   │   └── src/
│   │   └── shared-service/          # Shared services library (optional)
│   │       ├── pom.xml
│   │       └── src/
│   └── handlers/
│       ├── create/
│       │   ├── pom.xml
│       │   └── src/
│       │       ├── main/java/com/example/handlers/create/
│       │       │   └── CreateHandler.java
│       │       └── test/java/com/example/handlers/create/
│       ├── update/
│       │   ├── pom.xml
│       │   └── src/
│       │       ├── main/java/com/example/handlers/update/
│       │       │   └── UpdateHandler.java
│       │       └── test/java/com/example/handlers/update/
│       └── delete/
│           ├── pom.xml
│           └── src/
│               ├── main/java/com/example/handlers/delete/
│               │   └── DeleteHandler.java
│               └── test/java/com/example/handlers/delete/
│
├── cdk/                              # INFRASTRUCTURE MODULE
│   ├── pom.xml                       # Independent POM
│   └── src/main/java/com/example/infra/
│       └── LambdaStack.java
│
└── my-lambda-st/             # SYSTEM TESTS MODULE
    ├── pom.xml                       # Independent POM (NO app dependencies)
    └── src/test/java/com/example/systemtest/
        ├── CreateFlowTest.java
        ├── UpdateFlowTest.java
        └── EndToEndTest.java
```

## Module Descriptions

### `my-lambda/` - Application Module

Multi-module Maven project containing:

**Root `pom.xml`:**

- Parent POM for application only
- Uses Quarkus BOM import for version management
- Manages `shared/*` and `handlers/*` modules

```xml

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.quarkus.platform</groupId>
            <artifactId>quarkus-bom</artifactId>
            <version>3.17.0</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<modules>
<module>shared/shared-model</module>
<module>handlers/create</module>
<module>handlers/update</module>
<module>handlers/delete</module>
</modules>
```

**`shared/*` modules:**

- Multiple shared libraries organized by concern
- `shared-model/`: Domain models and DTOs (API contracts)
- `shared-util/`: Common utilities (optional)
- `shared-service/`: Shared business logic (optional)
- Pure Java libraries (no Quarkus dependencies)
- Used by handler modules as needed

**`handlers/*` modules:**

- Individual Quarkus Lambda handlers
- Each depends on required `shared/*` modules
- Each builds its own JAR (JVM or native)
- Includes `quarkus-amazon-lambda` dependency

### `cdk/` - Infrastructure Module

Completely independent Maven project:

**`pom.xml`:**

- Standalone POM with CDK dependencies
- No dependency on application modules
- References handler JARs by filesystem path for deployment

```xml

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>software.amazon.awscdk</groupId>
            <artifactId>aws-cdk-lib</artifactId>
            <version>2.165.0</version>
        </dependency>
    </dependencies>
</dependencyManagement>
```

**Purpose:**

- Defines all AWS resources (Lambda functions, API Gateway, DynamoDB, etc.)
- Deploys handler JARs from `../my-lambda/handlers/*/target/`
- Independent lifecycle from application

### `my-lambda-st/` - System Tests Module

Completely independent Maven project:

**`pom.xml`:**

- Standalone POM with AWS SDK BOM import
- **MUST NOT** depend on application or shared modules
- Only uses AWS SDK to interact with deployed resources

```xml

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>software.amazon.awssdk</groupId>
            <artifactId>bom</artifactId>
            <version>2.29.0</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

**Purpose:**

- Tests deployed AWS infrastructure end-to-end
- Invokes Lambda functions via AWS SDK
- Verifies API contracts, not implementation
- Simulates external consumer behavior

**Critical constraint:**
System tests MUST behave like a standalone external application with no access to application internals.

## Key Decisions

1. **Three independent top-level modules**: Application, Infrastructure, System Tests
2. **No root parent POM**: Each top-level module is self-contained
3. **BOM imports**: Version management via Bill of Materials (Quarkus standard approach)
4. **Isolated system tests**: No shared dependencies to force proper contract testing
5. **Application as multi-module**: Only the application module has sub-modules (shared/* + handlers/*)
6. **Multiple shared libraries**: Organized under `shared/` directory by concern (model, util, service)
7. **CDK naming**: Common practice across languages for infrastructure code
8. **Minimal dependencies**: Each module only includes what it needs

## Build Strategy

### Local Development

Build modules independently:

```bash
# Build application
cd my-lambda
mvn clean install

# Build infrastructure
cd cdk
mvn clean install

# Build system tests
cd my-lambda-st
mvn clean install
```

### CI/CD Pipeline

1. Build `my-lambda/` → produces handler JARs
2. Build `cdk/` → uses handler JARs for deployment
3. Deploy via `cdk deploy`
4. Build and run `my-lambda-st/` → tests deployed resources

### Dependency Updates

Each module updates independently:

- **Dependabot**: Creates separate PRs per module
- **Quarkus CLI**: `quarkus update` works in `my-lambda/`
- **CDK updates**: Only affect `cdk/` module
- **Test dependencies**: Only affect `my-lambda-st/`

## Common Commands

```bash
# Application
cd my-lambda
mvn clean install                              # Build all shared libs and handlers
mvn clean install -pl shared/shared-model      # Build specific shared lib
mvn clean install -pl handlers/create -am      # Build specific handler with dependencies
mvn clean install -Pnative                     # Native build

# Infrastructure
cd cdk
mvn clean install
cdk synth                           # Synthesize CloudFormation
cdk deploy                          # Deploy to AWS
cdk diff                            # Show changes

# System Tests
cd my-lambda-st
mvn verify                          # Run against deployed resources
mvn test -Dtest=CreateFlowTest      # Run specific test
```

## Module Independence Benefits

1. **Separation of Concerns**: Application, Infrastructure, Testing are truly separate
2. **Independent Releases**: Update CDK without rebuilding handlers
3. **Team Ownership**: Different teams can own different modules
4. **Cleaner Dependencies**: Dependabot PRs are focused and clear
5. **True Contract Testing**: System tests can't accidentally use implementation details
6. **Repository Flexibility**: Easy to split into separate repos later

## Notes

- Replace `my-lambda` with actual application name
- Replace `com.example` with actual package name
- Each handler can have different Quarkus configurations (application.properties)
- CDK module references handler JARs via relative filesystem paths
- System tests must use only AWS SDK - no application code imports allowed
