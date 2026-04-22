/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.profiles.data;

import javax.annotation.concurrent.Immutable;

/**
 * ValueType describes the type and units of a value.
 *
 * @see "profiles.proto::ValueType"
 */
@Immutable
public interface ValueTypeData {

  /**
   * Returns a new ValueTypeData describing the given type and unit characteristics.
   *
   * @return a new ValueTypeData describing the given type and unit characteristics.
   */
  @SuppressWarnings("AutoValueSubclassLeaked")
  static ValueTypeData create(int typeStringIndex, int unitStringIndex) {
    return new AutoValue_ImmutableValueTypeData(typeStringIndex, unitStringIndex);
  }

  /** Index into string table. */
  int getTypeStringIndex();

  /** Index into string table. */
  int getUnitStringIndex();
}
