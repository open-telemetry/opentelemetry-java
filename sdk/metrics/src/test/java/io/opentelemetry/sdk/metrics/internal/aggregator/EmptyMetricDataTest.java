/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.sdk.metrics.data.MetricData;
import org.junit.jupiter.api.Test;

class EmptyMetricDataTest {

  @Test
  void isEmpty() {
    assertThat(EmptyMetricData.getInstance().isEmpty()).isTrue();
  }

  @Test
  void unsupportedOperation() {
    MetricData instance = EmptyMetricData.getInstance();
    assertThatThrownBy(instance::getResource).isInstanceOf(UnsupportedOperationException.class);
    assertThatThrownBy(instance::getInstrumentationLibraryInfo)
        .isInstanceOf(UnsupportedOperationException.class);
    assertThatThrownBy(instance::getName).isInstanceOf(UnsupportedOperationException.class);
    assertThatThrownBy(instance::getDescription).isInstanceOf(UnsupportedOperationException.class);
    assertThatThrownBy(instance::getUnit).isInstanceOf(UnsupportedOperationException.class);
    assertThatThrownBy(instance::getType).isInstanceOf(UnsupportedOperationException.class);
    assertThatThrownBy(instance::getData).isInstanceOf(UnsupportedOperationException.class);
  }
}
