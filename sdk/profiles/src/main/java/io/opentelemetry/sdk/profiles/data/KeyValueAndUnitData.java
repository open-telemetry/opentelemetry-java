/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.profiles.data;

import io.opentelemetry.api.common.Value;

/**
 * Describes a KeyValue pair with optional unit description for the value. Similar to
 * io.opentelemetry.api.common.KeyValue, but dictionary-centric.
 *
 * @see "common.proto::KeyValue"
 * @see "profiles.proto::KeyValueAndUnit"
 */
public interface KeyValueAndUnitData {

  /** Returns a {@link KeyValueAndUnitData} for the given parameters. */
  @SuppressWarnings("AutoValueSubclassLeaked")
  static ImmutableKeyValueAndUnitData create(
      int keyStringIndex, Value<?> value, int unitStringIndex) {
    return new AutoValue_ImmutableKeyValueAndUnitData(keyStringIndex, value, unitStringIndex);
  }

  /** Index into string table. */
  int getKeyStringIndex();

  Value<?> getValue();

  /** Index into string table. 0 indicates implicit (via semconv) or undefined. */
  int getUnitStringIndex();
}
