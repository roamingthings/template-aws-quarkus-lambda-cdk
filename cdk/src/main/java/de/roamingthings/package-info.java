/// # my-service
/// > Serve greetings to Lambda callers, REST clients and AI agents from one AWS stack.
///
/// ## Components
/// - `myservice` (cdk) provisions three independently deployed handler modules, each its own source root and spec:
///   `greeter` (`greetings`, invoked directly), `greeter-api` (`greetings`, reached via `/api`) and
///   `greeter-mcp` (`mcp`, reached via `/mcp`).
/// - Handler modules never call each other; they share only the Item definition from the shared-model library.
/// - `mcp` trusts the caller identity verified by the gateway `myservice` provisions; it never verifies tokens itself.
///
/// ## System invariants
/// - S1 — If a log group lacks a retention period, then the system shall fail synthesis.
/// - S2 — If a log group retains logs for longer than ten years, then the system shall warn at synthesis.
/// - S3 — The system shall tag every resource with its project, environment and application.
/// - S4 — Where an AWS application tag is configured, the system shall tag every resource with it.
///
/// ## Ubiquitous language
/// - Item — the thing a caller wants greeted: an id, a name and a description.
/// - Greeting — the text answered to a caller.
///
/// ## Decisions
/// - D1 — Capability specs live in each BC's package doc (SBCE); `openspec/` is retired. _(why: one source of truth
///   co-located with the code it governs; rejected: keeping `openspec/` as a parallel spec tree)_
/// - D2 — Each handler module is its own deployable system with its own `greetings`/`mcp` spec. _(why: modules deploy
///   and behave independently; rejected: one shared `greetings` spec, renaming packages to distinct BCs)_
/// - D3 — The migration specs characterize current behaviour; the three greeting variants stay divergent. _(why: no
///   behaviour change during migration; rejected: unifying on a name-based greeting)_
/// - D4 — CDK control-only packages (compliance, configuration, encryption, function) carry no spec; their
///   cross-cutting rules are system invariants. _(why: control is implementation; rejected: a spec per CDK package)_
///
/// ## Stack
/// - application: microprofile-server (Quarkus on AWS Lambda, Gradle) · base package `de.roamingthings.myservice` ·
///   modules `my-service/handlers/{greeter,greeter-api,greeter-mcp}`
/// - infrastructure: aws-cdk · base package `de.roamingthings` · `cdk/`
package de.roamingthings;
