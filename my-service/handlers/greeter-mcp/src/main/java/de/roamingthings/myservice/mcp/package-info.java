/// # MCP
/// > Let an authenticated AI agent call the service's tools over the Model Context Protocol.
///
/// ## Boundary
/// - `greet` — answer a name with a greeting
/// - `refuse-event-stream` — turn away clients asking for a server-initiated event stream
/// - `identify-caller` — establish who is calling from the subject claim the gateway verified
///
/// ## Requirements
/// ### R1: Greet
/// - R1.1 — When an agent calls the `greet` tool with a name, the BC shall respond with `Hello, <name>!`.
///
/// ### R2: Refuse event streams
/// - R2.1 — If a client requests a server-initiated event stream, then the BC shall refuse it as method-not-allowed and
///   advertise that only POST and DELETE are accepted. _(why: a Lambda behind API Gateway cannot hold long-lived
///   streams, so clients must fall back to request/response mode)_
/// - R2.2 — When a client sends a protocol request, the BC shall process it without refusal.
///
/// ### R3: Identify the caller
/// - R3.1 — When a request carries a verified subject claim, the BC shall identify the caller by that subject.
/// - R3.2 — If a request carries no verified subject claim, then the BC shall leave the caller unidentified.
///
/// ## References
/// - [MCP 2025-03-26 — Streamable HTTP transport](https://modelcontextprotocol.io/specification/2025-03-26/basic/transports#streamable-http)
///   — request/response mode and the optional event stream (governs R2)
///
/// ## Out of scope
/// - Verifying access tokens; the gateway does that before a request arrives.
/// - Handling a missing or blank name.
@NullMarked
package de.roamingthings.myservice.mcp;

import org.jspecify.annotations.NullMarked;
