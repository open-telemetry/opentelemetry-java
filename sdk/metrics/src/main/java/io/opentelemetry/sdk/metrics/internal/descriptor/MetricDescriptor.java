/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.descriptor;

import com.google.auto.value.AutoValue;
import com.google.auto.value.extension.memoized.Memoized;
import javax.annotation.concurrent.Immutable;

/**
 * Describes a metric that will be output.
 *
 * <p>Provides equality/identity semantics for detecting duplicate metrics of incompatible.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
@AutoValue
@Immutable
public abstract class MetricDescriptor {

  public static MetricDescriptor create(String name, String description, String unit) {
    return new AutoValue_MetricDescriptor(name, description, unit);
  }

  public abstract String getName();

  public abstract String getDescription();

  public abstract String getUnit();

  @Memoized
  @Override
  public abstract int hashCode();

  public boolean isCompatibleWith(MetricDescriptor other) {
    // TODO: implement.
    return equals(other);
  }
}
