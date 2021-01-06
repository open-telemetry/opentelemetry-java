/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.context;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;
import io.github.netmikey.logunit.api.LogCapturer;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.event.Level;
import org.slf4j.event.LoggingEvent;

@SuppressWarnings("ClassCanBeStatic")
@ExtendWith(MockitoExtension.class)
class ContextTest {

  private static final ContextKey<String> ANIMAL = ContextKey.named("animal");
  private static final ContextKey<Object> BAG = ContextKey.named("bag");

  private static final Context CAT = Context.current().with(ANIMAL, "cat");

  @RegisterExtension
  LogCapturer logs =
      LogCapturer.create().captureForType(ThreadLocalContextStorage.class, Level.DEBUG);

  // Make sure all tests clean up
  @AfterEach
  void tearDown() {
    assertThat(Context.current()).isEqualTo(Context.root());
  }

  @Test
  void startsWithRoot() {
    assertThat(Context.current()).isEqualTo(Context.root());
  }

  @Test
  void canBeAttached() {
    Context context = Context.current().with(ANIMAL, "cat");
    assertThat(Context.current().get(ANIMAL)).isNull();
    try (Scope ignored = context.makeCurrent()) {
      assertThat(Context.current().get(ANIMAL)).isEqualTo("cat");

      try (Scope ignored2 = Context.root().makeCurrent()) {
        assertThat(Context.current().get(ANIMAL)).isNull();
      }

      assertThat(Context.current().get(ANIMAL)).isEqualTo("cat");
    }
    assertThat(Context.current().get(ANIMAL)).isNull();
  }

  @Test
  void attachSameTwice() {
    Context context = Context.current().with(ANIMAL, "cat");
    assertThat(Context.current().get(ANIMAL)).isNull();
    try (Scope ignored = context.makeCurrent()) {
      assertThat(Context.current().get(ANIMAL)).isEqualTo("cat");

      try (Scope ignored2 = context.makeCurrent()) {
        assertThat(Context.current().get(ANIMAL)).isEqualTo("cat");
      }

      assertThat(Context.current().get(ANIMAL)).isEqualTo("cat");
    }
    assertThat(Context.current().get(ANIMAL)).isNull();
  }

  @Test
  void newThreadStartsWithRoot() throws Exception {
    Context context = Context.current().with(ANIMAL, "cat");
    try (Scope ignored = context.makeCurrent()) {
      assertThat(Context.current().get(ANIMAL)).isEqualTo("cat");
      AtomicReference<Context> current = new AtomicReference<>();
      Thread thread = new Thread(() -> current.set(Context.current()));
      thread.start();
      thread.join();
      assertThat(current.get()).isEqualTo(Context.root());
    }
  }

  @Test
  public void closingScopeWhenNotActiveIsLogged() {
    Context initial = Context.current();
    Context context = initial.with(ANIMAL, "cat");
    try (Scope scope = context.makeCurrent()) {
      Context context2 = context.with(ANIMAL, "dog");
      try (Scope ignored = context2.makeCurrent()) {
        assertThat(Context.current().get(ANIMAL)).isEqualTo("dog");
        scope.close();
      }
    }
    assertThat(Context.current()).isEqualTo(initial);
    LoggingEvent log = logs.assertContains("Context in storage not the expected context");
    assertThat(log.getLevel()).isEqualTo(Level.DEBUG);
  }

  @Test
  void withValues() {
    Context context1 = Context.current().with(ANIMAL, "cat");
    assertThat(context1.get(ANIMAL)).isEqualTo("cat");

    Context context2 = context1.with(BAG, 100);
    // Old unaffected
    assertThat(context1.get(ANIMAL)).isEqualTo("cat");
    assertThat(context1.get(BAG)).isNull();

    assertThat(context2.get(ANIMAL)).isEqualTo("cat");
    assertThat(context2.get(BAG)).isEqualTo(100);

    Context context3 = context2.with(ANIMAL, "dog");
    // Old unaffected
    assertThat(context2.get(ANIMAL)).isEqualTo("cat");
    assertThat(context2.get(BAG)).isEqualTo(100);

    assertThat(context3.get(ANIMAL)).isEqualTo("dog");
    assertThat(context3.get(BAG)).isEqualTo(100);

    Context context4 = context3.with(BAG, null);
    // Old unaffected
    assertThat(context3.get(ANIMAL)).isEqualTo("dog");
    assertThat(context3.get(BAG)).isEqualTo(100);

    assertThat(context4.get(ANIMAL)).isEqualTo("dog");
    assertThat(context4.get(BAG)).isNull();

    Context context5 = context4.with(ANIMAL, "dog");
    assertThat(context5.get(ANIMAL)).isEqualTo("dog");
    assertThat(context5).isSameAs(context4);

    String dog = new String("dog");
    assertThat(dog).isEqualTo("dog");
    assertThat(dog).isNotSameAs("dog");
    Context context6 = context5.with(ANIMAL, dog);
    assertThat(context6.get(ANIMAL)).isEqualTo("dog");
    // We reuse context object when values match by reference, not value.
    assertThat(context6).isNotSameAs(context5);
  }

  @Test
  void wrapRunnable() {
    AtomicReference<String> value = new AtomicReference<>();
    Runnable callback = () -> value.set(Context.current().get(ANIMAL));

    callback.run();
    assertThat(value).hasValue(null);

    CAT.wrap(callback).run();
    assertThat(value).hasValue("cat");

    callback.run();
    assertThat(value).hasValue(null);
  }

  @Test
  void wrapCallable() throws Exception {
    AtomicReference<String> value = new AtomicReference<>();
    Callable<String> callback =
        () -> {
          value.set(Context.current().get(ANIMAL));
          return "foo";
        };

    assertThat(callback.call()).isEqualTo("foo");
    assertThat(value).hasValue(null);

    assertThat(CAT.wrap(callback).call()).isEqualTo("foo");
    assertThat(value).hasValue("cat");

    assertThat(callback.call()).isEqualTo("foo");
    assertThat(value).hasValue(null);
  }

  @Test
  void wrapExecutor() {
    AtomicReference<String> value = new AtomicReference<>();
    Executor executor = MoreExecutors.directExecutor();
    Runnable callback = () -> value.set(Context.current().get(ANIMAL));

    executor.execute(callback);
    assertThat(value).hasValue(null);

    CAT.wrap(executor).execute(callback);
    assertThat(value).hasValue("cat");

    executor.execute(callback);
    assertThat(value).hasValue(null);
  }

  @Nested
  @TestInstance(Lifecycle.PER_CLASS)
  class WrapExecutorService {

    protected ScheduledExecutorService executor;
    protected ExecutorService wrapped;
    protected AtomicReference<String> value;

    @BeforeAll
    void initExecutor() {
      executor = Executors.newSingleThreadScheduledExecutor();
      wrapped = CAT.wrap((ExecutorService) executor);
    }

    @AfterAll
    void stopExecutor() {
      executor.shutdown();
    }

    @BeforeEach
    void setUp() {
      value = new AtomicReference<>();
    }

    @Test
    void execute() {
      Runnable runnable = () -> value.set(Context.current().get(ANIMAL));
      wrapped.execute(runnable);
      await().untilAsserted(() -> assertThat(value).hasValue("cat"));
    }

    @Test
    void submitRunnable() {
      Runnable runnable = () -> value.set(Context.current().get(ANIMAL));
      Futures.getUnchecked(wrapped.submit(runnable));
      assertThat(value).hasValue("cat");
    }

    @Test
    void submitRunnableResult() {
      Runnable runnable = () -> value.set(Context.current().get(ANIMAL));
      assertThat(Futures.getUnchecked(wrapped.submit(runnable, "foo"))).isEqualTo("foo");
      assertThat(value).hasValue("cat");
    }

    @Test
    void submitCallable() {
      Callable<String> callable =
          () -> {
            value.set(Context.current().get(ANIMAL));
            return "foo";
          };
      assertThat(Futures.getUnchecked(wrapped.submit(callable))).isEqualTo("foo");
      assertThat(value).hasValue("cat");
    }

    @Test
    void invokeAll() throws Exception {
      AtomicReference<String> value1 = new AtomicReference<>();
      AtomicReference<String> value2 = new AtomicReference<>();
      Callable<String> callable1 =
          () -> {
            value1.set(Context.current().get(ANIMAL));
            return "foo";
          };
      Callable<String> callable2 =
          () -> {
            value2.set(Context.current().get(ANIMAL));
            return "bar";
          };
      List<Future<String>> futures = wrapped.invokeAll(Arrays.asList(callable1, callable2));
      assertThat(futures.get(0).get()).isEqualTo("foo");
      assertThat(futures.get(1).get()).isEqualTo("bar");
      assertThat(value1).hasValue("cat");
      assertThat(value2).hasValue("cat");
    }

    @Test
    void invokeAllTimeout() throws Exception {
      AtomicReference<String> value1 = new AtomicReference<>();
      AtomicReference<String> value2 = new AtomicReference<>();
      Callable<String> callable1 =
          () -> {
            value1.set(Context.current().get(ANIMAL));
            return "foo";
          };
      Callable<String> callable2 =
          () -> {
            value2.set(Context.current().get(ANIMAL));
            return "bar";
          };
      List<Future<String>> futures =
          wrapped.invokeAll(Arrays.asList(callable1, callable2), 10, TimeUnit.SECONDS);
      assertThat(futures.get(0).get()).isEqualTo("foo");
      assertThat(futures.get(1).get()).isEqualTo("bar");
      assertThat(value1).hasValue("cat");
      assertThat(value2).hasValue("cat");
    }

    @Test
    void invokeAny() throws Exception {
      AtomicReference<String> value1 = new AtomicReference<>();
      AtomicReference<String> value2 = new AtomicReference<>();
      Callable<String> callable1 =
          () -> {
            value1.set(Context.current().get(ANIMAL));
            throw new IllegalStateException("callable2 wins");
          };
      Callable<String> callable2 =
          () -> {
            value2.set(Context.current().get(ANIMAL));
            return "bar";
          };
      assertThat(wrapped.invokeAny(Arrays.asList(callable1, callable2))).isEqualTo("bar");
      assertThat(value1).hasValue("cat");
      assertThat(value2).hasValue("cat");
    }

    @Test
    void invokeAnyTimeout() throws Exception {
      AtomicReference<String> value1 = new AtomicReference<>();
      AtomicReference<String> value2 = new AtomicReference<>();
      Callable<String> callable1 =
          () -> {
            value1.set(Context.current().get(ANIMAL));
            throw new IllegalStateException("callable2 wins");
          };
      Callable<String> callable2 =
          () -> {
            value2.set(Context.current().get(ANIMAL));
            return "bar";
          };
      assertThat(wrapped.invokeAny(Arrays.asList(callable1, callable2), 10, TimeUnit.SECONDS))
          .isEqualTo("bar");
      assertThat(value1).hasValue("cat");
      assertThat(value2).hasValue("cat");
    }
  }

  @Test
  void keyToString() {
    assertThat(ANIMAL.toString()).isEqualTo("animal");
  }

  @Test
  void attachSameContext() {
    Context context = Context.current().with(ANIMAL, "cat");
    try (Scope scope1 = context.makeCurrent()) {
      assertThat(scope1).isNotSameAs(Scope.noop());
      try (Scope scope2 = context.makeCurrent()) {
        assertThat(scope2).isSameAs(Scope.noop());
      }
    }
  }

  // We test real context-related above but should test cleanup gets delegated, which is best with
  // a mock.
  @Nested
  @TestInstance(Lifecycle.PER_CLASS)
  class DelegatesToExecutorService {

    @Mock private ExecutorService executor;

    @Test
    void delegatesCleanupMethods() throws Exception {
      ExecutorService wrapped = CAT.wrap(executor);
      wrapped.shutdown();
      verify(executor).shutdown();
      verifyNoMoreInteractions(executor);
      wrapped.shutdownNow();
      verify(executor).shutdownNow();
      verifyNoMoreInteractions(executor);
      when(executor.isShutdown()).thenReturn(true);
      assertThat(wrapped.isShutdown()).isTrue();
      verify(executor).isShutdown();
      verifyNoMoreInteractions(executor);
      when(wrapped.isTerminated()).thenReturn(true);
      assertThat(wrapped.isTerminated()).isTrue();
      verify(executor).isTerminated();
      verifyNoMoreInteractions(executor);
      when(executor.awaitTermination(anyLong(), any())).thenReturn(true);
      assertThat(wrapped.awaitTermination(1, TimeUnit.SECONDS)).isTrue();
      verify(executor).awaitTermination(1, TimeUnit.SECONDS);
      verifyNoMoreInteractions(executor);
    }
  }

  @Nested
  @TestInstance(Lifecycle.PER_CLASS)
  class WrapScheduledExecutorService extends WrapExecutorService {

    private ScheduledExecutorService wrapScheduled;

    @BeforeEach
    void wrapScheduled() {
      wrapScheduled = CAT.wrap(executor);
    }

    @Test
    void scheduleRunnable() throws Exception {
      Runnable runnable = () -> value.set(Context.current().get(ANIMAL));
      wrapScheduled.schedule(runnable, 0, TimeUnit.SECONDS).get();
      assertThat(value).hasValue("cat");
    }

    @Test
    void scheduleCallable() throws Exception {
      Callable<String> callable =
          () -> {
            value.set(Context.current().get(ANIMAL));
            return "foo";
          };
      assertThat(wrapScheduled.schedule(callable, 0, TimeUnit.SECONDS).get()).isEqualTo("foo");
      assertThat(value).hasValue("cat");
    }

    @Test
    void scheduleAtFixedRate() {
      Runnable runnable = () -> value.set(Context.current().get(ANIMAL));
      ScheduledFuture<?> future =
          wrapScheduled.scheduleAtFixedRate(runnable, 0, 10, TimeUnit.SECONDS);
      await().untilAsserted(() -> assertThat(value).hasValue("cat"));
      future.cancel(true);
    }

    @Test
    void scheduleWithFixedDelay() {
      Runnable runnable = () -> value.set(Context.current().get(ANIMAL));
      ScheduledFuture<?> future =
          wrapScheduled.scheduleWithFixedDelay(runnable, 0, 10, TimeUnit.SECONDS);
      await().untilAsserted(() -> assertThat(value).hasValue("cat"));
      future.cancel(true);
    }
  }

  @Test
  void emptyContext() {
    assertThat(Context.root().get(new HashCollidingKey())).isEqualTo(null);
  }

  @Test
  void string() {
    assertThat(Context.root()).hasToString("{}");
    assertThat(Context.root().with(ANIMAL, "cat")).hasToString("{animal=cat}");
    assertThat(Context.root().with(ANIMAL, "cat").with(BAG, 10))
        .hasToString("{animal=cat, bag=10}");
  }

  @Test
  void hashcodeCollidingKeys() {
    Context context = Context.root();
    HashCollidingKey cheese = new HashCollidingKey();
    HashCollidingKey wine = new HashCollidingKey();

    Context twoKeys = context.with(cheese, "whiz").with(wine, "boone's farm");

    assertThat(twoKeys.get(wine)).isEqualTo("boone's farm");
    assertThat(twoKeys.get(cheese)).isEqualTo("whiz");
  }

  private static class HashCollidingKey implements ContextKey<String> {
    @Override
    public int hashCode() {
      return 1;
    }
  }
}
