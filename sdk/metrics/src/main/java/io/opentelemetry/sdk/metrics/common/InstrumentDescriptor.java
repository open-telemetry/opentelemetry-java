/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.common;

import com.google.auto.value.AutoValue;
import com.google.auto.value.extension.memoized.Memoized;
import javax.annotation.concurrent.Immutable;

@AutoValue
@Immutable
public abstract class InstrumentDescriptor {
  public static InstrumentDescriptor create(
      String name,
      String description,
      String unit,
      InstrumentType type,
      InstrumentValueType valueType) {
    return new AutoValue_InstrumentDescriptor(name, description, unit, type, valueType);
  }

  public abstract String getName();

  public abstract String getDescription();

  public abstract String getUnit();

  public abstract InstrumentType getType();

  public abstract InstrumentValueType getValueType();

  @Memoized
  @Override
  public abstract int hashCode();
}
