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

package io.opentelemetry.sdk.contrib.zpages;

import com.google.common.annotations.VisibleForTesting;
import io.opentelemetry.sdk.common.export.ConfigBuilder;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.trace.SpanId;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.concurrent.ThreadSafe;

/**
 * A {@link SpanProcessor} implementation for the traceZ zPage.
 *
 * <p>Configuration options for {@link io.opentelemetry.sdk.contrib.zpages.TracezSpanProcessor} can
 * be read from system properties, environment variables, or {@link java.util.Properties} objects.
 *
 * <p>For system properties and {@link java.util.Properties} objects, {@link
 * io.opentelemetry.sdk.contrib.zpages.TracezSpanProcessor} will look for the following names:
 *
 * <ul>
 *   <li>{@code otel.ssp.export.sampled}: sets whether only sampled spans should be exported.
 * </ul>
 *
 * <p>For environment variables, {@link io.opentelemetry.sdk.contrib.zpages.TracezSpanProcessor}
 * will look for the following names:
 *
 * <ul>
 *   <li>{@code OTEL_SSP_EXPORT_SAMPLED}: sets whether only sampled spans should be exported.
 * </ul>
 */
@ThreadSafe
public final class TracezSpanProcessor implements SpanProcessor {
  private final Map<SpanId, ReadableSpan> runningSpanCache;
  private final Map<SpanId, ReadableSpan> completedSpanCache;
  private final boolean sampled;

  /**
   * Constructor for {@link io.opentelemetry.sdk.contrib.zpages.TracezSpanProcessor}.
   *
   * @param sampled report only sampled spans.
   */
  public TracezSpanProcessor(boolean sampled) {
    runningSpanCache = new HashMap<>();
    completedSpanCache = new HashMap<>();
    this.sampled = sampled;
  }

  @Override
  public void onStart(ReadableSpan span) {
    synchronized (this) {
      runningSpanCache.put(span.getSpanContext().getSpanId(), span);
    }
  }

  @Override
  public boolean isStartRequired() {
    return true;
  }

  @Override
  public void onEnd(ReadableSpan span) {
    SpanId id = span.getSpanContext().getSpanId();
    synchronized (this) {
      runningSpanCache.remove(id);
      if (!sampled || span.getSpanContext().getTraceFlags().isSampled()) {
        completedSpanCache.put(id, span);
      }
    }
  }

  @Override
  public boolean isEndRequired() {
    return true;
  }

  @Override
  public void shutdown() {
    // Do nothing.
  }

  @Override
  public void forceFlush() {
    // Do nothing.
  }

  /**
   * Returns a Collection of all running spans for {@link
   * io.opentelemetry.sdk.contrib.zpages.TracezSpanProcessor}.
   *
   * @return a Collection of {@link io.opentelemetry.sdk.trace.ReadableSpan}.
   */
  public Collection<ReadableSpan> getRunningSpans() {
    synchronized (this) {
      return runningSpanCache.values();
    }
  }

  /**
   * Returns a Collection of all completed spans for {@link
   * io.opentelemetry.sdk.contrib.zpages.TracezSpanProcessor}.
   *
   * @return a Collection of {@link io.opentelemetry.sdk.trace.ReadableSpan}.
   */
  public Collection<ReadableSpan> getCompletedSpans() {
    synchronized (this) {
      return completedSpanCache.values();
    }
  }

  /**
   * Returns a new Builder for {@link io.opentelemetry.sdk.contrib.zpages.TracezSpanProcessor}.
   *
   * @return a new {@link io.opentelemetry.sdk.contrib.zpages.TracezSpanProcessor}.
   */
  public static Builder newBuilder() {
    return new Builder();
  }

  /** Builder class for {@link io.opentelemetry.sdk.contrib.zpages.TracezSpanProcessor}. */
  public static final class Builder extends ConfigBuilder<Builder> {

    private static final String KEY_SAMPLED = "otel.ssp.export.sampled";
    private static final boolean DEFAULT_EXPORT_ONLY_SAMPLED = true;
    private boolean sampled = DEFAULT_EXPORT_ONLY_SAMPLED;

    private Builder() {}

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
        return this.setExportOnlySampled(boolValue);
      }
      return this;
    }

    /**
     * Sets whether only sampled spans should be exported.
     *
     * <p>Default value is {@code true}.
     *
     * @see Builder#DEFAULT_EXPORT_ONLY_SAMPLED
     * @param sampled report only sampled spans.
     * @return this.
     */
    public Builder setExportOnlySampled(boolean sampled) {
      this.sampled = sampled;
      return this;
    }

    /**
     * Returns a new {@link io.opentelemetry.sdk.contrib.zpages.TracezSpanProcessor}.
     *
     * @return a new {@link io.opentelemetry.sdk.contrib.zpages.TracezSpanProcessor}.
     */
    public io.opentelemetry.sdk.contrib.zpages.TracezSpanProcessor build() {
      return new io.opentelemetry.sdk.contrib.zpages.TracezSpanProcessor(sampled);
    }
  }
}
