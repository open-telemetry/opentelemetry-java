/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import com.google.auto.value.AutoValue;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.AttributeLimits;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.LogRecordLimits;
import javax.annotation.Nullable;

@AutoValue
abstract class LogRecordLimitsAndAttributeLimits {

  static LogRecordLimitsAndAttributeLimits create(
      @Nullable AttributeLimits attributeLimits, @Nullable LogRecordLimits spanLimits) {
    return new AutoValue_LogRecordLimitsAndAttributeLimits(attributeLimits, spanLimits);
  }

  @Nullable
  abstract AttributeLimits getAttributeLimits();

  @Nullable
  abstract LogRecordLimits getLogRecordLimits();
}
