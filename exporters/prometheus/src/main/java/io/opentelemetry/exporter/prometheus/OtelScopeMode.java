package io.opentelemetry.exporter.prometheus;

public enum OtelScopeMode {
  /** No scope information is exported. */
  DISABLED,
  /**
   * Only the labels of the metrics are exported, but not the scope_info info metrics.
   *
   * <p>This will eventually be the default mode, but is opt-in for now.
   */
  LABELS_ONLY,
  /**
   * Both labels and scope_info are exported.
   *
   * <p>This is the default mode.
   */
  LABELS_AND_SCOPE_INFO;

  boolean isEnabled() {
    return this != DISABLED;
  }

  boolean isScopeInfoEnabled() {
    return this == LABELS_AND_SCOPE_INFO;
  }
}
