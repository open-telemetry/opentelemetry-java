/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import com.google.auto.value.AutoValue;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.resources.Resource;
import javax.annotation.concurrent.Immutable;

@AutoValue
@Immutable
abstract class MeterProviderSharedState {
  static MeterProviderSharedState create(
      Clock clock, Resource resource, ViewRegistry viewRegistry) {
    return new AutoValue_MeterProviderSharedState(clock, resource, viewRegistry, clock.now());
  }

  abstract Clock getClock();

  abstract Resource getResource();

  abstract ViewRegistry getViewRegistry();

  abstract long getStartEpochNanos();
}
