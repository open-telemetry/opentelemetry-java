/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.trace;

import io.opentelemetry.context.ContextKey;
import javax.annotation.concurrent.Immutable;

/** Util class to hold on to the key for storing a SpanLinks in the Context. */
@Immutable
final class SpanLinksKey {
  static final ContextKey<SpanLinks> KEY = ContextKey.named("opentelemetry-trace-spanlinks-key");

  private SpanLinksKey() {}
}
