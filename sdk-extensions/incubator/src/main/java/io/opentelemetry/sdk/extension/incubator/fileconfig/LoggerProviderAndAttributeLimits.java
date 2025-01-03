/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import com.google.auto.value.AutoValue;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.AttributeLimitsModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.LoggerProviderModel;
import javax.annotation.Nullable;

@AutoValue
abstract class LoggerProviderAndAttributeLimits {

  static LoggerProviderAndAttributeLimits create(
      @Nullable AttributeLimitsModel attributeLimits,
      @Nullable LoggerProviderModel loggerProvider) {
    return new AutoValue_LoggerProviderAndAttributeLimits(attributeLimits, loggerProvider);
  }

  @Nullable
  abstract AttributeLimitsModel getAttributeLimits();

  @Nullable
  abstract LoggerProviderModel getLoggerProvider();
}
