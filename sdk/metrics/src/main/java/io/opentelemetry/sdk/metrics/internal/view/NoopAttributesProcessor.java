/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.view;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;

class NoopAttributesProcessor extends AttributesProcessor {

  static final NoopAttributesProcessor NOOP = new NoopAttributesProcessor();

  private NoopAttributesProcessor() {}

  @Override
  public Attributes process(Attributes incoming, Context context) {
    return incoming;
  }

  @Override
  public boolean usesContext() {
    return false;
  }

  @Override
  public String toString() {
    return "NoopAttributesProcessor{}";
  }
}
