/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static org.mockito.Mockito.mock;

import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.MeterProviderModel;
import io.opentelemetry.sdk.metrics.ExemplarFilter;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ExemplarFilterFactoryTest {

  @ParameterizedTest
  @MethodSource("createTestCases")
  void create(MeterProviderModel.ExemplarFilter model, ExemplarFilter expectedResult) {
    ExemplarFilter exemplarFilter =
        ExemplarFilterFactory.getInstance().create(model, mock(DeclarativeConfigContext.class));
    assertThat(exemplarFilter.toString()).isEqualTo(expectedResult.toString());
  }

  private static Stream<Arguments> createTestCases() {
    return Stream.of(
        Arguments.argumentSet(
            "always_on", MeterProviderModel.ExemplarFilter.ALWAYS_ON, ExemplarFilter.alwaysOn()),
        Arguments.argumentSet(
            "always_off", MeterProviderModel.ExemplarFilter.ALWAYS_OFF, ExemplarFilter.alwaysOff()),
        Arguments.argumentSet(
            "trace_based",
            MeterProviderModel.ExemplarFilter.TRACE_BASED,
            ExemplarFilter.traceBased()));
  }
}
