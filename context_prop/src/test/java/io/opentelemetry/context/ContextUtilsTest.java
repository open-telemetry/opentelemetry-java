/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.context;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.grpc.Context;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link ContextUtils}. */
class ContextUtilsTest {

  private static final Context.Key<String> SIMPLE_KEY = Context.key("simple");

  @Test
  void withScopedContextNull() {
    assertThrows(NullPointerException.class, () -> ContextUtils.withScopedContext(null));
  }

  @Test
  void withScopedContext() {
    Context prevCtx = Context.current();
    Context ctx = Context.current().withValue(SIMPLE_KEY, "value1");
    try (Scope scope = ContextUtils.withScopedContext(ctx)) {
      assertThat(scope).isNotNull();
      assertThat(Context.current()).isEqualTo(ctx);

      Context ctx2 = Context.current().withValue(SIMPLE_KEY, "value2");
      try (Scope scope2 = ContextUtils.withScopedContext(ctx2)) {
        assertThat(scope2).isNotNull();
        assertThat(Context.current()).isEqualTo(ctx2);
      }
      assertThat(Context.current()).isEqualTo(ctx);
    }
    assertThat(Context.current()).isEqualTo(prevCtx);
  }
}
