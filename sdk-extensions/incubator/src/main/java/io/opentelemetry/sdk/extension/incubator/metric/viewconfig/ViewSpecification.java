/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.metric.viewconfig;

import com.google.auto.value.AutoValue;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

@AutoValue
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

  @Nullable
  abstract Map<String, Object> getAggregationArgs();

  @Nullable
  abstract List<String> getAttributeKeys();

  @AutoValue.Builder
  interface Builder {
    Builder name(@Nullable String name);

    Builder description(@Nullable String description);

    Builder aggregation(@Nullable String aggregation);

    Builder aggregationArgs(@Nullable Map<String, Object> aggregationArgs);

    Builder attributeKeys(@Nullable List<String> attributeKeys);

    ViewSpecification build();
  }
}
