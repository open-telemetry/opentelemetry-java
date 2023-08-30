/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.incubator.metrics;

import io.opentelemetry.api.metrics.DoubleGaugeBuilder;
import java.util.function.Consumer;

/** Extended {@link DoubleGaugeBuilder} with experimental APIs. */
public interface ExtendedDoubleGaugeBuilder extends DoubleGaugeBuilder {

  /**
   * Builds and returns a DoubleGauge instrument with the configuration.
   *
   * <p>NOTE: This produces a synchronous gauge which records gauge values as they occur. Most users
   * will want to instead register an {@link #buildWithCallback(Consumer)} to asynchronously observe
   * the value of the gauge when metrics are collected.
   *
   * @return The DoubleGauge instrument.
   */
  DoubleGauge build();

  /** Specify advice for gauge implementations. */
  default DoubleGaugeBuilder setAdvice(Consumer<DoubleGaugeAdviceConfigurer> adviceConsumer) {
    return this;
  }
}
