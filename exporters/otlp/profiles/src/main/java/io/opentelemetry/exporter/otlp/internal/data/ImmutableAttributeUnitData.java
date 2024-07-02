/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal.data;

import com.google.auto.value.AutoValue;
import io.opentelemetry.exporter.otlp.profiles.AttributeUnitData;
import javax.annotation.concurrent.Immutable;

/**
 * Auto value implementation of {@link AttributeUnitData}, which represents a mapping between
 * Attribute Keys and Units.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
@Immutable
@AutoValue
public abstract class ImmutableAttributeUnitData implements AttributeUnitData {

  /**
   * Returns a new AttributeUnitData mapping the given key to the given unit.
   *
   * @return a new AttributeUnitData mapping the given key to the given unit.
   */
  public static AttributeUnitData create(long attributeKey, long unitIndex) {
    return new AutoValue_ImmutableAttributeUnitData(attributeKey, unitIndex);
  }

  ImmutableAttributeUnitData() {}
}
