# Bootstrapping a New Project from This Template

This document provides instructions for AI coding agents (Claude Code, Cursor, etc.) to scaffold a new project
from this template. It can also be used as a manual reference.

## Usage

**From within the cloned template:**

```
/bootstrap
```

**From an empty directory (Claude Code):**

```
Set up a new service using https://github.com/roamingthings/template-aws-quarkus-lambda-cdk
```

The agent will clone the template, read this file, and walk you through the setup.

### Cloning into a Non-Empty Directory

When the user starts Claude Code in an empty directory, the `.claude/` configuration directory is created
automatically before the agent runs. This means `git clone <url> .` will fail because the directory is not empty.

**Workaround:** Clone into a temporary subdirectory and move the contents:

```bash
git clone <url> .bootstrap-tmp
mv .bootstrap-tmp/* .bootstrap-tmp/.* . 2>/dev/null
rm -rf .bootstrap-tmp
```

Then continue with the bootstrapping steps.

## Questions to Ask

Gather the following from the user before making any changes. Present all questions together in a single message.

| # | Question | Example | Default | Validation |
|---|----------|---------|---------|------------|
| 1 | **Project name** (kebab-case, used for directories, CDK app name, resource naming) | `inventory-service` | — | Must be kebab-case, no underscores |
| 2 | **Java base package** (the application-level package for all handler code) | `com.acme.inventory` | — | Valid Java package segments |
| 3 | **Gradle group** (the Maven/Gradle group ID for the project) | `com.acme` | Same as base package | Valid Java package segments |
| 4 | **Which handlers to include?** (multi-select) | REST API + MCP Server | All three | At least one must be selected |
| 5 | **Keep shared module?** (`shared/shared-model` with cross-handler model classes) | yes | yes | — |

### Handler Options

| Handler | Directory | Description |
|---------|-----------|-------------|
| **Event Handler** | `handlers/greeter` | Generic Lambda (`RequestHandler<Input, Output>`), for SQS, DynamoDB streams, etc. |
| **REST API** | `handlers/greeter-api` | JAX-RS resource behind API Gateway |
| **MCP Server** | `handlers/greeter-mcp` | Quarkus MCP Server with OAuth2/Cognito authentication |

### Derived Values

Calculate these from the user's answers — do not ask for them separately.

> **IMPORTANT — Package naming:** The Java base package provided by the user IS the application package.
> Do NOT append the project name to it. For example, if the user says base package `de.roamingthings.flowers`
> and project name `flowers`, the application package is `de.roamingthings.flowers` — NOT
> `de.roamingthings.flowers.flowers`.

| Value | Derivation | Example (project: `inventory-service`, base package: `com.acme.inventory`, group: `com.acme`) |
|-------|-----------|---------|
| `APP_PACKAGE` | The Java base package as provided by the user | `com.acme.inventory` |
| `APP_PACKAGE_PATH` | `APP_PACKAGE` with dots replaced by `/` | `com/acme/inventory` |
| `GRADLE_GROUP` | The Gradle group as provided (or defaults to `APP_PACKAGE`) | `com.acme` |
| `STACK_CLASS` | Project name in PascalCase + `Stack` | `InventoryServiceStack` |
| `STACK_PROPS_CLASS` | `STACK_CLASS` + `Props` | `InventoryServiceStackProps` |

**Show the user a confirmation summary** with all derived values before making any changes. Example:

```
Project name:       inventory-service
Java base package:  com.acme.inventory
Gradle group:       com.acme
Handlers:           REST API, MCP Server
Shared module:      keep
Stack class:        InventoryServiceStack

Proceed? (yes/no)
```

## Transformation Steps

Execute these steps in order. Each step lists exactly what to change.

### Step 1: Remove Template Git History

Always remove the template's git history and reinitialize:

```bash
rm -rf .git
git init
```

### Step 2: Rename Service Directories

```
my-service/        → {PROJECT_NAME}/
my-service-st/     → {PROJECT_NAME}-st/
```

### Step 3: Update Root `settings.gradle.kts`

```kotlin
rootProject.name = "{PROJECT_NAME}"

includeBuild("{PROJECT_NAME}")
includeBuild("cdk")
includeBuild("{PROJECT_NAME}-st")
```

### Step 4: Update `{PROJECT_NAME}/settings.gradle.kts`

```kotlin
rootProject.name = "{PROJECT_NAME}"

// Include only selected handlers:
include("shared:shared-model")    // only if shared module is kept
include("handlers:greeter")       // only if Event Handler selected
include("handlers:greeter-api")   // only if REST API selected
include("handlers:greeter-mcp")   // only if MCP Server selected
```

### Step 5: Update `{PROJECT_NAME}-st/settings.gradle.kts`

```kotlin
rootProject.name = "{PROJECT_NAME}-st"
```

### Step 6: Update Gradle `group` in All `build.gradle.kts`

Replace `group = "de.roamingthings"` with `group = "{GRADLE_GROUP}"` in:

- `{PROJECT_NAME}/build.gradle.kts`
- `cdk/build.gradle.kts`
- `{PROJECT_NAME}-st/build.gradle.kts`

### Step 7: Update CDK Application

**`cdk/src/main/java/.../CdkApp.java`:**
- Change `APP_NAME = "my-service"` to `APP_NAME = "{PROJECT_NAME}"`
- Change `Tags.of(app).add("project", "template-aws-quarkus-lambda-cdk")` to use `{PROJECT_NAME}`
- Update import of `MyServiceStack` to `{STACK_CLASS}`
- Update constructor call: `new MyServiceStack(...)` → `new {STACK_CLASS}(...)`
- Update `"MyServiceStack"` CDK ID string to `"{STACK_CLASS}"`
- Update `MyServiceStack.MyServiceStackProps` to `{STACK_CLASS}.{STACK_PROPS_CLASS}`

**`cdk/src/main/java/.../MyServiceStack.java`:**
- Rename file to `{STACK_CLASS}.java`
- Rename class to `{STACK_CLASS}`
- Rename inner record to `{STACK_PROPS_CLASS}`
- Update constructor and all references

**`cdk/build.gradle.kts`:**
- Update `mainClass.set("de.roamingthings.CdkApp")` to `mainClass.set("{APP_PACKAGE}.CdkApp")`

Note: CDK root-level classes (`CdkApp`, `Configuration`, `ConventionalDefaults`, etc.) use the application
package, not a separate CDK package.

### Step 8: Rename Java Packages

Rename all Java source directories and update package declarations and imports.

> **CRITICAL — Replacement order matters.** Always replace the most specific patterns first to avoid
> double-replacement. For example, replace `de.roamingthings.myservice` before `de.roamingthings`,
> otherwise `de.roamingthings.myservice` would become `{APP_PACKAGE}.myservice` instead of `{APP_PACKAGE}`.

> **CRITICAL — Moving files safely.** When moving files from old package directories to new ones,
> first create the new directory structure, then copy/move files, then delete ONLY the old directories
> that are now empty. Never use recursive delete (`rm -rf`) on a parent directory that contains both
> old and new paths (e.g., if old is `de/roamingthings/myservice/` and new is `de/roamingthings/flowers/`,
> do NOT `rm -rf de/roamingthings/` as it would delete the new path too).

**Source package mapping:**

| Old | New |
|-----|-----|
| `de/roamingthings/myservice/` | `{APP_PACKAGE_PATH}/` |
| `de/roamingthings/shared/` | `{APP_PACKAGE_PATH}/../shared/` (keep under same parent as app package) |
| `de/roamingthings/` (CDK root classes only) | `{APP_PACKAGE_PATH}/` |

**In all `.java` files**, replace in this exact order:
1. `de.roamingthings.myservice` → `{APP_PACKAGE}` (most specific first)
2. `de.roamingthings.shared` → `{APP_PACKAGE without last segment}.shared` (e.g., `com.acme.shared`)
3. `de.roamingthings` → `{APP_PACKAGE}` (CDK root classes — least specific last)

**Move source files** to match new package paths in:
- `{PROJECT_NAME}/handlers/*/src/main/java/`
- `{PROJECT_NAME}/handlers/*/src/test/java/`
- `{PROJECT_NAME}/shared/shared-model/src/main/java/`
- `{PROJECT_NAME}-st/src/main/java/`
- `{PROJECT_NAME}-st/src/test/java/`
- `cdk/src/main/java/`

After moving, verify new directories are correct, then delete only the empty old package directories.

### Step 9: Update CDK Module Paths

In `{STACK_CLASS}.java` and `McpApiConstruct.java`, update Lambda function module paths:

```java
// Old
.modulePath("../my-service/handlers/greeter")
.modulePath("../my-service/handlers/greeter-api")
.modulePath("../my-service/handlers/greeter-mcp")

// New
.modulePath("../{PROJECT_NAME}/handlers/greeter")
.modulePath("../{PROJECT_NAME}/handlers/greeter-api")
.modulePath("../{PROJECT_NAME}/handlers/greeter-mcp")
```

### Step 10: Update Configuration

**`cdk/src/main/java/.../Configuration.java`:**
- `Configuration.ofAppNamed("my-service")` is driven by `CdkApp.APP_NAME` — no direct change needed
- But if user has `~/.my-service/app.properties`, note the new path will be `~/.{PROJECT_NAME}/app.properties`

**`cdk/cdk.json`:**
- The `"app"` command (`"../gradlew run --console=plain"`) does not change (gradlew stays at root)

### Step 11: Remove Unselected Handlers

For each handler **not** selected by the user:

**If Event Handler is removed:**
- Delete `{PROJECT_NAME}/handlers/greeter/`
- In `{STACK_CLASS}.java`: remove `createGreeterHandler()` method and its call
- Remove `greeterHandlerFunction` variable and related `CfnOutput` entries
- In `{PROJECT_NAME}-st/`: remove `GreeterHandlerIT.java`

**If REST API is removed:**
- Delete `{PROJECT_NAME}/handlers/greeter-api/`
- In `{STACK_CLASS}.java`: remove `createGreeterApiHandler()` method, `createApiGateway()` method, `addApiGatewayBehavior()` method, and their calls
- Remove `greeterApiHandlerFunction` variable and related `CfnOutput` entries
- Remove `/api/*` CloudFront behavior
- In `{PROJECT_NAME}-st/`: remove `GreetingsResourceClient.java`, `GreetingsResourceIT.java`, and related entities

**If MCP Server is removed:**
- Delete `{PROJECT_NAME}/handlers/greeter-mcp/`
- In `{STACK_CLASS}.java`: remove all Cognito-related methods (`createUserPool`, `createTestUser`, `createMcpResourceServerScopes`, `createAgentUserPoolClient`, `createUserPoolDomain`), `McpApiConstruct` usage, `addMcpApiGatewayBehavior()`, `deployOAuthServerMetadata()`, `OAuthFrontendConstruct` usage, and `MCP_CALLBACK_URLS`
- Delete `McpApiConstruct.java`
- Delete `OAuthFrontendConstruct.java`
- Remove Cognito/MCP-related `CfnOutput` entries
- Remove MCP section from README

**If only one handler remains and it is the REST API:**
- The CloudFront distribution, `OAuthFrontendConstruct`, and S3 frontend are only needed for MCP. If MCP is removed, the REST API can be served directly via API Gateway without CloudFront.

### Step 12: Remove Shared Module (if not kept)

If the user chose not to keep the shared module:

1. **Copy shared classes into each remaining handler** that uses them.
   Currently `Item.java` (record in `shared.model.entity`) is used by the `greeter` and `greeter-api` handlers.
   For each handler that imports from the shared module:
   - Copy the class into the handler's entity package (e.g., `{APP_PACKAGE}.greetings.entity.Item`)
   - Update the package declaration and imports in the copied file
   - Update all imports in the handler's source and test files to point to the new location

2. **Remove shared module artifacts:**
   - Delete `{PROJECT_NAME}/shared/` directory
   - Remove `include("shared:shared-model")` from `{PROJECT_NAME}/settings.gradle.kts`
   - Remove `implementation(project(":shared:shared-model"))` from handler `build.gradle.kts` files

3. **Update system tests** (`{PROJECT_NAME}-st/`) — the ST module has its own copy of model classes
   and does not depend on shared-model, so no changes needed there.

### Step 13: Update `buildAndDeployDontAsk.sh`

Replace `my-service` references with `{PROJECT_NAME}`.

### Step 14: Update README.md

- Replace title with project name
- Remove references to unselected handlers
- Update `~/.my-service/app.properties` path to `~/.{PROJECT_NAME}/app.properties`
- Update directory names in the project structure section
- Remove the "Using This Template with Coding Agents" section (no longer a template)
- Remove reference to `BOOTSTRAP.md`

### Step 15: Clean Up Template Files

- Delete `BOOTSTRAP.md` (this file)
- Delete the `openspec/` directory if present (template development artifacts)
- Update `CLAUDE.md` — remove any bootstrap-related instructions, keep only `@AGENTS.md`

### Step 16: Create Initial Commit

```bash
git add .
git commit -m "feat: initialize {PROJECT_NAME} from template"
```

### Step 17: Verify Build

Run the build to verify everything compiles:

```bash
cd {PROJECT_NAME}
../gradlew clean build
```

```bash
cd cdk
../gradlew clean build
```

## Post-Setup Guidance

After bootstrapping, suggest the user:

1. Create `~/.{PROJECT_NAME}/app.properties` with their AWS configuration
2. Rename handler classes and business components to match their domain
3. Update handler business logic for their use case
4. Review and customize CDK infrastructure as needed
