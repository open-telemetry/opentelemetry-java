/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import com.google.auto.value.AutoValue;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.AttributeLimitsModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.TracerProviderModel;
import javax.annotation.Nullable;

@AutoValue
abstract class TracerProviderAndAttributeLimits {

  static TracerProviderAndAttributeLimits create(
      @Nullable AttributeLimitsModel attributeLimits,
      @Nullable TracerProviderModel tracerProvider) {
    return new AutoValue_TracerProviderAndAttributeLimits(attributeLimits, tracerProvider);
  }

  @Nullable
  abstract AttributeLimitsModel getAttributeLimits();

  @Nullable
  abstract TracerProviderModel getTracerProvider();
}
