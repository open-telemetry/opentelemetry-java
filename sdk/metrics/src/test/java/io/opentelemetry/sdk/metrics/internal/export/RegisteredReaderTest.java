/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.export;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.metrics.export.MetricReader;
import io.opentelemetry.sdk.metrics.internal.view.ViewRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RegisteredReaderTest {

  @Mock private MetricReader reader;

  @Test
  void create_UniqueIdentity() {
    RegisteredReader registeredReader1 = RegisteredReader.create(reader, ViewRegistry.create());
    RegisteredReader registeredReader2 = RegisteredReader.create(reader, ViewRegistry.create());

    assertThat(registeredReader1).isEqualTo(registeredReader1);
    assertThat(registeredReader1).isNotEqualTo(registeredReader2);

    assertThat(registeredReader1.hashCode()).isEqualTo(registeredReader1.hashCode());
    assertThat(registeredReader1.hashCode()).isNotEqualTo(registeredReader2.hashCode());
  }

  @Test
  void getReader() {
    RegisteredReader registeredReader = RegisteredReader.create(reader, ViewRegistry.create());

    assertThat(registeredReader.getReader()).isSameAs(reader);
  }

  @Test
  void setAndGetLastCollectEpochNanos() {
    RegisteredReader registeredReader = RegisteredReader.create(reader, ViewRegistry.create());

    assertThat(registeredReader.getLastCollectEpochNanos()).isEqualTo(0);
    registeredReader.setLastCollectEpochNanos(1);
    assertThat(registeredReader.getLastCollectEpochNanos()).isEqualTo(1);
    registeredReader.setLastCollectEpochNanos(5);
    assertThat(registeredReader.getLastCollectEpochNanos()).isEqualTo(5);
  }
}
