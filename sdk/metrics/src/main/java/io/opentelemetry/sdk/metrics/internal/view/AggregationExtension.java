/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.view;

import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.internal.aggregator.AggregatorFactory;
import io.opentelemetry.sdk.metrics.internal.exemplar.ExemplarReservoirFactory;

/**
 * An interface which allows customized configuration of aggregators.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public interface AggregationExtension extends Aggregation, AggregatorFactory {
  /** Override the exemplar reservoir used for this aggregation. */
  AggregationExtension setExemplarReservoirFactory(ExemplarReservoirFactory reservoirFactory);
}
