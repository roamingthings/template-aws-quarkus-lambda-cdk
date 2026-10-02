/// # Greetings
/// > Answer an item with the service's configured greeting.
///
/// ## Boundary
/// - `greet` — answer an item with a greeting
///
/// ## Requirements
/// ### R1: Greet an item
/// - R1.1 — While no custom greeting is configured, when an item is submitted, the BC shall respond with the greeting
///   `hello, Quarkus on BCE`.
/// - R1.2 — Where a custom greeting is configured, the BC shall respond with that greeting.
///
/// ## Out of scope
/// - Personalising the greeting with the item's name.
/// - Handling an item with a missing or blank name.
package de.roamingthings.myservice.greetings;
