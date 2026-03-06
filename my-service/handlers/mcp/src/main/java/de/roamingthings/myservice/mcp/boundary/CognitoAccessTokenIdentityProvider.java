package de.roamingthings.myservice.mcp.boundary;

import io.quarkus.amazon.lambda.http.LambdaIdentityProvider;
import io.quarkus.amazon.lambda.http.model.AwsProxyRequest;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.runtime.QuarkusPrincipal;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import org.jspecify.annotations.Nullable;

@ApplicationScoped
class CognitoAccessTokenIdentityProvider implements LambdaIdentityProvider {

    @Override
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
