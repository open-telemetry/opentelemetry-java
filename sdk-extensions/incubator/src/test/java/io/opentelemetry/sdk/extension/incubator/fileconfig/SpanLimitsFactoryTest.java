/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static org.mockito.Mockito.mock;

import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.SpanLimits;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class SpanLimitsFactoryTest {

  @Test
  void create_Null() {
    assertThat(
            SpanLimitsFactory.getInstance()
                .create(null, mock(SpiHelper.class), Collections.emptyList()))
        .isEqualTo(io.opentelemetry.sdk.trace.SpanLimits.getDefault());
  }

  @Test
  void create_Defaults() {
    assertThat(
            SpanLimitsFactory.getInstance()
                .create(new SpanLimits(), mock(SpiHelper.class), Collections.emptyList()))
        .isEqualTo(io.opentelemetry.sdk.trace.SpanLimits.getDefault());
  }

  @Test
  void create() {
    assertThat(
            SpanLimitsFactory.getInstance()
                .create(
                    new SpanLimits()
                        .withAttributeCountLimit(1)
                        .withAttributeValueLengthLimit(2)
                        .withEventCountLimit(3)
                        .withLinkCountLimit(4)
                        .withEventAttributeCountLimit(5)
                        .withLinkAttributeCountLimit(6),
                    mock(SpiHelper.class),
                    Collections.emptyList()))
        .isEqualTo(
            io.opentelemetry.sdk.trace.SpanLimits.builder()
                .setMaxNumberOfAttributes(1)
                .setMaxAttributeValueLength(2)
                .setMaxNumberOfEvents(3)
                .setMaxNumberOfLinks(4)
                .setMaxNumberOfAttributesPerEvent(5)
                .setMaxNumberOfAttributesPerLink(6)
                .build());
  }
}
