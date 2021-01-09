/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.export;

import java.util.Map;
import java.util.Objects;

/**
 * Builder class for {@link SimpleSpanProcessor}.
 *
 * @deprecated Use {@link SimpleSpanProcessor#create(SpanExporter)}
 */
@Deprecated
@SuppressWarnings("deprecation") // Remove after ConfigBuilder is deleted
public final class SimpleSpanProcessorBuilder
    extends io.opentelemetry.sdk.common.export.ConfigBuilder<SimpleSpanProcessorBuilder> {

  private static final String KEY_SAMPLED = "otel.ssp.export.sampled";

  // Visible for testing
  static final boolean DEFAULT_EXPORT_ONLY_SAMPLED = true;
  private final SpanExporter spanExporter;
  private boolean exportOnlySampled = DEFAULT_EXPORT_ONLY_SAMPLED;

  SimpleSpanProcessorBuilder(SpanExporter spanExporter) {
    this.spanExporter = Objects.requireNonNull(spanExporter, "spanExporter");
  }

  /**
   * Sets the configuration values from the given configuration map for only the available keys.
   * This method looks for the following keys:
   *
   * <ul>
   *   <li>{@code otel.ssp.export.sampled}: to set whether only sampled spans should be exported.
   * </ul>
   *
   * @param configMap {@link Map} holding the configuration values.
   * @return this.
   */
  @Override
  protected SimpleSpanProcessorBuilder fromConfigMap(
      Map<String, String> configMap, NamingConvention namingConvention) {
    configMap = namingConvention.normalize(configMap);
    Boolean boolValue = getBooleanProperty(KEY_SAMPLED, configMap);
    if (boolValue != null) {
      return this.setExportOnlySampled(boolValue);
    }
    return this;
  }

  /**
   * Set whether only sampled spans should be exported.
   *
   * <p>Default value is {@code true}.
   *
   * @param exportOnlySampled if {@code true} report only sampled spans.
   * @return this.
   */
  public SimpleSpanProcessorBuilder setExportOnlySampled(boolean exportOnlySampled) {
    this.exportOnlySampled = exportOnlySampled;
    return this;
  }

  // Visible for testing
  boolean getExportOnlySampled() {
    return exportOnlySampled;
  }

  // TODO: Add metrics for total exported spans.
  // TODO: Consider to add support for constant Attributes and/or Resource.

  /**
   * Returns a new {@link SimpleSpanProcessor} that converts spans to proto and forwards them to the
   * given {@code spanExporter}.
   *
   * @return a new {@link SimpleSpanProcessor}.
   * @throws NullPointerException if the {@code spanExporter} is {@code null}.
   */
  public SimpleSpanProcessor build() {
    return new SimpleSpanProcessor(spanExporter, exportOnlySampled);
  }
}
