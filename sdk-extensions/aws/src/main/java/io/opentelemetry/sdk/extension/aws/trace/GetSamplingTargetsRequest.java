/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.aws.trace;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;
import java.util.Date;
import java.util.List;

@AutoValue
@JsonSerialize(as = GetSamplingTargetsRequest.class)
abstract class GetSamplingTargetsRequest {

  static GetSamplingTargetsRequest create(List<SamplingStatisticsDocument> documents) {
    return new AutoValue_GetSamplingTargetsRequest(documents);
  }

  // Limit of 25 items
  @JsonProperty("SamplingStatisticsDocuments")
  abstract List<SamplingStatisticsDocument> getDocuments();

  @AutoValue
  @JsonSerialize(as = SamplingStatisticsDocument.class)
  abstract static class SamplingStatisticsDocument {

    static SamplingStatisticsDocument.Builder newBuilder() {
      return new AutoValue_GetSamplingTargetsRequest_SamplingStatisticsDocument.Builder();
    }

    @JsonProperty("BorrowCount")
    abstract long getBorrowCount();

    @JsonProperty("ClientID")
    abstract String getClientId();

    @JsonProperty("RequestCount")
    abstract long getRequestCount();

    @JsonProperty("RuleName")
    abstract String getRuleName();

    @JsonProperty("SampledCount")
    abstract long getSampledCount();

    @JsonProperty("Timestamp")
    @JsonFormat(
        shape = JsonFormat.Shape.STRING,
        pattern = "yyyy-MM-dd'T'HH:mm:ss",
        timezone = "UTC")
    abstract Date getTimestamp();

    @AutoValue.Builder
    abstract static class Builder {
      abstract Builder setBorrowCount(long borrowCount);

      abstract Builder setClientId(String clientId);

      abstract Builder setRequestCount(long requestCount);

      abstract Builder setRuleName(String ruleName);

      abstract Builder setSampledCount(long sampledCount);

      abstract Builder setTimestamp(Date timestamp);

      abstract SamplingStatisticsDocument build();
    }
  }
}
