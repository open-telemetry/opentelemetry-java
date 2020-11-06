/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import com.google.auto.value.AutoValue;
import javax.annotation.concurrent.Immutable;

@AutoValue
@Immutable
abstract class LongAccumulation implements Accumulation {
  static LongAccumulation create(long value) {
    return new AutoValue_LongAccumulation(value);
  }

  abstract long value();
}
