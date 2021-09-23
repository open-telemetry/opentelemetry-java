/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.metrics.internal.export.CollectionHandle;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DeltaAccumulationTest {
  private CollectionHandle handle1;
  private CollectionHandle handle2;
  private Set<CollectionHandle> all;

  @BeforeEach
  void setup() {
    Supplier<CollectionHandle> supplier = CollectionHandle.createSupplier();
    handle1 = supplier.get();
    handle2 = supplier.get();
    all = CollectionHandle.mutableSet();
    all.add(handle1);
    all.add(handle2);
  }

  @Test
  void wasReadBy_works() {
    Map<Attributes, Long> measurement = new HashMap<>();
    measurement.put(Attributes.empty(), 1L);
    DeltaAccumulation<Long> accumlation = new DeltaAccumulation<>(measurement);
    assertThat(accumlation.wasReadBy(handle1)).isFalse();
    assertThat(accumlation.wasReadBy(handle2)).isFalse();
    assertThat(accumlation.wasReadyByAll(all)).isFalse();

    // Read and check.
    assertThat(accumlation.read(handle1)).isEqualTo(measurement);
    assertThat(accumlation.wasReadBy(handle1)).isTrue();
    assertThat(accumlation.wasReadBy(handle2)).isFalse();
    assertThat(accumlation.wasReadyByAll(all)).isFalse();

    // Read and check.
    assertThat(accumlation.read(handle2)).isEqualTo(measurement);
    assertThat(accumlation.wasReadBy(handle1)).isTrue();
    assertThat(accumlation.wasReadBy(handle2)).isTrue();
    assertThat(accumlation.wasReadyByAll(all)).isTrue();
  }
}
