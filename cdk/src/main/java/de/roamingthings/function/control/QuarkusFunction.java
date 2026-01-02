package de.roamingthings.function.control;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.services.ec2.IVpc;
import software.amazon.awscdk.services.ec2.SecurityGroup;
import software.amazon.awscdk.services.iam.IGrantable;
import software.amazon.awscdk.services.iam.ManagedPolicy;
import software.amazon.awscdk.services.kms.IKey;
import software.amazon.awscdk.services.lambda.AdotInstrumentationConfig;
import software.amazon.awscdk.services.lambda.AdotLambdaExecWrapper;
import software.amazon.awscdk.services.lambda.AdotLambdaLayerJavaAutoInstrumentationVersion;
import software.amazon.awscdk.services.lambda.AdotLayerVersion;
import software.amazon.awscdk.services.lambda.Alias;
import software.amazon.awscdk.services.lambda.ApplicationLogLevel;
import software.amazon.awscdk.services.lambda.Architecture;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.ILayerVersion;
import software.amazon.awscdk.services.lambda.LambdaInsightsVersion;
import software.amazon.awscdk.services.lambda.LoggingFormat;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.lambda.SystemLogLevel;
import software.amazon.awscdk.services.lambda.Tracing;
import software.amazon.awscdk.services.logs.LogGroup;
import software.amazon.awscdk.services.logs.RetentionDays;
import software.amazon.awscdk.services.s3.assets.AssetOptions;
import software.constructs.Construct;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import static java.lang.Boolean.TRUE;
import static software.amazon.awscdk.services.lambda.SnapStartConf.ON_PUBLISHED_VERSIONS;

public class QuarkusFunction extends Construct {

    // At 1,769 MB, a function has the equivalent of one vCPU (https://docs.aws.amazon.com/lambda/latest/dg/configuration-memory.html)
    private static final int DEFAULT_MEMORY_SIZE = 1769;
    private static final Runtime DEFAULT_RUNTIME = Runtime.JAVA_25;
    private static final String QUARKUS_LAMBDA_BUILD_ARTIFACT_NAME = "/function.zip";
    private static final String GRADE_BUILD_DIR = "/build";
    private static final String MAVEN_BUILD_DIR = "/target";
    private static final Architecture DEFAULT_ARCHITECTURE = Architecture.ARM_64;
    private static final String QUARKUS_STREAM_HANDLER = "io.quarkus.amazon.lambda.runtime.QuarkusStreamHandler::handleRequest";
    private static final String DEFAULT_LIVE_ALIAS_NAME = "live";
    private static final String DEFAULT_JAVA_OPTS = "--add-opens=java.base/java.lang=ALL-UNNAMED -XX:+TieredCompilation -XX:TieredStopAtLevel=1 -Djdk.httpclient.keepalive.timeout=10";

    private final Function function;
    private final Alias liveAlias;
    private final LogGroup logGroup;

    public QuarkusFunction(Construct scope, String id, QuarkusLambdaFunctionProps props) {
        super(scope, id);
        var runtime = Objects.requireNonNullElse(props.runtime, DEFAULT_RUNTIME);
        var architecture = Objects.requireNonNullElse(props.architecture, DEFAULT_ARCHITECTURE);
        var memorySize = Objects.requireNonNullElse(props.memorySize, DEFAULT_MEMORY_SIZE);
        var tracing = Objects.requireNonNullElse(props.tracing, Tracing.ACTIVE);
        var aliasName = Objects.requireNonNullElse(props.liveAliasName, DEFAULT_LIVE_ALIAS_NAME);
        var logRetention = Objects.requireNonNullElse(props.logRetention, RetentionDays.TWO_WEEKS);

        logGroup = createEncryptedLogGroup(props.functionName, props.encryptionKey, logRetention);

        function = Function.Builder.create(this, "Function")
                .functionName(props.functionName)
                .description(props.description)
                .runtime(runtime)
                .architecture(architecture)
                .memorySize(memorySize)
                .timeout(props.timeout)
                .handler(QUARKUS_STREAM_HANDLER)
                .code(
                        Code.fromAsset(
                                quarkusAssetPath(props.modulePath, props.buildTool),
                                AssetOptions.builder().deployTime(true).build()
                        )
                )
                .layers(props.layers)
                .insightsVersion(TRUE.equals(props.addLambdaInsightsLayer) ? LambdaInsightsVersion.VERSION_1_0_498_0 : null)
                .tracing(tracing)
                .snapStart((TRUE.equals(props.snapStartEnabled)) ? ON_PUBLISHED_VERSIONS : null)
                .loggingFormat(LoggingFormat.JSON)
                .logGroup(logGroup)
                .vpc(props.vpc)
                .securityGroups(props.securityGroups)
                .applicationLogLevelV2(props.applicationLogLevel)
                .systemLogLevelV2(props.systemLogLevel)
                .adotInstrumentation(createAdotInstrumentation(props.addAdotInstrumentation))
                .build();
        createEnvironment(props);

        liveAlias = function.addAlias(aliasName);

        function.getRole().addManagedPolicy(ManagedPolicy.fromAwsManagedPolicyName("AWSXRayDaemonWriteAccess"));
        function.getRole()
                .addManagedPolicy(
                        ManagedPolicy.fromAwsManagedPolicyName("CloudWatchLambdaInsightsExecutionRolePolicy"));
    }

    private static @Nullable AdotInstrumentationConfig createAdotInstrumentation(Boolean addAdotInstrumentation) {
        if (TRUE.equals(addAdotInstrumentation)) {
            return AdotInstrumentationConfig.builder()
                    .layerVersion(AdotLayerVersion.fromJavaAutoInstrumentationLayerVersion(AdotLambdaLayerJavaAutoInstrumentationVersion.LATEST))
                    .execWrapper(AdotLambdaExecWrapper.REGULAR_HANDLER)
                    .build();
        }
        return null;
    }

    private void createEnvironment(QuarkusLambdaFunctionProps props) {
        if (props.environment != null) {
            props.environment.entrySet()
                    .forEach(entry -> function.addEnvironment(entry.getKey(), entry.getValue()));
            if (!props.environment.containsKey("JAVA_OPTS")) {
                function.addEnvironment("JAVA_OPTS", DEFAULT_JAVA_OPTS);
            }
        }
    }

    private LogGroup createEncryptedLogGroup(String functionName,
                                             IKey encryptionKey,
                                             RetentionDays logRetention
    ) {
        return LogGroup.Builder.create(this, "LogGroup")
                .logGroupName("/aws/lambda/" + functionName)
                .retention(logRetention)
                .encryptionKey(encryptionKey)
                .removalPolicy(RemovalPolicy.DESTROY)
                .build();
    }

    private static String quarkusAssetPath(String modulePath, BuildTool buildTool) {
        Objects.requireNonNull(modulePath, "modulePath is required");
        var usedBuildTool = Objects.requireNonNullElse(buildTool, BuildTool.GRADLE);
        var buildDir = usedBuildTool == BuildTool.GRADLE ? GRADE_BUILD_DIR : MAVEN_BUILD_DIR;
        return modulePath + buildDir + QUARKUS_LAMBDA_BUILD_ARTIFACT_NAME;
    }

    public Alias getLiveAlias() {
        return liveAlias;
    }

    public Function getFunction() {
        return function;
    }

    public LogGroup getLogGroup() {
        return logGroup;
    }

    public enum BuildTool {
        GRADLE, MAVEN;
    }

    public record QuarkusLambdaFunctionProps(@NonNull String functionName,
                                             String description,
                                             String liveAliasName,
                                             @NonNull BuildTool buildTool,
                                             @NonNull String modulePath,
                                             @NonNull IKey encryptionKey,
                                             List<ILayerVersion> layers,
                                             Boolean addLambdaInsightsLayer,
                                             Map<String, String> environment,
                                             Architecture architecture,
                                             Integer memorySize,
                                             Duration timeout,
                                             Runtime runtime,
                                             Tracing tracing,
                                             Integer reservedConcurrency,
                                             RetentionDays logRetention,
                                             IVpc vpc,
                                             List<SecurityGroup> securityGroups,
                                             Boolean snapStartEnabled,
                                             ApplicationLogLevel applicationLogLevel,
                                             SystemLogLevel systemLogLevel,
                                             Boolean addInsights,
                                             Boolean addAdotInstrumentation) {

        public QuarkusLambdaFunctionProps {
            Objects.requireNonNull(functionName, "property :functionName is required");
            Objects.requireNonNull(buildTool, "property :buildTool is required");
            Objects.requireNonNull(modulePath, "property :modulePath is required");
        }

        @Override
        public String toString() {
            return getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(this));
        }

        public static Builder builder() {
            return new Builder();
        }

        public static final class Builder {

            private Consumer<IGrantable> permissionConfiguration;

            private String functionName;
            private String description;
            private String liveAliasName;
            private BuildTool buildTool;
            private String modulePath;
            private IKey encryptionKey;
            private List<ILayerVersion> layers;
            private Boolean addLambdaInsightsLayer;
            private Map<String, String> environment;
            private Architecture architecture;
            private Integer memorySize;
            private Duration timeout;
            private Runtime runtime;
            private Tracing tracing;
            private Integer reservedConcurrency;
            private RetentionDays logRetention;
            private Boolean snapStartEnabled;
            private ApplicationLogLevel applicationLogLevel;
            private SystemLogLevel systemLogLevel;
            private Boolean addInsights;
            private IVpc vpc;
            private List<SecurityGroup> securityGroups;
            private Boolean addAdotInstrumentation;

            private Builder() {
            }

            public Builder permissionConfiguration(Consumer<IGrantable> permissionConfiguration) {
                this.permissionConfiguration = permissionConfiguration;
                return this;
            }

            public Builder functionName(@NonNull String functionName) {
                this.functionName = functionName;
                return this;
            }

            public Builder buildTool(
                    QuarkusFunction.BuildTool buildTool) {
                this.buildTool = buildTool;
                return this;
            }

            public Builder modulePath(@NonNull String modulePath) {
                this.modulePath = modulePath;
                return this;
            }

            public Builder description(String description) {
                this.description = description;
                return this;
            }

            public Builder liveAliasName(String liveAliasName) {
                this.liveAliasName = liveAliasName;
                return this;
            }

            public Builder encryptionKey(IKey encryptionKey) {
                this.encryptionKey = encryptionKey;
                return this;
            }

            public Builder layers(List<ILayerVersion> layers) {
                this.layers = layers;
                return this;
            }

            public Builder addLambdaInsightsLayer(Boolean addLambdaInsightsLayer) {
                this.addLambdaInsightsLayer = addLambdaInsightsLayer;
                return this;
            }

            public Builder environment(Map<String, String> environment) {
                this.environment = environment;
                return this;
            }

            public Builder architecture(Architecture architecture) {
                this.architecture = architecture;
                return this;
            }

            public Builder memorySize(Integer memorySize) {
                this.memorySize = memorySize;
                return this;
            }

            public Builder timeout(Duration timeout) {
                this.timeout = timeout;
                return this;
            }

            public Builder runtime(Runtime runtime) {
                this.runtime = runtime;
                return this;
            }

            public Builder tracing(Tracing tracing) {
                this.tracing = tracing;
                return this;
            }

            public Builder reservedConcurrency(Integer reservedConcurrency) {
                this.reservedConcurrency = reservedConcurrency;
                return this;
            }

            public Builder logRetention(RetentionDays logRetention) {
                this.logRetention = logRetention;
                return this;
            }

            public Builder vpc(IVpc vpc) {
                this.vpc = vpc;
                return this;
            }

            public Builder securityGroups(List<SecurityGroup> securityGroups) {
                this.securityGroups = securityGroups;
                return this;
            }

            public Builder snapStartEnabled(Boolean snapStartEnabled) {
                this.snapStartEnabled = snapStartEnabled;
                return this;
            }

            public Builder applicationLogLevel(
                    ApplicationLogLevel applicationLogLevel) {
                this.applicationLogLevel = applicationLogLevel;
                return this;
            }

            public Builder systemLogLevel(
                    SystemLogLevel systemLogLevel) {
                this.systemLogLevel = systemLogLevel;
                return this;
            }

            public Builder addInsights(Boolean addInsights) {
                this.addInsights = addInsights;
                return this;
            }

            public Builder addAdotInstrumentation(Boolean addAdotInstrumentation) {
                this.addAdotInstrumentation = addAdotInstrumentation;
                return this;
            }

            public QuarkusLambdaFunctionProps build() {
                return new QuarkusLambdaFunctionProps(this.functionName, this.description, this.liveAliasName,
                        this.buildTool, this.modulePath, this.encryptionKey, this.layers, this.addLambdaInsightsLayer,
                        this.environment, this.architecture, this.memorySize, this.timeout, this.runtime, this.tracing,
                        this.reservedConcurrency, this.logRetention, this.vpc, this.securityGroups, this.snapStartEnabled,
                        this.applicationLogLevel, this.systemLogLevel, this.addInsights, this.addAdotInstrumentation);
            }
        }
    }
}
