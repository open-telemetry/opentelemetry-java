/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.spi.traces;

import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.trace.export.SpanExporter;

/**
 * A service provider interface (SPI) for wrapping autoconfigured {@link SpanExporter}s. For any
 * {@link SpanExporter} automatically created by autoconfiguration, implementations of this
 * interface will be invoked to replace it, commonly with a wrapping {@link SpanExporter} which uses
 * {@link io.opentelemetry.sdk.trace.data.DelegatingSpanData} to adjust the exported data.
 */
public interface SpanExporterWrapper {

  /** Returns a {@link SpanExporter} which will replace the given {@code exporter}. */
  SpanExporter wrap(SpanExporter exporter, ConfigProperties config);
}
