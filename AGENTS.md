# Coding Guidelines

The keywords "MUST", "MUST NOT", "SHOULD", "SHOULD NOT", and "MAY" in this document are to be interpreted as described
in [RFC 2119](https://www.rfc-editor.org/rfc/rfc2119).

## Java Version & Syntax

- MUST use Java 25 with modern syntax (var, pattern matching, records, text blocks)
- MUST use Java var keyword for local variables
- SHOULD prefer dependencies in this order: Java SE, MicroProfile, Jakarta EE
- SHOULD use Java SE APIs over writing custom code
- SHOULD prefer unchecked over checked exceptions; MUST NOT throw generic exceptions like java.lang.Exception
- MUST throw RuntimeException subclasses, not directly; MUST inherit from WebApplicationException in JAX-RS projects
- MAY use Java records instead of classes with final fields
- SHOULD prefer factory methods in records over passing null in constructors

## Logging

- MUST use java.lang.System.Logger instead of System.out statements
- MUST NOT use java.util.logging.Logger
- MUST name Logger fields LOGGER (uppercase) and mark them as static final
- MUST use the following semantics for log levels:
    - **Debug:** Debug information for development
    - **Info:** "As expected by default" - normal operations
    - **Warn:** "To be looked at tomorrow" - potential issues
    - **Error:** "Wake me up in the middle of the night" - critical failures

## BCE/ECB Architecture

- MUST structure code using the Boundary Control Entity (BCE/ECB) pattern
- MUST follow package structure: [ORGANIZATION_NAME].[PROJECT_NAME].[COMPONENT_NAME].[boundary|control|entity]
- MUST name top level package after the application responsibility or name
- MUST name business components after their responsibilities as children of top level package
- MUST place boundary, control, entity packages only in business components
- MUST place classes with cross-cutting functionality in the root application package
- MAY access control package contents directly; not every BCE component needs a dedicated boundary package
- MUST NOT explain the BCE pattern in documentation

## Package Naming

- MUST create an application level package with a name derived from a maven project or context
- MUST name packages after their domain responsibilities
- SHOULD create package-info.java for top level packages with JavaDoc documenting design decisions and
  responsibilities (not contents)
- SHOULD NOT create package-info.java for components with obvious meaning; MUST describe the "Why?" not implementation
  details

## Boundary Layer

- MUST keep coarse-grained classes in the boundary package
- MUST place facades in the boundary package
- MUST place health checks in the boundary package
- MUST place @Transactional only in boundary layer
- SHOULD use ApplicationScope if there is no Boundary stereotype

## Control Layer

- MUST implement procedural business logic in the control package
- SHOULD prefer interfaces with static methods over classes for stateless/procedural logic
- SHOULD prefer static over default methods in utility interfaces

## Entity Layer

- MUST maintain domain objects, data classes, and entities in the entity package
- MUST ensure entities maintain state and corresponding behavior
- SHOULD model value objects as enums

## Class Naming Conventions

- MUST name classes after their responsibilities
- SHOULD NOT use meaningless suffixes: *Impl, *Service, *Manager, *Creator
- MUST NOT end class names with "Control"
- MUST use "Resource" suffix only for JAX-RS classes
- MUST use "Factory" suffix only for actual GoF Factory patterns
- MUST use "Builder" suffix only for classes with typical builder structure (method chaining)
- SHOULD end stacks with "Stack" suffix

## Visibility & Modifiers

- SHOULD NOT use private visibility; SHOULD prefer package-private (default) visibility
- SHOULD NOT use "private static" methods; SHOULD prefer default visibility
- MUST NOT use final for fields (exception: static final for LOGGER)
- MUST NOT use constructor injection

## Interfaces & Classes

- MUST use interfaces only with multiple implementations or for strategy patterns
- MUST NOT create interfaces with abstract methods implemented by a single class; MUST use classes directly
- MAY create multiple classes only if it decreases complexity and increases readability

## Method Naming & Design

- SHOULD NOT use "getter" methods starting with "get"; SHOULD prefer record convention (e.g., configuration() not
  getConfiguration())
- SHOULD keep methods short and testable
- SHOULD create well-named methods for coarse-grained, cohesive, self-contained logic
- MUST extract lambdas requiring multiple statements or braces {} into well-named helper methods
- MUST NOT create multiline lambda expressions; MUST use method references instead
- MUST extract repeated calculations or string concatenations into helper methods (DRY principle)
- MUST NOT create empty delegates which just call methods without added value

## Stream & Collections

- SHOULD prefer java.util.stream.Stream API over for loops
- SHOULD NOT use forEach; SHOULD prefer Stream methods
- SHOULD prefer Stream.of to Arrays.stream
- SHOULD prefer toList() to .collect(Collectors.toList())
- SHOULD prefer List.of over String[] or new ArrayList<>()
- SHOULD NOT create unnecessary intermediate collections when streaming arrays
- SHOULD prefer variable declaration over lengthy method chaining

## Code Style

- MUST use spaces for indentation (no tabs)
- SHOULD prefer multiple simpler lines to one more complex line
- SHOULD prefer multiline Strings (text blocks) over String concatenations
- SHOULD prefer imports over fully qualified class names
- SHOULD use "this" to reference instance fields
- MUST remove unused imports
- SHOULD extract variables to eliminate duplication
- SHOULD prefer enums over plain Strings for finite, well-defined values
- MAY reuse enum constants as values; enum constants do not have to follow naming conventions
- SHOULD prefer try-with-resources over explicitly closing resources

## Simplicity Principles

- MUST keep the design KISS and YAGNI
- MUST implement the simplest possible solution
- MUST write simple code first; MUST ask before implementing enhancements or optional features
- MUST NOT over-engineer; MUST ask about adding optional features or extension points
- MUST create new components with minimal business logic and essential fields only
- MUST NOT implement premature optimization
- MUST optimize only when performance becomes a measurable problem
- MUST NOT force patterns like DRY if it requires excessive complexity (multiple nested if-statements or growing from a
  few lines to tens of lines)

## Exceptions and Error Handling

- MAY create custom exceptions only if it significantly improves robustness or maintainability
- SHOULD use explicit exceptions like BadRequestException for Response.Status.BAD_REQUEST
- MUST use WebApplicationException in compact constructors in JAX-RS projects
- MUST NOT re-throw exceptions with "throw e" without adding value
- SHOULD use Data Oriented Programming with Result types over exceptions
- SHOULD use sealed interface hierarchies with record subtypes for result types
    - Example: `MyUseCaseResult` → `Success | InvalidRequest | ServiceFailed`
- SHOULD encapsulate success data or error details including original exceptions into result types
- MUST adapt to error handling patterns in existing projects

## JavaDoc

- MUST NOT write obvious JavaDoc comments that rephrase code
- MUST document the intentions and the "why", not implementation details
- MUST either describe the "why" or not comment at all
- SHOULD follow links in JavaDoc to external specifications and use them for code generation
- MAY use popular, also funny, technical terms from the Java SE, MicroProfile and Jakarta EE ecosystems as examples in
  unit tests and javadoc

## README Guidelines

- MUST write brief, 'to the point' README.md files for advanced developers
- MUST use precise and concise language; MUST NOT use generic adjectives like "simple", "lightweight"
- MUST NOT include detailed project structure (file/folder listings); MAY include high-level module descriptions
- MUST NOT list REST resources in READMEs
- SHOULD provide links if modules are listed
- MUST NOT use "Orchestrates" term; MUST use more specific alternatives

## Testing

- MUST NOT start unit test methods with "test" or "should"
- SHOULD NOT write repetitive or trivial unit tests; SHOULD keep only essential tests verifying core functionality
- MUST NOT write tests for implementations that cannot fail (enums, records, getters/setters)
- SHOULD create minimalistic tests first
- SHOULD generate at most three UTs, ITs or STs
- MUST use AssertJ library instead of JUnit assertions
- SHOULD NOT use less specific checks (startsWith, isNotNull) when isEqualTo assertion is present
- MUST NOT use private visibility in tests

## Integration Tests

- MUST end integration tests with IT suffix; executed by the failsafe maven plugin (no configuration necessary)

## System Tests (ST)

- MUST create system tests in a dedicated Maven module ending with "-st"
- MUST use microprofile-rest-client for testing JAX-RS resources
- MUST place REST client interfaces in src/main/java of the -st module
- MUST place test classes in src/test/java of the -st module
- MUST name client interfaces after the resource with "Client" suffix (e.g., GreetingsResource ->
  GreetingsResourceClient)
- MUST use RegisterRestClient configKey: "service_uri"
- MUST end STs with "IT" suffix
- MUST NOT use RestAssured; MUST write e2e test in the -st module

## JAX-RS

- SHOULD name resources in plural (e.g., SpeakersResource not SpeakerResource)
- SHOULD declare @Consumes and @Produces on class-level
- MUST NOT implement business logic in JAX-RS resources; MUST delegate instead
- SHOULD prefer returning JAX-RS Response over JsonObject in resources
- MUST NOT create new "@RegisterRestClient(configKey," - MUST reuse existing

## JSON Serialization (JSON-P)

- SHOULD prefer JSON-P over JSON-B
- SHOULD ship record entities with toJSON method returning a JSON-P object
- MUST map JSON-P in the boundary to entities
- SHOULD create record entities from JSON-P JsonObject in static method: fromJSON(JsonObject json)

## HTTP Client

- SHOULD prefer synchronous HTTPClient APIs
- MUST use asynchronous Http APIs (HttpClient.sendAsync) only if explicitly requested

## Project Management

- MUST ask before changing pom.xml
- MUST NOT create or change any files on opening existing projects; MUST stop after initialization and wait for
  instructions
- MUST NOT generate code initially in an empty project
- MUST NOT create Maven pom.xml for Java 25 CLI applications
- MUST execute Java 25 CLI applications in source-file mode
- MUST NOT use quarkus-hibernate-validator
- MUST create metrics and observability features with OTEL / opentelemetry

## Unnamed Classes

- MUST NOT import packages available in java.base module in unnamed classes
- MUST NOT use static methods in unnamed classes

## Version Control

- MUST use Git for version control
- MUST use conventional commits for commit messages and pull request titles
- MUST use pull requests for code review and merging
- MUST use semantic versioning for releases
