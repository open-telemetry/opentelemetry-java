/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.processor;

import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.api.metrics.common.Labels;

/** Uses {@link Baggage} to extract labels for metrics. Used with {@link BaggageLabelsProcessor} */
public interface BaggageMetricsLabelsExtractor {

  Labels fromBaggage(Baggage ctx);
}
