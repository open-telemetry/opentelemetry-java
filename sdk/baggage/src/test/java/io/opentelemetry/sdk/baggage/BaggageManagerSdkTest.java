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

package io.opentelemetry.sdk.baggage;

import static org.assertj.core.api.Assertions.assertThat;

import io.grpc.Context;
import io.opentelemetry.baggage.Baggage;
import io.opentelemetry.baggage.BaggageUtils;
import io.opentelemetry.baggage.EmptyBaggage;
import io.opentelemetry.context.Scope;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/** Unit tests for {@link BaggageManagerSdk}. */
// Need to suppress warnings for MustBeClosed because Android 14 does not support
// try-with-resources.
@SuppressWarnings("MustBeClosedChecker")
class BaggageManagerSdkTest {
  @Mock private Baggage distContext;
  private final BaggageManagerSdk contextManager = new BaggageManagerSdk();

  @BeforeEach
  void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  void testGetCurrentContext_DefaultContext() {
    assertThat(contextManager.getCurrentBaggage()).isSameAs(EmptyBaggage.getInstance());
  }

  @Test
  void testGetCurrentContext_ContextSetToNull() {
    Context orig = BaggageUtils.withBaggage(null, Context.current()).attach();
    try {
      Baggage distContext = contextManager.getCurrentBaggage();
      assertThat(distContext).isNotNull();
      assertThat(distContext.getEntries()).isEmpty();
    } finally {
      Context.current().detach(orig);
    }
  }

  @Test
  void testWithBaggage() {
    assertThat(contextManager.getCurrentBaggage()).isSameAs(EmptyBaggage.getInstance());
    try (Scope wtm = contextManager.withContext(distContext)) {
      assertThat(contextManager.getCurrentBaggage()).isSameAs(distContext);
    }
    assertThat(contextManager.getCurrentBaggage()).isSameAs(EmptyBaggage.getInstance());
  }

  @Test
  void testWithBaggageUsingWrap() {
    Runnable runnable;
    try (Scope wtm = contextManager.withContext(distContext)) {
      assertThat(contextManager.getCurrentBaggage()).isSameAs(distContext);
      runnable =
          Context.current()
              .wrap(
                  () -> {
                    assertThat(contextManager.getCurrentBaggage()).isSameAs(distContext);
                  });
    }
    assertThat(contextManager.getCurrentBaggage()).isSameAs(EmptyBaggage.getInstance());
    // When we run the runnable we will have the Baggage in the current Context.
    runnable.run();
  }
}
