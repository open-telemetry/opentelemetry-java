/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.profiles.data;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.common.Value;
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
abstract class ImmutableKeyValueAndUnitData implements KeyValueAndUnitData {

  static ImmutableKeyValueAndUnitData create(
      int keyStringIndex, Value<?> value, int unitStringIndex) {
    return new AutoValue_ImmutableKeyValueAndUnitData(keyStringIndex, value, unitStringIndex);
  }

  ImmutableKeyValueAndUnitData() {}
}
