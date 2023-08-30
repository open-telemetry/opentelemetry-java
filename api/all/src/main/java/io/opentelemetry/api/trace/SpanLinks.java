/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.trace;

import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.internal.ApiUsageLogger;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.ImplicitContextKeyed;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
public interface SpanLinks extends ImplicitContextKeyed {

  /**
   * Returns the {@link SpanLinks} from the specified {@link Context}, falling back to a default, no-op
   * {@link SpanLinks} if there is no span in the context.
   */
  static SpanLinks fromContext(Context context) {
    SpanLinks links = fromContextOrNull(context);
    return links == null ? ParentExcludedSpanLinks.create(Span.fromContext(context).getSpanContext()) : links;
  }

  /**
   * Returns the {@link SpanLinks} from the specified {@link Context}, or {@code null} if there is no
   * span in the context.
   */
  @Nullable
  static SpanLinks fromContextOrNull(Context context) {
    return context.get(SpanLinksKey.KEY);
  }

  ImplicitContextKeyed with(SpanContext spanContext);

  void consume(Consumer<SpanContext> consumer);

  @Override
  default Context storeInContext(Context context) {
    return context.with(SpanLinksKey.KEY, this);
  }
}
