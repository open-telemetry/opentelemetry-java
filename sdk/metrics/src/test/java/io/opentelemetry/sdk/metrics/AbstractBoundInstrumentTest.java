/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.metrics.aggregator.Aggregator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AbstractBoundInstrumentTest {
  @Mock private Aggregator aggregator;

  @Test
  void bindMapped() {
    TestBoundInstrument testBoundInstrument = new TestBoundInstrument(aggregator);
    assertThat(testBoundInstrument.bind()).isTrue();
    testBoundInstrument.unbind();
    assertThat(testBoundInstrument.bind()).isTrue();
    assertThat(testBoundInstrument.bind()).isTrue();
    testBoundInstrument.unbind();
    assertThat(testBoundInstrument.bind()).isTrue();
    testBoundInstrument.unbind();
    testBoundInstrument.unbind();
  }

  @Test
  void tryUnmap_BoundInstrument() {
    TestBoundInstrument testBoundInstrument = new TestBoundInstrument(aggregator);
    assertThat(testBoundInstrument.bind()).isTrue();
    assertThat(testBoundInstrument.tryUnmap()).isFalse();
    testBoundInstrument.unbind();
    // The binding is by default bound, so need an extra unbind.
    assertThat(testBoundInstrument.tryUnmap()).isFalse();
    testBoundInstrument.unbind();
    assertThat(testBoundInstrument.tryUnmap()).isTrue();
  }

  @Test
  void tryUnmap_BoundInstrument_MultipleTimes() {
    TestBoundInstrument testBoundInstrument = new TestBoundInstrument(aggregator);
    assertThat(testBoundInstrument.bind()).isTrue();
    assertThat(testBoundInstrument.bind()).isTrue();
    assertThat(testBoundInstrument.bind()).isTrue();
    assertThat(testBoundInstrument.tryUnmap()).isFalse();
    testBoundInstrument.unbind();
    assertThat(testBoundInstrument.bind()).isTrue();
    assertThat(testBoundInstrument.tryUnmap()).isFalse();
    testBoundInstrument.unbind();
    assertThat(testBoundInstrument.tryUnmap()).isFalse();
    testBoundInstrument.unbind();
    assertThat(testBoundInstrument.tryUnmap()).isFalse();
    testBoundInstrument.unbind();
    // The binding is by default bound, so need an extra unbind.
    assertThat(testBoundInstrument.tryUnmap()).isFalse();
    testBoundInstrument.unbind();
    assertThat(testBoundInstrument.tryUnmap()).isTrue();
  }

  @Test
  void bind_ThenUnmap_ThenTryToBind() {
    TestBoundInstrument testBoundInstrument = new TestBoundInstrument(aggregator);
    testBoundInstrument.unbind();
    assertThat(testBoundInstrument.tryUnmap()).isTrue();
    assertThat(testBoundInstrument.bind()).isFalse();
    testBoundInstrument.unbind();
  }

  @Test
  void recordDoubleValue() {
    TestBoundInstrument testBoundInstrument = new TestBoundInstrument(aggregator);
    Mockito.verifyNoInteractions(aggregator);
    Mockito.doNothing().when(aggregator).recordDouble(Mockito.anyDouble());
    testBoundInstrument.recordDouble(1.2);
    Mockito.verify(aggregator, Mockito.times(1)).recordDouble(1.2);
  }

  @Test
  void recordLongValue() {
    TestBoundInstrument testBoundInstrument = new TestBoundInstrument(aggregator);
    Mockito.verifyNoInteractions(aggregator);
    Mockito.doNothing().when(aggregator).recordLong(Mockito.anyLong());
    testBoundInstrument.recordLong(13);
    Mockito.verify(aggregator, Mockito.times(1)).recordLong(13);
  }

  private static final class TestBoundInstrument extends AbstractBoundInstrument {
    TestBoundInstrument(Aggregator aggregator) {
      super(aggregator);
    }
  }
}
