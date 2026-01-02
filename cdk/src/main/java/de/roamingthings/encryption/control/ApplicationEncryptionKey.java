package de.roamingthings.encryption.control;

import de.roamingthings.ConventionalDefaults;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.services.kms.IKey;
import software.amazon.awscdk.services.kms.Key;
import software.constructs.Construct;

import java.util.List;
import java.util.Optional;

public interface ApplicationEncryptionKey {

    static IKey create(@NonNull Construct scope,
                       @NonNull String appName,
                       @Nullable List<ManagedServicePolicy.AwsService> grantEncryptionVia) {
        var key = Key.Builder.create(scope, "ApplicationEncryptionKey")
                .alias(ConventionalDefaults.resourceName(appName, "ApplicationEncryptionKey"))
                .description("Key to encrypt %s".formatted(appName))
                .removalPolicy(RemovalPolicy.DESTROY)
                .build();

        Optional.ofNullable(grantEncryptionVia)
                .orElse(List.of())
                .forEach(service -> key.addToResourcePolicy(
                        ManagedServicePolicy.generatePolicyStatement(Stack.of(scope),
                                service))
                );

        return key;
    }
}
