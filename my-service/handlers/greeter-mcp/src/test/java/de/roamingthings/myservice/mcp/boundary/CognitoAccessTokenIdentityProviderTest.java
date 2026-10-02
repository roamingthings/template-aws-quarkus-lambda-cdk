package de.roamingthings.myservice.mcp.boundary;

import de.roamingthings.myservice.mcp.Requirement;
import io.quarkus.amazon.lambda.http.model.ApiGatewayAuthorizerContext;
import io.quarkus.amazon.lambda.http.model.AwsProxyRequest;
import io.quarkus.amazon.lambda.http.model.AwsProxyRequestContext;
import io.quarkus.amazon.lambda.http.model.CognitoAuthorizerClaims;
import io.quarkus.security.identity.SecurityIdentity;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Optional;
import java.util.stream.Stream;

import static de.roamingthings.myservice.mcp.Requirement.Rn.R3_1;
import static de.roamingthings.myservice.mcp.Requirement.Rn.R3_2;
import static org.assertj.core.api.Assertions.assertThat;

class CognitoAccessTokenIdentityProviderTest {

    static Stream<Arguments> identifyCaller() {
        return Stream.of(
                Arguments.of(R3_1, requestWithSubject("duke"), Optional.of("duke")),
                Arguments.of(R3_2, requestWithoutAuthorizer(), Optional.empty()));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource
    @Requirement({R3_1, R3_2})
    void identifyCaller(Requirement.Rn statement, AwsProxyRequest request, Optional<String> expectedCaller) {
        var identity = new CognitoAccessTokenIdentityProvider().authenticate(request);

        var caller = Optional.ofNullable(identity).map(CognitoAccessTokenIdentityProviderTest::callerName);
        assertThat(caller).as(statement + " — " + statement.statement()).isEqualTo(expectedCaller);
    }

    static String callerName(SecurityIdentity identity) {
        return identity.getPrincipal().getName();
    }

    static AwsProxyRequest requestWithSubject(String subject) {
        var claims = new CognitoAuthorizerClaims();
        claims.setSubject(subject);
        var authorizer = new ApiGatewayAuthorizerContext();
        authorizer.setClaims(claims);
        var request = requestWithoutAuthorizer();
        request.getRequestContext().setAuthorizer(authorizer);
        return request;
    }

    static AwsProxyRequest requestWithoutAuthorizer() {
        var request = new AwsProxyRequest();
        request.setRequestContext(new AwsProxyRequestContext());
        return request;
    }
}
