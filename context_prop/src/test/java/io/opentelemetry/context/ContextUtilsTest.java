/*
 * Copyright 2020, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
