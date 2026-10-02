package de.roamingthings.myservice.mcp;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// Generated from the capability spec in [de.roamingthings.myservice.mcp] — do not edit.
/// Marks the boundary method or test that realizes the given requirement statements.
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Requirement {

    /// One constant per statement id in the spec's `## Requirements`.
    enum Rn {
        /// When an agent calls the `greet` tool with a name, the BC shall respond with `Hello, <name>!`.
        R1_1("R1.1", "When an agent calls the `greet` tool with a name, the BC shall respond with `Hello, <name>!`."),
        /// If a client requests a server-initiated event stream, then the BC shall refuse it as method-not-allowed and advertise that only POST and DELETE are accepted.
        R2_1("R2.1", "If a client requests a server-initiated event stream, then the BC shall refuse it as method-not-allowed and advertise that only POST and DELETE are accepted."),
        /// When a client sends a protocol request, the BC shall process it without refusal.
        R2_2("R2.2", "When a client sends a protocol request, the BC shall process it without refusal."),
        /// When a request carries a verified subject claim, the BC shall identify the caller by that subject.
        R3_1("R3.1", "When a request carries a verified subject claim, the BC shall identify the caller by that subject."),
        /// If a request carries no verified subject claim, then the BC shall leave the caller unidentified.
        R3_2("R3.2", "If a request carries no verified subject claim, then the BC shall leave the caller unidentified.");

        String id;
        String statement;

        Rn(String id, String statement) {
            this.id = id;
            this.statement = statement;
        }

        public String statement() {
            return this.statement;
        }

        @Override
        public String toString() {
            return this.id;
        }
    }

    Rn[] value();
}
