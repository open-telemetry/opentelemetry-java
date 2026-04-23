/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import com.google.auto.value.AutoValue;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.AttributeLimitsModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.LogRecordLimitsModel;
import javax.annotation.Nullable;

@AutoValue
abstract class LogRecordLimitsAndAttributeLimits {

  static LogRecordLimitsAndAttributeLimits create(
      @Nullable AttributeLimitsModel attributeLimits, @Nullable LogRecordLimitsModel spanLimits) {
    return new AutoValue_LogRecordLimitsAndAttributeLimits(attributeLimits, spanLimits);
  }

  @Nullable
  abstract AttributeLimitsModel getAttributeLimits();

  @Nullable
  abstract LogRecordLimitsModel getLogRecordLimits();
}
