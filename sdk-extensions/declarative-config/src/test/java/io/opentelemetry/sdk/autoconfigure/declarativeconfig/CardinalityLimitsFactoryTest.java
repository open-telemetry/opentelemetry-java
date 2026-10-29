/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static org.mockito.Mockito.mock;

import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.CardinalityLimitsModel;
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
        Arguments.argumentSet(
            "defaults",
            new CardinalityLimitsModel(),
            CardinalityLimitSelector.defaultCardinalityLimitSelector()),
        Arguments.argumentSet(
            "default and counter",
            new CardinalityLimitsModel().setDefault(10).setCounter(1),
            (CardinalityLimitSelector)
                instrumentType -> {
                  if (instrumentType == InstrumentType.COUNTER) {
                    return 1;
                  }
                  return 10;
                }),
        Arguments.argumentSet(
            "all instrument types",
            new CardinalityLimitsModel()
                .setCounter(1)
                .setUpDownCounter(2)
                .setHistogram(3)
                .setObservableCounter(4)
                .setObservableUpDownCounter(5)
                .setObservableGauge(6)
                .setGauge(7),
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
