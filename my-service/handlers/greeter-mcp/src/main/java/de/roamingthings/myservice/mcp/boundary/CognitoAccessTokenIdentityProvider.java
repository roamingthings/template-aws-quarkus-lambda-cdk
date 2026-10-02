package de.roamingthings.myservice.mcp.boundary;

import de.roamingthings.myservice.mcp.Requirement;
import io.quarkus.amazon.lambda.http.LambdaIdentityProvider;
import io.quarkus.amazon.lambda.http.model.AwsProxyRequest;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.runtime.QuarkusPrincipal;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import org.jspecify.annotations.Nullable;

import static de.roamingthings.myservice.mcp.Requirement.Rn.R3_1;
import static de.roamingthings.myservice.mcp.Requirement.Rn.R3_2;

@ApplicationScoped
class CognitoAccessTokenIdentityProvider implements LambdaIdentityProvider {

    @Override
    @Requirement({R3_1, R3_2})
    public @Nullable SecurityIdentity authenticate(AwsProxyRequest event) {
        var authorizer = event.getRequestContext().getAuthorizer();
        if (authorizer == null) {
            return null;
        }
        var claims = authorizer.getClaims();
        if (claims == null || claims.getSubject() == null || claims.getSubject().isBlank()) {
            return null;
        }
        return QuarkusSecurityIdentity.builder()
                .setPrincipal(new QuarkusPrincipal(claims.getSubject()))
                .build();
    }
}
