/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import com.google.auto.value.AutoValue;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.AttributeLimits;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.SpanLimits;
import javax.annotation.Nullable;

@AutoValue
abstract class SpanLimitsAndAttributeLimits {

  static SpanLimitsAndAttributeLimits create(
      @Nullable AttributeLimits attributeLimits, @Nullable SpanLimits spanLimits) {
    return new AutoValue_SpanLimitsAndAttributeLimits(attributeLimits, spanLimits);
  }

  @Nullable
  abstract AttributeLimits getAttributeLimits();

  @Nullable
  abstract SpanLimits getSpanLimits();
}
