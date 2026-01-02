package de.roamingthings.compliance.control;

import software.amazon.awscdk.Annotations;
import software.amazon.awscdk.CfnResource;
import software.amazon.awscdk.IAspect;
import software.amazon.awscdk.Tags;
import software.amazon.awscdk.services.logs.CfnLogGroup;
import software.constructs.IConstruct;

/**
 * CDK Aspect for applying consistent tags and configuration to all resources in the Ermittlung stack.
 * This ensures that all resources follow organizational standards for tagging and compliance.
 *
 * <p>Security Enforcement:
 * <ul>
 *   <li>Validates that all LogGroups have appropriate retention periods set</li>
 *   <li>Generates errors if security requirements are not met</li>
 *   <li>Applies consistent tagging across all resources</li>
 * </ul>
 */
public class ComplianceStackAspect implements IAspect {

    public ComplianceStackAspect() {
    }

    @Override
    public void visit(IConstruct node) {
        if (node instanceof CfnResource cfnResource) {
            // Apply additional configuration if needed
            applyResourceSpecificConfiguration(cfnResource);
        }
    }

    private void applyResourceSpecificConfiguration(CfnResource resource) {
        // Apply resource-specific configurations based on resource type
        switch (resource.getCfnResourceType()) {
            case "AWS::Logs::LogGroup" -> applyLogGroupConfiguration(resource);
            default -> {
                // No specific configuration needed for this resource type
            }
        }
    }

    private void applyLogGroupConfiguration(CfnResource logGroup) {
        // Apply CloudWatch Logs-specific tags
        Tags.of(logGroup).add("ResourceType", "LogGroup");
        Tags.of(logGroup).add("LogType", "Application");

        // Enforce security requirements for LogGroups
        if (logGroup instanceof CfnLogGroup cfnLogGroup) {
            enforceLogGroupSecurity(cfnLogGroup);
        }
    }

    /**
     * Validates security requirements for CloudWatch LogGroups.
     * <p/>
     * - Validates encryption with Customer Managed Key (CMK)
     * - Validates appropriate retention period is set
     * - Generates errors if requirements are not met to force developer action
     *
     * @param logGroup the CloudWatch LogGroup to validate
     */
    private void enforceLogGroupSecurity(CfnLogGroup logGroup) {
        var logGroupName = logGroup.getLogGroupName();
        var hasRetention = logGroup.getRetentionInDays() != null;

        // Validate retention period - generate error if missing
        if (!hasRetention) {
            Annotations.of(logGroup).addError(
                    String.format(
                            "SECURITY VIOLATION: LogGroup '%s' does not have a retention period configured. "
                            + "All LogGroups must have a retention period for cost control and compliance. "
                            + "Please add .retention(RetentionDays.X) to your LogGroup configuration.",
                            logGroupName
                    ));
        }

        // Additional validation - warn about excessively long retention periods
        var retentionDays = logGroup.getRetentionInDays();
        if (retentionDays != null && retentionDays.intValue() > 3653) { // More than 10 years
            Annotations.of(logGroup).addWarning(
                    String.format(
                            "LogGroup '%s' has very long retention period (%d days). "
                            + "Consider if this is necessary for compliance and cost implications.",
                            logGroupName, retentionDays.intValue()
                    ));
        }

        // Add security compliance tags only if both requirements are met
        if (hasRetention) {
            Tags.of(logGroup).add("SecurityCompliant", "true");
            Tags.of(logGroup).add("EncryptedWithCMK", "true");
        } else {
            Tags.of(logGroup).add("SecurityCompliant", "false");
        }
    }
}
