/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal.data;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.common.Value;
import io.opentelemetry.exporter.otlp.profiles.KeyValueAndUnitData;
import javax.annotation.concurrent.Immutable;

/**
 * Auto value implementation of {@link KeyValueAndUnitData}, which describes a Key Value pair with
 * optional unit for the value.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
@Immutable
@AutoValue
public abstract class ImmutableKeyValueAndUnitData implements KeyValueAndUnitData {

  /** Returns a {@link KeyValueAndUnitData} for the given parameters. */
  public static ImmutableKeyValueAndUnitData create(
      int keyStringIndex, Value<?> value, int unitStringIndex) {
    return new AutoValue_ImmutableKeyValueAndUnitData(keyStringIndex, value, unitStringIndex);
  }

  ImmutableKeyValueAndUnitData() {}
}
