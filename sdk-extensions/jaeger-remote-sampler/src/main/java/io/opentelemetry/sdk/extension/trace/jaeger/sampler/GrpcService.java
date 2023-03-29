/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.trace.jaeger.sampler;

import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.sdk.common.CompletableResultCode;

interface GrpcService {

  /**
   * Exports the {@code exportRequest} which is a request {@link Marshaler} for {@code numItems}
   * items.
   */
  SamplingStrategyResponseUnMarshaler execute(
      SamplingStrategyParametersMarshaler request, SamplingStrategyResponseUnMarshaler response);

  /** Shuts the exporter down. */
  CompletableResultCode shutdown();
}
