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
@JsonDeserialize(builder = AutoValue_ViewConfigSpecification.Builder.class)
abstract class ViewConfigSpecification {

  @Nullable
  abstract SelectorSpecification getSelectorSpecification();

  @Nullable
  abstract ViewSpecification getViewSpecification();

  @AutoValue.Builder
  interface Builder {
    @JsonProperty("selector")
    Builder selectorSpecification(@Nullable SelectorSpecification selectorSpecification);

    @JsonProperty("view")
    Builder viewSpecification(@Nullable ViewSpecification viewSpecification);

    ViewConfigSpecification build();
  }
}
