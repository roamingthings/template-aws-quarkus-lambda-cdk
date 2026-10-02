/// # Greetings
/// > Answer an item with a greeting addressed to the item's name.
///
/// ## Boundary
/// - `greet` — answer an item with a greeting that names it
///
/// ## Requirements
/// ### R1: Greet an item by name
/// - R1.1 — While no custom greeting template is configured, when an item is submitted, the BC shall respond with the
///   greeting `hello, <name> Quarkus on BCE`, where `<name>` is the item's name.
/// - R1.2 — Where a custom greeting template is configured, the BC shall respond with that template filled with the
///   item's name.
///
/// ## Out of scope
/// - Handling an item with a missing or blank name.
package de.roamingthings.myservice.greetings;
