/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.prometheus;

/**
 * Controls how OpenTelemetry metric and label names are translated to Prometheus format.
 *
 * @see <a
 *     href="https://opentelemetry.io/docs/specs/otel/metrics/sdk_exporters/prometheus/">Prometheus
 *     exporter configuration</a>
 */
public enum TranslationStrategy {
  /**
   * Default. Non-standard characters are converted to underscores, and type / unit suffixes are
   * attached.
   */
  UNDERSCORE_ESCAPING_WITH_SUFFIXES,

  /**
   * Non-standard characters are converted to underscores, but type / unit suffixes are not attached
   * by the exporter.
   */
  UNDERSCORE_ESCAPING_WITHOUT_SUFFIXES,

  /** UTF-8 metric and label names are preserved, while type / unit suffixes are still attached. */
  NO_UTF8_ESCAPING_WITH_SUFFIXES,

  /** Metric and label names are passed through without translation. */
  NO_TRANSLATION;

  boolean shouldEscape() {
    return this == UNDERSCORE_ESCAPING_WITH_SUFFIXES
        || this == UNDERSCORE_ESCAPING_WITHOUT_SUFFIXES;
  }
}
