/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.viewconfig;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.auto.value.AutoValue;
import javax.annotation.Nullable;

@AutoValue
@JsonDeserialize(builder = AutoValue_ViewSpecification.Builder.class)
abstract class ViewSpecification {

  static AutoValue_ViewSpecification.Builder builder() {
    return new AutoValue_ViewSpecification.Builder();
  }

  @Nullable
  abstract String getName();

  @Nullable
  abstract String getDescription();

  @Nullable
  abstract String getAggregation();

  @AutoValue.Builder
  interface Builder {
    @JsonProperty("name")
    Builder name(@Nullable String name);

    @JsonProperty("description")
    Builder description(@Nullable String description);

    @JsonProperty("aggregation")
    Builder aggregation(@Nullable String aggregation);

    ViewSpecification build();
  }
}
