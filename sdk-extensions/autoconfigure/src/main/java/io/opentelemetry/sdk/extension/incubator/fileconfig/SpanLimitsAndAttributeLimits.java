/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import com.google.auto.value.AutoValue;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.AttributeLimitsModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.SpanLimitsModel;
import javax.annotation.Nullable;

@AutoValue
abstract class SpanLimitsAndAttributeLimits {

  static SpanLimitsAndAttributeLimits create(
      @Nullable AttributeLimitsModel attributeLimits, @Nullable SpanLimitsModel spanLimits) {
    return new AutoValue_SpanLimitsAndAttributeLimits(attributeLimits, spanLimits);
  }

  @Nullable
  abstract AttributeLimitsModel getAttributeLimits();

  @Nullable
  abstract SpanLimitsModel getSpanLimits();
}
