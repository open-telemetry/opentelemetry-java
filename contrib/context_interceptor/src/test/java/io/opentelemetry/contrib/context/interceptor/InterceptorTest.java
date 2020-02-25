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

package io.opentelemetry.contrib.context.interceptor;

import static com.google.common.truth.Truth.assertThat;

import io.grpc.Context;
import io.grpc.override.ContextStorageOverride;
import javax.annotation.Nullable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link Interceptor}. */
@RunWith(JUnit4.class)
public class InterceptorTest {
  private static final Context.Key<String> contextKey = Context.key("myKeyContext");
  private final TestInterceptor testInterceptor = new TestInterceptor();

  @Test
  public void testDefault() {
    ContextStorageOverride.addInterceptor(testInterceptor);
    assertThat(contextKey.get()).isNull();
    assertThat(testInterceptor.currentValue).isNull();
  }

  @Test
  public void testAttachDetach() {
    ContextStorageOverride.addInterceptor(testInterceptor);
    Context context = Context.ROOT.withValue(contextKey, "myValue1");
    Context prev = context.attach();
    try {
      assertThat(testInterceptor.currentValue).isEqualTo("myValue1");
      Context newContext = context.withValue(contextKey, "myValue2");
      Context newPrev = newContext.attach();
      try {
        assertThat(testInterceptor.currentValue).isEqualTo("myValue2");
      } finally {
        newContext.detach(newPrev);
      }

    } finally {
      context.detach(prev);
    }
  }

  private static final class TestInterceptor implements Interceptor {
    @Nullable private String currentValue = null;

    @Override
    public void updated(Context oldContext, Context newContext) {
      assertThat(contextKey.get(oldContext)).isEqualTo(currentValue);
      currentValue = contextKey.get(newContext);
    }
  }
}
