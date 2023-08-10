/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.descriptor;

import com.google.auto.value.AutoValue;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.InstrumentValueType;
import io.opentelemetry.sdk.metrics.internal.debug.SourceInfo;
import java.util.Locale;
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
      InstrumentValueType valueType,
      Advice advice) {
    return new AutoValue_InstrumentDescriptor(name, description, unit, type, valueType, advice);
  }

  InstrumentDescriptor() {}

  public abstract String getName();

  public abstract String getDescription();

  public abstract String getUnit();

  public abstract InstrumentType getType();

  public abstract InstrumentValueType getValueType();

  /**
   * Not part of instrument identity. Ignored from {@link #hashCode()} and {@link #equals(Object)}.
   */
  public abstract Advice getAdvice();

  /**
   * Debugging information for this instrument. Ignored from {@link #equals(Object)} and {@link
   * #toString()}.
   */
  public final SourceInfo getSourceInfo() {
    return sourceInfo;
  }

  /**
   * Uses case-insensitive version of {@link #getName()}, ignores {@link #getAdvice()} (not part of
   * instrument identity}, ignores {@link #getSourceInfo()}.
   */
  @Override
  public final int hashCode() {
    // TODO: memoize
    int h = 1;
    h *= 1000003;
    h ^= getName().toLowerCase(Locale.ROOT).hashCode();
    h *= 1000003;
    h ^= getDescription().hashCode();
    h *= 1000003;
    h ^= getUnit().hashCode();
    h *= 1000003;
    h ^= getType().hashCode();
    h *= 1000003;
    h ^= getValueType().hashCode();
    return h;
  }

  /**
   * Uses case-insensitive version of {@link #getName()}, ignores {@link #getAdvice()} (not part of
   * instrument identity}, ignores {@link #getSourceInfo()}.
   */
  @Override
  public final boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof InstrumentDescriptor) {
      InstrumentDescriptor that = (InstrumentDescriptor) o;
      return this.getName().equalsIgnoreCase(that.getName())
          && this.getDescription().equals(that.getDescription())
          && this.getUnit().equals(that.getUnit())
          && this.getType().equals(that.getType())
          && this.getValueType().equals(that.getValueType());
    }
    return false;
  }
}
