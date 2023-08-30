/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static org.mockito.Mockito.mock;

import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.LogRecordLimits;
import io.opentelemetry.sdk.logs.LogLimits;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class LogLimitsFactoryTest {

  @Test
  void create_Null() {
    assertThat(
            LogLimitsFactory.getInstance()
                .create(null, mock(SpiHelper.class), Collections.emptyList()))
        .isEqualTo(LogLimits.getDefault());
  }

  @Test
  void create_Defaults() {
    assertThat(
            LogLimitsFactory.getInstance()
                .create(new LogRecordLimits(), mock(SpiHelper.class), Collections.emptyList()))
        .isEqualTo(LogLimits.getDefault());
  }

  @Test
  void create() {
    assertThat(
            LogLimitsFactory.getInstance()
                .create(
                    new LogRecordLimits()
                        .withAttributeCountLimit(1)
                        .withAttributeValueLengthLimit(2),
                    mock(SpiHelper.class),
                    Collections.emptyList()))
        .isEqualTo(
            LogLimits.builder().setMaxNumberOfAttributes(1).setMaxAttributeValueLength(2).build());
  }
}
