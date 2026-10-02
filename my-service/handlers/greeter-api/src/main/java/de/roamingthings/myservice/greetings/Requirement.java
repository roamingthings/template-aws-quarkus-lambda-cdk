package de.roamingthings.myservice.greetings;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// Generated from the capability spec in [de.roamingthings.myservice.greetings] — do not edit.
/// Marks the boundary method or test that realizes the given requirement statements.
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Requirement {

    /// One constant per statement id in the spec's `## Requirements`.
    enum Rn {
        /// While no custom greeting template is configured, when an item is submitted, the BC shall respond with the greeting `hello, <name> Quarkus on BCE`, where `<name>` is the item's name.
        R1_1("R1.1", "While no custom greeting template is configured, when an item is submitted, the BC shall respond with the greeting `hello, <name> Quarkus on BCE`, where `<name>` is the item's name."),
        /// Where a custom greeting template is configured, the BC shall respond with that template filled with the item's name.
        R1_2("R1.2", "Where a custom greeting template is configured, the BC shall respond with that template filled with the item's name.");

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
