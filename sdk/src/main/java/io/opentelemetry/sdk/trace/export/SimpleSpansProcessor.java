/*
 * Copyright 2019, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.sdk.trace.export;

import com.google.common.annotations.VisibleForTesting;
import io.opentelemetry.sdk.common.export.ConfigBuilder;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.data.SpanData;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An implementation of the {@link SpanProcessor} that converts the {@link ReadableSpan} to {@link
 * SpanData} and passes it to the configured exporter.
 */
public final class SimpleSpansProcessor implements SpanProcessor {

  private static final Logger logger = Logger.getLogger(SimpleSpansProcessor.class.getName());

  private final SpanExporter spanExporter;
  private final boolean sampled;

  private SimpleSpansProcessor(SpanExporter spanExporter, boolean sampled) {
    this.spanExporter = Objects.requireNonNull(spanExporter, "spanExporter");
    this.sampled = sampled;
  }

  @Override
  public void onStart(ReadableSpan span) {
    // Do nothing.
  }

  @Override
  public boolean isStartRequired() {
    return false;
  }

  @Override
  public void onEnd(ReadableSpan span) {
    if (sampled && !span.getSpanContext().getTraceFlags().isSampled()) {
      return;
    }
    try {
      List<SpanData> spans = Collections.singletonList(span.toSpanData());
      spanExporter.export(spans);
    } catch (Throwable e) {
      logger.log(Level.WARNING, "Exception thrown by the export.", e);
    }
  }

  @Override
  public boolean isEndRequired() {
    return true;
  }

  @Override
  public void shutdown() {
    spanExporter.shutdown();
  }

  @Override
  public void forceFlush() {
    // Do nothing.
  }

  /**
   * Returns a new Builder for {@link SimpleSpansProcessor}.
   *
   * @param spanExporter the {@code SpanExporter} to where the Spans are pushed.
   * @return a new {@link SimpleSpansProcessor}.
   * @throws NullPointerException if the {@code spanExporter} is {@code null}.
   */
  public static Builder newBuilder(SpanExporter spanExporter) {
    return new Builder(spanExporter);
  }

  /** Builder class for {@link SimpleSpansProcessor}. */
  public static final class Builder extends ConfigBuilder<Builder> {

    private static final String KEY_SAMPLED = "otel.ssp.export.sampled";

    private static final boolean DEFAULT_EXPORT_ONLY_SAMPLED = true;
    private final SpanExporter spanExporter;
    private boolean sampled = DEFAULT_EXPORT_ONLY_SAMPLED;

    private Builder(SpanExporter spanExporter) {
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
    @VisibleForTesting
    @Override
    protected Builder fromConfigMap(
        Map<String, String> configMap, NamingConvention namingConvention) {
      configMap = namingConvention.normalize(configMap);
      Boolean boolValue = getBooleanProperty(KEY_SAMPLED, configMap);
      if (boolValue != null) {
        this.setExportOnlySampled(boolValue);
      }
      return this;
    }

    /**
     * Sets the configuration values from the given properties object for only the available keys.
     * This method looks for the following keys:
     *
     * <ul>
     *   <li>{@code otel.ssp.export.sampled}: to set whether only sampled spans should be exported.
     * </ul>
     *
     * @param properties {@link Properties} holding the configuration values.
     * @return this.
     */
    @Override
    public Builder readProperties(Properties properties) {
      return super.readProperties(properties);
    }

    /**
     * Sets the configuration values from environment variables for only the available keys. This
     * method looks for the following keys:
     *
     * <ul>
     *   <li>{@code OTEL_SSP_EXPORT_SAMPLED}: to set whether only sampled spans should be exported.
     * </ul>
     *
     * @return this.
     */
    @Override
    public Builder readEnvironmentVariables() {
      return super.readEnvironmentVariables();
    }

    /**
     * Sets the configuration values from system properties for only the available keys. This method
     * looks for the following keys:
     *
     * <ul>
     *   <li>{@code otel.ssp.export.sampled}: to set whether only sampled spans should be reported.
     * </ul>
     *
     * @return this.
     */
    @Override
    public Builder readSystemProperties() {
      return super.readSystemProperties();
    }

    /**
     * Set whether only sampled spans should be exported.
     *
     * <p>Default value is {@code true}.
     *
     * @see SimpleSpansProcessor.Builder#DEFAULT_EXPORT_ONLY_SAMPLED
     * @param sampled report only sampled spans.
     * @return this.
     */
    public Builder setExportOnlySampled(boolean sampled) {
      this.sampled = sampled;
      return this;
    }

    // TODO: Add metrics for total exported spans.
    // TODO: Consider to add support for constant Attributes and/or Resource.

    /**
     * Returns a new {@link SimpleSpansProcessor} that converts spans to proto and forwards them to
     * the given {@code spanExporter}.
     *
     * @return a new {@link SimpleSpansProcessor}.
     * @throws NullPointerException if the {@code spanExporter} is {@code null}.
     */
    public SimpleSpansProcessor build() {
      return new SimpleSpansProcessor(spanExporter, sampled);
    }
  }
}
