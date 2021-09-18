/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import com.google.auto.value.AutoValue;
import io.opentelemetry.sdk.metrics.data.Exemplar;
import java.util.Collections;
import java.util.List;
import javax.annotation.concurrent.Immutable;

/** An accumulation representing {@code long} values and exemplars. */
@Immutable
@AutoValue
abstract class DoubleAccumulation {

  static DoubleAccumulation create(double value, List<Exemplar> exemplars) {
    return new AutoValue_DoubleAccumulation(value, exemplars);
  }

  static DoubleAccumulation create(double value) {
    return create(value, Collections.emptyList());
  }

  DoubleAccumulation() {}

  /** The current value. */
  abstract double getValue();

  /** Sampled measurements recorded during this accumulation. */
  abstract List<Exemplar> getExemplars();
}
