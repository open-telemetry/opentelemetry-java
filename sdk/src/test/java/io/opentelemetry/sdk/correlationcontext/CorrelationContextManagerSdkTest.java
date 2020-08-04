/*
 * Copyright 2019, OpenTelemetry Authors
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

package io.opentelemetry.sdk.correlationcontext;

import static org.assertj.core.api.Assertions.assertThat;

import io.grpc.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.correlationcontext.CorrelationContext;
import io.opentelemetry.correlationcontext.CorrelationsContextUtils;
import io.opentelemetry.correlationcontext.EmptyCorrelationContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/** Unit tests for {@link CorrelationContextManagerSdk}. */
// Need to suppress warnings for MustBeClosed because Android 14 does not support
// try-with-resources.
@SuppressWarnings("MustBeClosedChecker")
class CorrelationContextManagerSdkTest {
  @Mock private CorrelationContext distContext;
  private final CorrelationContextManagerSdk contextManager = new CorrelationContextManagerSdk();

  @BeforeEach
  void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  void testGetCurrentContext_DefaultContext() {
    assertThat(contextManager.getCurrentContext()).isSameAs(EmptyCorrelationContext.getInstance());
  }

  @Test
  void testGetCurrentContext_ContextSetToNull() {
    Context orig =
        CorrelationsContextUtils.withCorrelationContext(null, Context.current()).attach();
    try {
      CorrelationContext distContext = contextManager.getCurrentContext();
      assertThat(distContext).isNotNull();
      assertThat(distContext.getEntries()).isEmpty();
    } finally {
      Context.current().detach(orig);
    }
  }

  @Test
  void testWithCorrelationContext() {
    assertThat(contextManager.getCurrentContext()).isSameAs(EmptyCorrelationContext.getInstance());
    try (Scope wtm = contextManager.withContext(distContext)) {
      assertThat(contextManager.getCurrentContext()).isSameAs(distContext);
    }
    assertThat(contextManager.getCurrentContext()).isSameAs(EmptyCorrelationContext.getInstance());
  }

  @Test
  void testWithCorrelationContextUsingWrap() {
    Runnable runnable;
    try (Scope wtm = contextManager.withContext(distContext)) {
      assertThat(contextManager.getCurrentContext()).isSameAs(distContext);
      runnable =
          Context.current()
              .wrap(
                  () -> {
                    assertThat(contextManager.getCurrentContext()).isSameAs(distContext);
                  });
    }
    assertThat(contextManager.getCurrentContext()).isSameAs(EmptyCorrelationContext.getInstance());
    // When we run the runnable we will have the CorrelationContext in the current Context.
    runnable.run();
  }
}
