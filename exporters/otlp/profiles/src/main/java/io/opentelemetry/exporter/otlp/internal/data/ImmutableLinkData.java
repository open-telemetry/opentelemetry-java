/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal.data;

import com.google.auto.value.AutoValue;
import io.opentelemetry.exporter.otlp.profiles.LinkData;
import javax.annotation.concurrent.Immutable;

/**
 * Auto value implementation of {@link LinkData}, which represents a connection from a profile
 * Sample to a trace Span.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
@Immutable
@AutoValue
public abstract class ImmutableLinkData implements LinkData {

  /**
   * Returns a new LinkData representing an association to the given trace span.
   *
   * @return a new LinkData representing an association to the given trace span.
   */
  public static LinkData create(String traceId, String spanId) {
    return new AutoValue_ImmutableLinkData(traceId, spanId);
  }

  ImmutableLinkData() {}
}
