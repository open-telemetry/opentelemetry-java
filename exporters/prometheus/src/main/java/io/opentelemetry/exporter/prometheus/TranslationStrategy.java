/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.prometheus;

/**
 * Controls how OpenTelemetry metric and label names are translated to Prometheus format.
 *
 * @see <a
 *     href="https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/metrics/sdk_exporters/prometheus.md#configuration">Prometheus
 *     Exporter Configuration</a>
 */
public enum TranslationStrategy {

  /**
   * Default. Non-standard characters (dots, dashes, etc.) are converted to underscores. Type
   * suffixes ({@code _total} for counters) and unit suffixes are appended. Repeated underscores are
   * collapsed.
   */
  UNDERSCORE_ESCAPING_WITH_SUFFIXES,

  /**
   * Same escaping as {@link #UNDERSCORE_ESCAPING_WITH_SUFFIXES} but no type or unit suffixes are
   * appended.
   */
  UNDERSCORE_ESCAPING_WITHOUT_SUFFIXES,

  /** Names pass through without escaping (UTF-8 preserved). Type and unit suffixes are appended. */
  NO_UTF8_ESCAPING_WITH_SUFFIXES,

  /**
   * Full passthrough for both metric and label names. No escaping, no suffixes. Metric names are
   * passed through exactly as provided by the OpenTelemetry SDK.
   */
  NO_TRANSLATION;

  boolean shouldEscape() {
    return this == UNDERSCORE_ESCAPING_WITH_SUFFIXES
        || this == UNDERSCORE_ESCAPING_WITHOUT_SUFFIXES;
  }

  boolean shouldAddSuffixes() {
    return this == UNDERSCORE_ESCAPING_WITH_SUFFIXES || this == NO_UTF8_ESCAPING_WITH_SUFFIXES;
  }
}
