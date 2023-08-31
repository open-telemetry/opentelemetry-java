/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import com.google.auto.value.AutoValue;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.AttributeLimits;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.TracerProvider;
import javax.annotation.Nullable;

@AutoValue
abstract class TracerProviderAndAttributeLimits {

  static TracerProviderAndAttributeLimits create(
      @Nullable AttributeLimits attributeLimits, @Nullable TracerProvider tracerProvider) {
    return new AutoValue_TracerProviderAndAttributeLimits(attributeLimits, tracerProvider);
  }

  @Nullable
  abstract AttributeLimits getAttributeLimits();

  @Nullable
  abstract TracerProvider getTracerProvider();
}
