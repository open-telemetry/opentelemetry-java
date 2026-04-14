/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.profiles.internal.data;

import com.google.auto.value.AutoValue;
import io.opentelemetry.sdk.profiles.FunctionData;
import io.opentelemetry.sdk.profiles.KeyValueAndUnitData;
import io.opentelemetry.sdk.profiles.LinkData;
import io.opentelemetry.sdk.profiles.LocationData;
import io.opentelemetry.sdk.profiles.MappingData;
import io.opentelemetry.sdk.profiles.ProfilesDictionaryData;
import io.opentelemetry.sdk.profiles.StackData;
import java.util.List;
import javax.annotation.concurrent.Immutable;

/**
 * Auto value implementation of {@link ProfilesDictionaryData}, which represents profiles data
 * shared across the entire message being sent.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
@Immutable
@AutoValue
public abstract class ImmutableProfilesDictionaryData implements ProfilesDictionaryData {

  /**
   * Returns a new ProfileData representing the given data.
   *
   * @return a new ProfileData representing the given data.
   */
  @SuppressWarnings("TooManyParameters")
  public static ProfilesDictionaryData create(
      List<MappingData> mappingTable,
      List<LocationData> locationTable,
      List<FunctionData> functionTable,
      List<LinkData> linkTable,
      List<String> stringTable,
      List<KeyValueAndUnitData> attributeTable,
      List<StackData> stackTable) {
    return new AutoValue_ImmutableProfilesDictionaryData(
        mappingTable,
        locationTable,
        functionTable,
        linkTable,
        stringTable,
        attributeTable,
        stackTable);
  }

  ImmutableProfilesDictionaryData() {}
}
