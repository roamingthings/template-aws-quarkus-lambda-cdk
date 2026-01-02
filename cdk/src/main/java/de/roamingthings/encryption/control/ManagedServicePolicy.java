package de.roamingthings.encryption.control;

import software.amazon.awscdk.Arn;
import software.amazon.awscdk.ArnComponents;
import software.amazon.awscdk.ArnFormat;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.services.iam.AnyPrincipal;
import software.amazon.awscdk.services.iam.Effect;
import software.amazon.awscdk.services.iam.PolicyStatement;
import software.amazon.awscdk.services.iam.ServicePrincipal;

import java.util.List;
import java.util.Map;

public interface ManagedServicePolicy {

    enum AwsService {

        DYNAMODB("dynamodb"),
        EC2("ec2"),
        ECR("ecr"),
        KINESIS("kinesis"),
        LAMBDA("lambda"),
        LOGS("logs"),
        SECRETSMANAGER("secretsmanager"),
        SNS("sns"),
        SQS("sqs"),
        SSM("ssm"),
        XRAY("xray"),
        S3("s3"),
        EVENTBRIDGE("events"),
        STEPFUNCTIONS("states");

        public final String serviceName;

        AwsService(String serviceName) {
            this.serviceName = serviceName;
        }
    }

    static PolicyStatement generatePolicyStatement(Stack scope, AwsService service) {
        if (service == AwsService.LOGS) {
            return PolicyStatement.Builder.create()
                    .effect(Effect.ALLOW)
                    .principals(List.of(new ServicePrincipal("logs")))
                    .resources(List.of("*"))
                    .actions(List.of("kms:Encrypt*", "kms:Decrypt*", "kms:ReEncrypt*", "kms:GenerateDataKey*", "kms:Describe*"))
                    .conditions(Map.of(
                            "ArnEquals", Map.of(
                                    "kms:EncryptionContext:aws:logs:arn", Arn.format(
                                            ArnComponents.builder()
                                                    .partition("aws")
                                                    .service("logs")
                                                    .resource("log-group")
                                                    .resourceName("*")
                                                    .arnFormat(ArnFormat.COLON_RESOURCE_NAME)
                                                    .build(),
                                            scope
                                    )
                            )
                    ))
                    .build();
        } else {
            return PolicyStatement.Builder.create()
                    .effect(Effect.ALLOW)
                    .principals(List.of(new AnyPrincipal()))
                    .resources(List.of("*"))
                    .actions(List.of("kms:Encrypt", "kms:Decrypt", "kms:ReEncrypt*", "kms:GenerateDataKey*", "kms:DescribeKey"))
                    .conditions(Map.of(
                            "StringEquals", Map.of(
                                    "kms:ViaService", "%s.%s.amazonaws.com".formatted(service.serviceName, scope.getRegion()),
                                    "kms:CallerAccount", scope.getAccount()
                            )
                    ))
                    .build();
        }
    }
}