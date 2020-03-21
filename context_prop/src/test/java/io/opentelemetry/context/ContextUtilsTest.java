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

import static com.google.common.truth.Truth.assertThat;

import io.grpc.Context;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link DefaultContextPropagators}. */
@RunWith(JUnit4.class)
public class ContextUtilsTest {

  @Rule public final ExpectedException thrown = ExpectedException.none();

  private static final Context.Key<String> SIMPLE_KEY = Context.key("simple");

  @Test
  public void withScopedContextNull() {
    thrown.expect(NullPointerException.class);
    ContextUtils.withScopedContext(null);
  }

  @Test
  public void withScopedContext() {
    Context prevCtx = Context.current();
    Context ctx = Context.current().withValue(SIMPLE_KEY, "value1");
    Scope scope = ContextUtils.withScopedContext(ctx);
    try {
      assertThat(scope).isNotNull();
      assertThat(Context.current()).isEqualTo(ctx);

      Context ctx2 = Context.current().withValue(SIMPLE_KEY, "value2");
      Scope scope2 = ContextUtils.withScopedContext(ctx2);
      try {
        assertThat(scope2).isNotNull();
        assertThat(Context.current()).isEqualTo(ctx2);
      } finally {
        scope2.close();
      }
      assertThat(Context.current()).isEqualTo(ctx);
    } finally {
      scope.close();
    }
    assertThat(Context.current()).isEqualTo(prevCtx);
  }
}
