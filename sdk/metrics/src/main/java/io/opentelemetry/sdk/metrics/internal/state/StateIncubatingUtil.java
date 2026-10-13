/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.incubator.common.ContextAttributes;
import io.opentelemetry.context.Context;

final class StateIncubatingUtil {
  private static final boolean INCUBATOR_AVAILABLE;

  static {
    boolean incubatorAvailable = false;
    try {
      Class.forName("io.opentelemetry.api.incubator.common.ContextAttributes");
      incubatorAvailable = true;
    } catch (ClassNotFoundException e) {
      // Not available
    }
    INCUBATOR_AVAILABLE = incubatorAvailable;
  }

  static Attributes getContextAttributes(Context context) {
    if (!INCUBATOR_AVAILABLE) {
      return Attributes.empty();
    }
    return ContextAttributes.get(context);
  }

  private StateIncubatingUtil() {}
}
