/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.incubator.metrics;

import io.opentelemetry.api.metrics.LongGaugeBuilder;
import java.util.function.Consumer;

/** Extended {@link LongGaugeBuilder} with experimental APIs. */
public interface ExtendedLongGaugeBuilder extends LongGaugeBuilder {

  /**
   * Builds and returns a LongGauge instrument with the configuration.
   *
   * <p>NOTE: This produces a synchronous gauge which records gauge values as they occur. Most users
   * will want to instead register an {@link #buildWithCallback(Consumer)} to asynchronously observe
   * the value of the gauge when metrics are collected.
   *
   * @return The LongGauge instrument.
   */
  LongGauge build();

  /** Specify advice for gauge implementations. */
  default LongGaugeBuilder setAdvice(Consumer<LongGaugeAdviceConfigurer> adviceConsumer) {
    return this;
  }
}
