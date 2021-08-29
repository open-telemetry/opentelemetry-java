/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import javax.annotation.concurrent.Immutable;

@Immutable
@AutoValue
abstract class ImmutableValidSpanContext implements SpanContext {

  /**
   * This method is provided as an optimization when {@code traceIdHex} and {@code spanIdHex} have
   * already been validated. Only use this method if you are sure these have both been validated,
   * e.g. when using {@code traceIdHex} from a parent {@link SpanContext} and {@code spanIdHex} from
   * an {@link IdGenerator}.
   */
  static SpanContext createBypassingValidation(
      String traceIdHex, String spanIdHex, TraceFlags traceFlags, TraceState traceState) {
    return new AutoValue_ImmutableValidSpanContext(
        traceIdHex, spanIdHex, traceFlags, traceState, /* remote= */ false);
  }

  @Override
  public boolean isValid() {
    return true;
  }
}
