/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.aws.trace;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;
import javax.annotation.Nullable;

@AutoValue
@JsonSerialize(as = GetSamplingRulesRequest.class)
abstract class GetSamplingRulesRequest {

  static GetSamplingRulesRequest create(@Nullable String nextToken) {
    return new AutoValue_GetSamplingRulesRequest(nextToken);
  }

  @JsonProperty("NextToken")
  @Nullable
  abstract String getNextToken();
}
