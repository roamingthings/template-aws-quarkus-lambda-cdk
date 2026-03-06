package de.roamingthings.myservice.boundary;

import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.services.cloudfront.Distribution;
import software.amazon.awscdk.services.cloudfront.origins.S3BucketOrigin;
import software.amazon.awscdk.services.s3.Bucket;
import software.amazon.awscdk.services.s3.IBucket;
import software.constructs.Construct;

class OAuthFrontendConstruct extends Construct {

    IBucket bucket;
    Distribution distribution;

    OAuthFrontendConstruct(Construct scope, String id) {
        super(scope, id);

        bucket = Bucket.Builder.create(this, "OAuthMetadataBucket")
                .autoDeleteObjects(true)
                .removalPolicy(RemovalPolicy.DESTROY)
                .build();

        distribution = Distribution.Builder.create(this, "Distribution")
                .defaultBehavior(software.amazon.awscdk.services.cloudfront.BehaviorOptions.builder()
                        .origin(S3BucketOrigin.withOriginAccessControl(bucket))
                        .build())
                .build();
    }

    IBucket bucket() {
        return bucket;
    }

    Distribution distribution() {
        return distribution;
    }
}
