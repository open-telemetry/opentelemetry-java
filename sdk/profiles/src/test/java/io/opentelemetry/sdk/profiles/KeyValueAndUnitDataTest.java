/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.profiles;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Value;
import io.opentelemetry.sdk.profiles.data.KeyValueAndUnitData;
import org.junit.jupiter.api.Test;

// Lives outside the data package on purpose: create() must be usable from other packages.
class KeyValueAndUnitDataTest {

  @Test
  void createResultIsUsableOutsideDataPackage() {
    assertThat(KeyValueAndUnitData.create(1, Value.of("v"), 2).getKeyStringIndex()).isEqualTo(1);
    assertThat(KeyValueAndUnitData.create(1, Value.of("v"), 2).getValue()).isEqualTo(Value.of("v"));
    assertThat(KeyValueAndUnitData.create(1, Value.of("v"), 2).getUnitStringIndex()).isEqualTo(2);
  }
}
