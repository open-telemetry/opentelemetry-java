/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.descriptor;

import com.google.auto.value.AutoValue;
import com.google.auto.value.extension.memoized.Memoized;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import io.opentelemetry.sdk.metrics.internal.debug.SourceInfo;
import javax.annotation.concurrent.Immutable;

/**
 * Describes an instrument that was registered to record data.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
@AutoValue
@Immutable
public abstract class InstrumentDescriptor {

  private final SourceInfo sourceInfo = SourceInfo.fromCurrentStack();

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

  /**
   * Debugging information for this instrument. Ignored from {@link #equals(Object)} and {@link
   * #toString()}
   */
  public final SourceInfo getSourceInfo() {
    return sourceInfo;
  }

  @Memoized
  @Override
  public abstract int hashCode();
}
