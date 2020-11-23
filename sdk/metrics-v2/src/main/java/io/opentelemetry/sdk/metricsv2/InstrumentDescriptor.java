/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metricsv2;

import com.google.auto.value.AutoValue;
import com.google.auto.value.extension.memoized.Memoized;
import javax.annotation.concurrent.Immutable;

@AutoValue
@Immutable
abstract class InstrumentDescriptor {
  static InstrumentDescriptor create(
      String name,
      String description,
      String unit,
      InstrumentType type,
      InstrumentValueType valueType) {
    return new AutoValue_InstrumentDescriptor(name, description, unit, type, valueType);
  }

  abstract String getName();

  abstract String getDescription();

  abstract String getUnit();

  abstract InstrumentType getType();

  abstract InstrumentValueType getValueType();

  @Memoized
  @Override
  public abstract int hashCode();
}
