/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregator;

import com.google.auto.value.AutoValue;
import io.opentelemetry.sdk.metrics.data.Exemplar;
import java.util.Collections;
import java.util.List;
import javax.annotation.concurrent.Immutable;

/** An accumulation representing {@code long} values and exemplars. */
@Immutable
@AutoValue
abstract class LongAccumulation {

  static LongAccumulation create(long value, List<Exemplar> exemplars) {
    return new AutoValue_LongAccumulation(value, exemplars);
  }

  static LongAccumulation create(long value) {
    return create(value, Collections.emptyList());
  }

  LongAccumulation() {}

  /** The current value. */
  abstract long getValue();

  /** Sampled measurements recorded during this accumulation. */
  abstract List<Exemplar> getExemplars();
}
