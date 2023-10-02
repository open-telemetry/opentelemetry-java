/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import com.google.auto.value.AutoValue;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.AttributeLimits;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.LoggerProvider;
import javax.annotation.Nullable;

@AutoValue
abstract class LoggerProviderAndAttributeLimits {

  static LoggerProviderAndAttributeLimits create(
      @Nullable AttributeLimits attributeLimits, @Nullable LoggerProvider loggerProvider) {
    return new AutoValue_LoggerProviderAndAttributeLimits(attributeLimits, loggerProvider);
  }

  @Nullable
  abstract AttributeLimits getAttributeLimits();

  @Nullable
  abstract LoggerProvider getLoggerProvider();
}
