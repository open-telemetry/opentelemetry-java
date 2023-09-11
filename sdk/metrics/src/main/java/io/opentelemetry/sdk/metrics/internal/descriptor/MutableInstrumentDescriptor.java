/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.descriptor;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.InstrumentValueType;
import java.util.List;
import java.util.Locale;

/**
 * Describes an instrument that was registered to record data. This is the mutable form of the
 * MetricDescriptor.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class MutableInstrumentDescriptor {

  private int hashcode;
  private static final String DEFAULT_UNIT = "";

  private final String name;
  private final InstrumentValueType valueType;
  private final Advice.AdviceBuilder adviceBuilder;
  private String description;
  private String unit;
  private InstrumentType type;

  public static MutableInstrumentDescriptor create(
      String name, InstrumentType type, InstrumentValueType valueType) {
    return new MutableInstrumentDescriptor(
        name, type, valueType, "", DEFAULT_UNIT, Advice.builder());
  }

  public static MutableInstrumentDescriptor create(
      String name,
      InstrumentType type,
      InstrumentValueType valueType,
      String description,
      String unit,
      Advice.AdviceBuilder adviceBuilder) {
    return new MutableInstrumentDescriptor(name, type, valueType, description, unit, adviceBuilder);
  }

  private MutableInstrumentDescriptor(
      String name,
      InstrumentType type,
      InstrumentValueType valueType,
      String description,
      String unit,
      Advice.AdviceBuilder adviceBuilder) {
    this.name = name;
    this.description = description;
    this.unit = unit;
    this.type = type;
    this.valueType = valueType;
    this.adviceBuilder = adviceBuilder;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getUnit() {
    return unit;
  }

  public void setUnit(String unit) {
    this.unit = unit;
  }

  public InstrumentType getType() {
    return type;
  }

  public void setType(InstrumentType type) {
    this.type = type;
  }

  public InstrumentValueType getValueType() {
    return valueType;
  }

  /**
   * Not part of instrument identity. Ignored from {@link #hashCode()} and {@link #equals(Object)}.
   */
  public Advice.AdviceBuilder getAdviceBuilder() {
    return adviceBuilder;
  }

  public void setAdviceAttributes(List<AttributeKey<?>> attributes) {
    getAdviceBuilder().setAttributes(attributes);
  }

  public void setExplicitBucketBoundaries(List<Double> bucketBoundaries) {
    getAdviceBuilder().setExplicitBucketBoundaries(bucketBoundaries);
  }

  public InstrumentDescriptor toImmutable() {
    return InstrumentDescriptor.create(
        getName(),
        getDescription(),
        getUnit(),
        getType(),
        getValueType(),
        getAdviceBuilder().build());
  }

  /**
   * Uses case-insensitive version of {@link #getName()}, ignores {@link #adviceBuilder} (not part
   * of instrument identity}.
   */
  @Override
  public final int hashCode() {
    int result = hashcode;
    if (result == 0) {
      result = 1;
      result *= 1000003;
      result ^= getName().toLowerCase(Locale.ROOT).hashCode();
      result *= 1000003;
      result ^= getDescription().hashCode();
      result *= 1000003;
      result ^= getUnit().hashCode();
      result *= 1000003;
      result ^= getType().hashCode();
      result *= 1000003;
      result ^= getValueType().hashCode();
      hashcode = result;
    }
    return result;
  }

  /**
   * Uses case-insensitive version of {@link #getName()}, ignores {@link #adviceBuilder} (not part
   * of instrument identity}.
   */
  @Override
  public final boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof MutableInstrumentDescriptor) {
      MutableInstrumentDescriptor that = (MutableInstrumentDescriptor) o;
      return this.getName().equalsIgnoreCase(that.getName())
          && this.getDescription().equals(that.getDescription())
          && this.getUnit().equals(that.getUnit())
          && this.getType().equals(that.getType())
          && this.getValueType().equals(that.getValueType());
    }
    return false;
  }
}
