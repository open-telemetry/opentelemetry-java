/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.context;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;

class StorageWrappersTest {

  private static final ContextKey<String> ANIMAL = ContextKey.named("key");

  private static final AtomicInteger scopeOpenedCount = new AtomicInteger();
  private static final AtomicInteger scopeClosedCount = new AtomicInteger();

  @SuppressWarnings("UnnecessaryLambda")
  private static final Function<ContextStorage, ContextStorage> wrapper =
      delegate ->
          new ContextStorage() {
            @Override
            public Scope attach(Context toAttach) {
              Scope scope = delegate.attach(toAttach);
              scopeOpenedCount.incrementAndGet();
              return () -> {
                scope.close();
                scopeClosedCount.incrementAndGet();
              };
            }

            @Override
            public Context current() {
              return delegate.current();
            }
          };

  @BeforeEach
  void resetCounts() {
    scopeOpenedCount.set(0);
    scopeClosedCount.set(0);
  }

  // Run twice to ensure second wrapping has no effect.
  @RepeatedTest(2)
  void wrapAndInitialize() {
    ContextStorage.addWrapper(wrapper);

    assertThat(scopeOpenedCount).hasValue(0);
    assertThat(scopeClosedCount).hasValue(0);

    try (Scope ignored = Context.current().with(ANIMAL, "koala").makeCurrent()) {
      assertThat(Context.current().get(ANIMAL)).isEqualTo("koala");
    }

    assertThat(scopeOpenedCount).hasValue(1);
    assertThat(scopeClosedCount).hasValue(1);
  }
}
