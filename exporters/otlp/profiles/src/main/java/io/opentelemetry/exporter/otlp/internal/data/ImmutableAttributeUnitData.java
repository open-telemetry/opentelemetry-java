/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal.data;

import com.google.auto.value.AutoValue;
import io.opentelemetry.exporter.otlp.profiles.AttributeUnitData;
import javax.annotation.concurrent.Immutable;

@Immutable
@AutoValue
public abstract class ImmutableAttributeUnitData implements AttributeUnitData {

  public static AttributeUnitData create(long attributeKey, long unitIndex) {
    return new AutoValue_ImmutableAttributeUnitData(attributeKey, unitIndex);
  }

  ImmutableAttributeUnitData() {}
}
