/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.incubator.common.ContextAttributes;
import io.opentelemetry.context.Context;

final class StateIncubatingUtil {

  static Attributes getContextAttributes(Context context) {
    return ContextAttributes.get(context);
  }

  private StateIncubatingUtil() {}
}
