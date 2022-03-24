/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal;

import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.export.MetricReader;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Utilities for exporter builders.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class ExporterBuilderUtil {

  /** Validate OTLP endpoint. */
  public static URI validateEndpoint(String endpoint) {
    URI uri;
    try {
      uri = new URI(endpoint);
    } catch (URISyntaxException e) {
      throw new IllegalArgumentException("Invalid endpoint, must be a URL: " + endpoint, e);
    }

    if (uri.getScheme() == null
        || (!uri.getScheme().equals("http") && !uri.getScheme().equals("https"))) {
      throw new IllegalArgumentException(
          "Invalid endpoint, must start with http:// or https://: " + uri);
    }
    return uri;
  }

  /**
   * A {@link MetricReader#getAggregationTemporality(InstrumentType)} function that indicates a
   * cumulative preference.
   *
   * <p>{@link AggregationTemporality#CUMULATIVE} is returned for all instrument types.
   */
  public static AggregationTemporality cumulativePreferred(InstrumentType unused) {
    return AggregationTemporality.CUMULATIVE;
  }

  /**
   * A {@link MetricReader#getAggregationTemporality(InstrumentType)} function that indicates a
   * delta preference.
   *
   * <p>{@link AggregationTemporality#DELTA} is returned for counter (sync and async) and histogram
   * instruments. {@link AggregationTemporality#CUMULATIVE} is returned for up down counter (sync
   * and async) instruments.
   */
  public static AggregationTemporality deltaPreferred(InstrumentType instrumentType) {
    switch (instrumentType) {
      case UP_DOWN_COUNTER:
      case OBSERVABLE_UP_DOWN_COUNTER:
        return AggregationTemporality.CUMULATIVE;
      case COUNTER:
      case OBSERVABLE_COUNTER:
      case HISTOGRAM:
      default:
        return AggregationTemporality.DELTA;
    }
  }

  private ExporterBuilderUtil() {}
}
