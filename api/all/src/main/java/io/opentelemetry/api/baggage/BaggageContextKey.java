/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.baggage;

import io.opentelemetry.context.ContextKey;
import javax.annotation.concurrent.Immutable;

/** Util class to hold on to the key for storing Baggage in the Context. */
@Immutable
class BaggageContextKey {
  static final ContextKey<Baggage> KEY = ContextKey.named("opentelemetry-baggage-key");

  private BaggageContextKey() {}
}
