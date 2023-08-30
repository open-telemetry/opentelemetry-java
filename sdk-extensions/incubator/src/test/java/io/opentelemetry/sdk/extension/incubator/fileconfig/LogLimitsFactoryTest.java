/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static org.mockito.Mockito.mock;

import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.AttributeLimits;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.LogRecordLimits;
import io.opentelemetry.sdk.logs.LogLimits;
import java.util.Collections;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class LogLimitsFactoryTest {

  @ParameterizedTest
  @MethodSource("createArguments")
  void create(LogRecordLimitsAndAttributeLimits model, LogLimits expectedLogLimits) {
    assertThat(
            LogLimitsFactory.getInstance()
                .create(model, mock(SpiHelper.class), Collections.emptyList()))
        .isEqualTo(expectedLogLimits);
  }

  private static Stream<Arguments> createArguments() {
    return Stream.of(
        Arguments.of(null, LogLimits.builder().build()),
        Arguments.of(
            LogRecordLimitsAndAttributeLimits.create(null, null), LogLimits.builder().build()),
        Arguments.of(
            LogRecordLimitsAndAttributeLimits.create(new AttributeLimits(), new LogRecordLimits()),
            LogLimits.builder().build()),
        Arguments.of(
            LogRecordLimitsAndAttributeLimits.create(
                new AttributeLimits().withAttributeValueLengthLimit(1).withAttributeCountLimit(2),
                new LogRecordLimits()),
            LogLimits.builder().setMaxAttributeValueLength(1).setMaxNumberOfAttributes(2).build()),
        Arguments.of(
            LogRecordLimitsAndAttributeLimits.create(
                new AttributeLimits().withAttributeValueLengthLimit(1).withAttributeCountLimit(2),
                new LogRecordLimits().withAttributeValueLengthLimit(3).withAttributeCountLimit(4)),
            LogLimits.builder().setMaxAttributeValueLength(3).setMaxNumberOfAttributes(4).build()));
  }
}
