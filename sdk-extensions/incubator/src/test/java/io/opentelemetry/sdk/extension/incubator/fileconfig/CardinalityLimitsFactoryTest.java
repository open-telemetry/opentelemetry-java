/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static org.mockito.Mockito.mock;

import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.CardinalityLimitsModel;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.export.CardinalityLimitSelector;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class CardinalityLimitsFactoryTest {

  @ParameterizedTest
  @MethodSource("createTestCases")
  void create(CardinalityLimitsModel model, CardinalityLimitSelector expectedResult) {
    CardinalityLimitSelector cardinalityLimitSelector =
        CardinalityLimitsFactory.getInstance().create(model, mock(DeclarativeConfigContext.class));

    for (InstrumentType instrumentType : InstrumentType.values()) {
      assertThat(cardinalityLimitSelector.getCardinalityLimit(instrumentType))
          .describedAs(instrumentType.toString())
          .isEqualTo(expectedResult.getCardinalityLimit(instrumentType));
    }
  }

  private static Stream<Arguments> createTestCases() {
    return Stream.of(
        Arguments.of(
            new CardinalityLimitsModel(),
            CardinalityLimitSelector.defaultCardinalityLimitSelector()),
        Arguments.of(
            new CardinalityLimitsModel().withDefault(10).withCounter(1),
            (CardinalityLimitSelector)
                instrumentType -> {
                  if (instrumentType == InstrumentType.COUNTER) {
                    return 1;
                  }
                  return 10;
                }),
        Arguments.of(
            new CardinalityLimitsModel()
                .withCounter(1)
                .withUpDownCounter(2)
                .withHistogram(3)
                .withObservableCounter(4)
                .withObservableUpDownCounter(5)
                .withObservableGauge(6)
                .withGauge(7),
            (CardinalityLimitSelector)
                instrumentType -> {
                  switch (instrumentType) {
                    case COUNTER:
                      return 1;
                    case UP_DOWN_COUNTER:
                      return 2;
                    case HISTOGRAM:
                      return 3;
                    case OBSERVABLE_COUNTER:
                      return 4;
                    case OBSERVABLE_UP_DOWN_COUNTER:
                      return 5;
                    case OBSERVABLE_GAUGE:
                      return 6;
                    case GAUGE:
                      return 7;
                  }
                  return 2000;
                }));
  }
}
