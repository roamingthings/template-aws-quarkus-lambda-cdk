package de.roamingthings.systemtest;

import org.junit.jupiter.api.Test;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class ItemHandlerIT {

    static final System.Logger LOGGER = System.getLogger(ItemHandlerIT.class.getName());

    @Test
    void invokeItemHandler() {
        InvokeResponse response;
        try (var lambdaClient = LambdaClient.builder().build()) {

            var payload = """
                    {
                        "id": "72fee59e-3812-4dd6-be49-6ab638ee5a5e",
                        "name": "Test Item",
                        "description": "Test Description"
                    }
                    """;

            var request = InvokeRequest.builder()
                    .functionName("my-lambda-ItemHandler")
                    .payload(SdkBytes.fromUtf8String(payload))
                    .build();

            response = lambdaClient.invoke(request);
        }

        var responsePayload = response.payload().asString(StandardCharsets.UTF_8);
        LOGGER.log(System.Logger.Level.INFO, "Response: {0}", responsePayload);

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(responsePayload).contains("Test Item")
                .contains("Test Description");
    }
}
