/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.aws.trace;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

@AutoValue
abstract class GetSamplingRulesResponse {

  @JsonCreator
  static GetSamplingRulesResponse create(
      @JsonProperty("NextToken") String nextToken,
      @JsonProperty("SamplingRuleRecords") List<SamplingRuleRecord> samplingRules) {
    return new AutoValue_GetSamplingRulesResponse(nextToken, samplingRules);
  }

  @Nullable
  abstract String getNextToken();

  abstract List<SamplingRuleRecord> getSamplingRules();

  @AutoValue
  abstract static class SamplingRuleRecord {

    @JsonCreator
    static SamplingRuleRecord create(
        @JsonProperty("CreatedAt") String createdAt,
        @JsonProperty("ModifiedAt") String modifiedAt,
        @JsonProperty("SamplingRule") SamplingRule rule) {
      return new AutoValue_GetSamplingRulesResponse_SamplingRuleRecord(createdAt, modifiedAt, rule);
    }

    abstract String getCreatedAt();

    abstract String getModifiedAt();

    abstract SamplingRule getRule();
  }

  @AutoValue
  abstract static class SamplingRule {

    @JsonCreator
    static SamplingRule create(
        @JsonProperty("Attributes") Map<String, String> attributes,
        @JsonProperty("FixedRate") double fixedRate,
        @JsonProperty("Host") String host,
        @JsonProperty("HTTPMethod") String httpMethod,
        @JsonProperty("Priority") int priority,
        @JsonProperty("ReservoirSize") int reservoirSize,
        @JsonProperty("ResourceARN") String resourceArn,
        @JsonProperty("RuleARN") @Nullable String ruleArn,
        @JsonProperty("RuleName") @Nullable String ruleName,
        @JsonProperty("ServiceName") String serviceName,
        @JsonProperty("ServiceType") String serviceType,
        @JsonProperty("URLPath") String urlPath,
        @JsonProperty("Version") int version) {
      return new AutoValue_GetSamplingRulesResponse_SamplingRule(
          attributes,
          fixedRate,
          host,
          httpMethod,
          priority,
          reservoirSize,
          resourceArn,
          ruleArn,
          ruleName,
          serviceName,
          serviceType,
          urlPath,
          version);
    }

    abstract Map<String, String> getAttributes();

    abstract double getFixedRate();

    abstract String getHost();

    abstract String getHttpMethod();

    abstract int getPriority();

    abstract int getReservoirSize();

    abstract String getResourceArn();

    @Nullable
    abstract String getRuleArn();

    @Nullable
    abstract String getRuleName();

    abstract String getServiceName();

    abstract String getServiceType();

    abstract String getUrlPath();

    abstract int getVersion();
  }
}
