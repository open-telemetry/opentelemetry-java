/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.CardinalityLimitsModel;
import io.opentelemetry.sdk.metrics.export.CardinalityLimitSelector;
import io.opentelemetry.sdk.metrics.internal.state.MetricStorage;
import javax.annotation.Nullable;

final class CardinalityLimitsFactory
    implements Factory<CardinalityLimitsModel, CardinalityLimitSelector> {

  private static final CardinalityLimitsFactory INSTANCE = new CardinalityLimitsFactory();

  private CardinalityLimitsFactory() {}

  static CardinalityLimitsFactory getInstance() {
    return INSTANCE;
  }

  @Override
  public CardinalityLimitSelector create(
      CardinalityLimitsModel model, DeclarativeConfigContext context) {
    int defaultLimit = getOrDefault(model.getDefault(), MetricStorage.DEFAULT_MAX_CARDINALITY);
    int counterLimit = getOrDefault(model.getCounter(), defaultLimit);
    int gaugeLimit = getOrDefault(model.getGauge(), defaultLimit);
    int histogramLimit = getOrDefault(model.getHistogram(), defaultLimit);
    int observableCounterLimit = getOrDefault(model.getObservableCounter(), defaultLimit);
    int observableGaugeLimit = getOrDefault(model.getObservableGauge(), defaultLimit);
    int observableUpDownCounterLimit =
        getOrDefault(model.getObservableUpDownCounter(), defaultLimit);
    int upDownCounterLimit = getOrDefault(model.getUpDownCounter(), defaultLimit);

    return instrumentType -> {
      switch (instrumentType) {
        case COUNTER:
          return counterLimit;
        case UP_DOWN_COUNTER:
          return upDownCounterLimit;
        case HISTOGRAM:
          return histogramLimit;
        case OBSERVABLE_COUNTER:
          return observableCounterLimit;
        case OBSERVABLE_UP_DOWN_COUNTER:
          return observableUpDownCounterLimit;
        case OBSERVABLE_GAUGE:
          return observableGaugeLimit;
        case GAUGE:
          return gaugeLimit;
      }
      return defaultLimit;
    };
  }

  private static int getOrDefault(@Nullable Integer value, int defaultValue) {
    return value == null ? defaultValue : value;
  }
}
