/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.metrics;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.metrics.DoubleGaugeBuilder;
import java.util.List;
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

  /**
   * Specify the attribute advice, which suggests the recommended set of attribute keys to be used
   * for this gauge.
   */
  default ExtendedDoubleGaugeBuilder setAttributesAdvice(List<AttributeKey<?>> attributes) {
    return this;
  }
}
