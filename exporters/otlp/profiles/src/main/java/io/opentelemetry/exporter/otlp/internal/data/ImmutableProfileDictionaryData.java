/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal.data;

import com.google.auto.value.AutoValue;
import io.opentelemetry.exporter.internal.otlp.AttributeKeyValue;
import io.opentelemetry.exporter.otlp.profiles.AttributeUnitData;
import io.opentelemetry.exporter.otlp.profiles.FunctionData;
import io.opentelemetry.exporter.otlp.profiles.LinkData;
import io.opentelemetry.exporter.otlp.profiles.LocationData;
import io.opentelemetry.exporter.otlp.profiles.MappingData;
import io.opentelemetry.exporter.otlp.profiles.ProfileDictionaryData;
import java.util.List;
import javax.annotation.concurrent.Immutable;

/**
 * Auto value implementation of {@link ProfileDictionaryData}, which represents profiles data shared
 * across the entire message being sent.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
@Immutable
@AutoValue
public abstract class ImmutableProfileDictionaryData implements ProfileDictionaryData {

  /**
   * Returns a new ProfileData representing the given data.
   *
   * @return a new ProfileData representing the given data.
   */
  @SuppressWarnings("TooManyParameters")
  public static ProfileDictionaryData create(
      List<MappingData> mappingTable,
      List<LocationData> locationTable,
      List<FunctionData> functionTable,
      List<AttributeKeyValue<?>> attributeTable,
      List<AttributeUnitData> attributeUnits,
      List<LinkData> linkTable,
      List<String> stringTable) {
    return new AutoValue_ImmutableProfileDictionaryData(
        mappingTable,
        locationTable,
        functionTable,
        attributeTable,
        attributeUnits,
        linkTable,
        stringTable);
  }

  ImmutableProfileDictionaryData() {}
}
