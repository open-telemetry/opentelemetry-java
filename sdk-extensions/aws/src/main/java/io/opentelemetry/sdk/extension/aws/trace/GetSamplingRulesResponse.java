/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.aws.trace;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.auto.value.AutoValue;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

@AutoValue
@JsonDeserialize(builder = GetSamplingRulesResponse.Builder.class)
abstract class GetSamplingRulesResponse {

  @JsonProperty("NextToken")
  @Nullable
  abstract String getNextToken();

  @JsonProperty("SamplingRuleRecords")
  abstract List<SamplingRuleRecord> getSamplingRules();

  @AutoValue.Builder
  abstract static class Builder {
    @JsonCreator
    static Builder builder() {
      return new AutoValue_GetSamplingRulesResponse.Builder();
    }

    @JsonProperty("NextToken")
    abstract Builder setNextToken(@Nullable String nextToken);

    @JsonProperty("SamplingRuleRecords")
    abstract Builder setSamplingRules(List<SamplingRuleRecord> samplingRules);

    abstract GetSamplingRulesResponse build();
  }

  @AutoValue
  @JsonDeserialize(builder = SamplingRuleRecord.Builder.class)
  abstract static class SamplingRuleRecord {

    @JsonProperty("CreatedAt")
    abstract String getCreatedAt();

    @JsonProperty("ModifiedAt")
    abstract String getModifiedAt();

    @JsonProperty("SamplingRule")
    abstract SamplingRule getRule();

    @AutoValue.Builder
    abstract static class Builder {

      @JsonCreator
      static Builder builder() {
        return new AutoValue_GetSamplingRulesResponse_SamplingRuleRecord.Builder();
      }

      @JsonProperty("CreatedAt")
      abstract Builder setCreatedAt(String createdAt);

      @JsonProperty("ModifiedAt")
      abstract Builder setModifiedAt(String modifiedAt);

      @JsonProperty("SamplingRule")
      abstract Builder setRule(SamplingRule rule);

      abstract SamplingRuleRecord build();
    }
  }

  @AutoValue
  @JsonDeserialize(builder = SamplingRule.Builder.class)
  abstract static class SamplingRule {
    @JsonProperty("Attributes")
    abstract Map<String, String> getAttributes();

    @JsonProperty("FixedRate")
    abstract double getFixedRate();

    @JsonProperty("Host")
    abstract String getHost();

    @JsonProperty("HTTPMethod")
    abstract String getHttpMethod();

    @JsonProperty("Priority")
    abstract int getPriority();

    @JsonProperty("ReservoirSize")
    abstract int getReservoirSize();

    @JsonProperty("ResourceARN")
    abstract String getResourceArn();

    @JsonProperty("RuleARN")
    @Nullable
    abstract String getRuleArn();

    @JsonProperty("RuleName")
    @Nullable
    abstract String getRuleName();

    @JsonProperty("ServiceName")
    abstract String getServiceName();

    @JsonProperty("ServiceType")
    abstract String getServiceType();

    @JsonProperty("URLPath")
    abstract String getUrlPath();

    @JsonProperty("Version")
    abstract int getVersion();

    @AutoValue.Builder
    abstract static class Builder {

      @JsonCreator
      static Builder builder() {
        return new AutoValue_GetSamplingRulesResponse_SamplingRule.Builder();
      }

      @JsonProperty("Attributes")
      abstract Builder setAttributes(Map<String, String> attributes);

      @JsonProperty("FixedRate")
      abstract Builder setFixedRate(double fixedRate);

      @JsonProperty("Host")
      abstract Builder setHost(String host);

      @JsonProperty("HTTPMethod")
      abstract Builder setHttpMethod(String httpMethod);

      @JsonProperty("Priority")
      abstract Builder setPriority(int priority);

      @JsonProperty("ReservoirSize")
      abstract Builder setReservoirSize(int reservoirSize);

      @JsonProperty("ResourceARN")
      abstract Builder setResourceArn(String resourceArn);

      @JsonProperty("RuleARN")
      @Nullable
      abstract Builder setRuleArn(@Nullable String ruleArn);

      @JsonProperty("RuleName")
      @Nullable
      abstract Builder setRuleName(@Nullable String ruleName);

      @JsonProperty("ServiceName")
      abstract Builder setServiceName(String serviceName);

      @JsonProperty("ServiceType")
      abstract Builder setServiceType(String serviceType);

      @JsonProperty("URLPath")
      abstract Builder setUrlPath(String urlPath);

      @JsonProperty("Version")
      abstract Builder setVersion(int version);

      abstract SamplingRule build();
    }
  }
}
