# Java 25 Project Structure

## Project Overview

Three independent Gradle builds for AWS Lambda application:

- Java 25
- Quarkus for Lambda handlers
- AWS CDK (in Java) for infrastructure
- Isolated system tests

## Directory Structure

```
project-root/
├── gradle/
│   └── libs.versions.toml           # Shared version catalog
├── my-service/                # APPLICATION MODULE
│   ├── build.gradle.kts              # Root build with shared configuration
│   ├── settings.gradle.kts           # Multi-project build settings
│   ├── shared/
│   │   ├── shared-model/            # Shared domain models library
│   │   │   ├── build.gradle.kts
│   │   │   └── src/
│   │   │       ├── main/java/com/example/shared/model/
│   │   │       └── test/java/com/example/shared/model/
│   │   ├── shared-util/             # Shared utilities library (optional)
│   │   │   ├── build.gradle.kts
│   │   │   └── src/
│   │   └── shared-service/          # Shared services library (optional)
│   │       ├── build.gradle.kts
│   │       └── src/
│   └── handlers/
│       ├── create/
│       │   ├── build.gradle.kts
│       │   └── src/
│       │       ├── main/java/com/example/handlers/create/
│       │       │   └── CreateHandler.java
│       │       └── test/java/com/example/handlers/create/
│       ├── update/
│       │   ├── build.gradle.kts
│       │   └── src/
│       │       ├── main/java/com/example/handlers/update/
│       │       │   └── UpdateHandler.java
│       │       └── test/java/com/example/handlers/update/
│       └── delete/
│           ├── build.gradle.kts
│           └── src/
│               ├── main/java/com/example/handlers/delete/
│               │   └── DeleteHandler.java
│               └── test/java/com/example/handlers/delete/
│
├── cdk/                              # INFRASTRUCTURE MODULE
│   ├── build.gradle.kts              # Standalone build with application plugin
│   ├── settings.gradle.kts
│   └── src/main/java/com/example/infra/
│       └── LambdaStack.java
│
└── my-service-st/             # SYSTEM TESTS MODULE
    ├── build.gradle.kts              # Standalone build (NO app dependencies)
    ├── settings.gradle.kts
    └── src/test/java/com/example/systemtest/
        ├── CreateFlowTest.java
        ├── UpdateFlowTest.java
        └── EndToEndTest.java
```

## Module Descriptions

### `my-service/` - Application Module

Multi-project Gradle build containing:

**Root `build.gradle.kts`:**

- Shared configuration for all subprojects
- Uses Quarkus BOM via `enforcedPlatform` for version management
- Includes `shared/*` and `handlers/*` subprojects

```kotlin
subprojects {
    apply(plugin = "java")

    dependencies {
        implementation(enforcedPlatform("io.quarkus.platform:quarkus-bom:3.31.2"))
    }
}
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

Completely independent Gradle build:

**`build.gradle.kts`:**

- Standalone build with CDK dependencies from version catalog
- No dependency on application modules
- References handler JARs by filesystem path for deployment

```kotlin
dependencies {
    implementation(libs.aws.cdk.lib)
    implementation(libs.aws.cdk.constructs)
}
```

**Purpose:**

- Defines all AWS resources (Lambda functions, API Gateway, DynamoDB, etc.)
- Deploys handler JARs from `../my-service/handlers/*/build/`
- Independent lifecycle from application

### `my-service-st/` - System Tests Module

Completely independent Gradle build:

**`build.gradle.kts`:**

- Standalone build with AWS SDK BOM via `platform`
- **MUST NOT** depend on application or shared modules
- Only uses AWS SDK to interact with deployed resources

```kotlin
dependencies {
    implementation(platform(libs.aws.sdk.bom))
    implementation(libs.aws.sdk.lambda)
}
```

**Purpose:**

- Tests deployed AWS infrastructure end-to-end
- Invokes Lambda functions via AWS SDK
- Verifies API contracts, not implementation
- Simulates external consumer behavior

**Critical constraint:**
System tests MUST behave like a standalone external application with no access to application internals.

## Key Decisions

1. **Three independent top-level builds**: Application, Infrastructure, System Tests
2. **No root build file**: Each top-level build is self-contained
3. **Version catalog**: Shared `gradle/libs.versions.toml` for consistent dependency versions
4. **Isolated system tests**: No shared dependencies to force proper contract testing
5. **Application as multi-project**: Only the application build has subprojects (shared/* + handlers/*)
6. **Multiple shared libraries**: Organized under `shared/` directory by concern (model, util, service)
7. **CDK naming**: Common practice across languages for infrastructure code
8. **Minimal dependencies**: Each module only includes what it needs

## Build Strategy

### Local Development

Build modules independently:

```bash
# Build application
cd my-service
../gradlew clean build

# Build infrastructure
cd cdk
../gradlew clean build

# Build system tests
cd my-service-st
../gradlew clean build
```

### CI/CD Pipeline

1. Build `my-service/` → produces handler JARs
2. Build `cdk/` → uses handler JARs for deployment
3. Deploy via `cdk deploy`
4. Build and run `my-service-st/` → tests deployed resources

### Dependency Updates

Each module updates independently:

- **Dependabot**: Creates separate PRs per module
- **Quarkus CLI**: `quarkus update` works in `my-service/`
- **CDK updates**: Only affect `cdk/` module
- **Test dependencies**: Only affect `my-service-st/`

## Common Commands

```bash
# Application
cd my-service
../gradlew clean build                          # Build all shared libs and handlers
../gradlew :shared:shared-model:build           # Build specific shared lib
../gradlew :handlers:create:build               # Build specific handler

# Infrastructure
cd cdk
../gradlew clean build
cdk synth                           # Synthesize CloudFormation
cdk deploy                          # Deploy to AWS
cdk diff                            # Show changes

# System Tests
cd my-service-st
../gradlew clean test               # Run against deployed resources
../gradlew test --tests '*CreateFlowTest' # Run specific test
```

## Module Independence Benefits

1. **Separation of Concerns**: Application, Infrastructure, Testing are truly separate
2. **Independent Releases**: Update CDK without rebuilding handlers
3. **Team Ownership**: Different teams can own different modules
4. **Cleaner Dependencies**: Dependabot PRs are focused and clear
5. **True Contract Testing**: System tests can't accidentally use implementation details
6. **Repository Flexibility**: Easy to split into separate repos later

## Notes

- Replace `my-service` with actual application name
- Replace `com.example` with actual package name
- Each handler can have different Quarkus configurations (application.properties)
- CDK module references handler JARs via relative filesystem paths
- System tests must use only AWS SDK - no application code imports allowed
