/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.context;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
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
  void keyEqualityIsInstanceCheck() {
    Context context = Context.current().with(ContextKey.named("animal"), "cat");
    assertNull(context.get(ContextKey.named("animal"))); // yup
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
  void closingScopeWhenNotActiveIsNoopAndLogged() {
    Context initial = Context.current();
    Context context = initial.with(ANIMAL, "cat");
    try (Scope scope = context.makeCurrent()) {
      Context context2 = context.with(ANIMAL, "dog");
      try (Scope ignored = context2.makeCurrent()) {
        assertThat(Context.current().get(ANIMAL)).isEqualTo("dog");
        scope.close();
        assertThat(Context.current().get(ANIMAL)).isEqualTo("dog");
      }
    }
    assertThat(Context.current()).isEqualTo(initial);
    LoggingEvent log =
        logs.assertContains("Trying to close scope which does not represent current context");
    assertThat(log.getLevel()).isEqualTo(Level.DEBUG);
  }

  @SuppressWarnings("MustBeClosedChecker")
  @Test
  void closeScopeIsIdempotent() {
    Context initial = Context.current();
    Context context1 = Context.root().with(ANIMAL, "cat");
    Scope scope1 = context1.makeCurrent();
    Context context2 = context1.with(ANIMAL, "dog");
    Scope scope2 = context2.makeCurrent();

    scope2.close();
    assertThat(Context.current()).isEqualTo(context1);

    scope1.close();
    assertThat(Context.current()).isEqualTo(initial);

    scope2.close();
    assertThat(Context.current()).isEqualTo(initial);
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
    assertThat(dog).isEqualTo("dog").isNotSameAs("dog");
    Context context6 = context5.with(ANIMAL, dog);
    assertThat(context6.get(ANIMAL)).isEqualTo("dog");
    // We reuse the context object when values match by reference, not value.
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
  void wrapFunction() {
    AtomicReference<String> value = new AtomicReference<>();
    Function<String, String> callback =
        a -> {
          value.set(Context.current().get(ANIMAL));
          return "foo";
        };

    assertThat(callback.apply("bar")).isEqualTo("foo");
    assertThat(value).hasValue(null);

    assertThat(CAT.wrapFunction(callback).apply("bar")).isEqualTo("foo");
    assertThat(value).hasValue("cat");

    assertThat(callback.apply("bar")).isEqualTo("foo");
    assertThat(value).hasValue(null);
  }

  @Test
  void wrapBiFunction() {
    AtomicReference<String> value = new AtomicReference<>();
    BiFunction<String, String, String> callback =
        (a, b) -> {
          value.set(Context.current().get(ANIMAL));
          return "foo";
        };

    assertThat(callback.apply("bar", "baz")).isEqualTo("foo");
    assertThat(value).hasValue(null);

    assertThat(CAT.wrapFunction(callback).apply("bar", "baz")).isEqualTo("foo");
    assertThat(value).hasValue("cat");

    assertThat(callback.apply("bar", "baz")).isEqualTo("foo");
    assertThat(value).hasValue(null);
  }

  @Test
  void wrapConsumer() {
    AtomicReference<String> value = new AtomicReference<>();
    AtomicBoolean consumed = new AtomicBoolean();
    Consumer<String> callback =
        a -> {
          value.set(Context.current().get(ANIMAL));
          consumed.set(true);
        };

    callback.accept("bar");
    assertThat(consumed).isTrue();
    assertThat(value).hasValue(null);

    consumed.set(false);
    CAT.wrapConsumer(callback).accept("bar");
    assertThat(consumed).isTrue();
    assertThat(value).hasValue("cat");

    consumed.set(false);
    callback.accept("bar");
    assertThat(consumed).isTrue();
    assertThat(value).hasValue(null);
  }

  @Test
  void wrapBiConsumer() {
    AtomicReference<String> value = new AtomicReference<>();
    AtomicBoolean consumed = new AtomicBoolean();
    BiConsumer<String, String> callback =
        (a, b) -> {
          value.set(Context.current().get(ANIMAL));
          consumed.set(true);
        };

    callback.accept("bar", "baz");
    assertThat(consumed).isTrue();
    assertThat(value).hasValue(null);

    consumed.set(false);
    CAT.wrapConsumer(callback).accept("bar", "baz");
    assertThat(consumed).isTrue();
    assertThat(value).hasValue("cat");

    consumed.set(false);
    callback.accept("bar", "baz");
    assertThat(consumed).isTrue();
    assertThat(value).hasValue(null);
  }

  @Test
  void wrapSupplier() {
    AtomicReference<String> value = new AtomicReference<>();
    Supplier<String> callback =
        () -> {
          value.set(Context.current().get(ANIMAL));
          return "foo";
        };

    assertThat(callback.get()).isEqualTo("foo");
    assertThat(value).hasValue(null);

    assertThat(CAT.wrapSupplier(callback).get()).isEqualTo("foo");
    assertThat(value).hasValue("cat");

    assertThat(callback.get()).isEqualTo("foo");
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

    try (Scope ignored = CAT.makeCurrent()) {
      Context.taskWrapping(executor).execute(callback);
      assertThat(value).hasValue("cat");
    }
  }

  @Test
  void wrapExecutorService() {
    // given
    ExecutorService executorService = Executors.newSingleThreadExecutor();

    // when
    ExecutorService firstWrap = CAT.wrap(executorService);
    ExecutorService secondWrap = CAT.wrap(firstWrap);

    // then
    assertThat(firstWrap).isInstanceOf(ContextExecutorService.class);
    assertThat(((ContextExecutorService) firstWrap).context()).isEqualTo(CAT);
    assertThat(((ContextExecutorService) firstWrap).delegate()).isEqualTo(executorService);
    assertThat(secondWrap).isInstanceOf(ContextExecutorService.class);
    assertThat(((ContextExecutorService) secondWrap).context()).isEqualTo(CAT);
    assertThat(((ContextExecutorService) secondWrap).delegate()).isEqualTo(executorService);
  }

  @Nested
  @TestInstance(Lifecycle.PER_CLASS)
  class WrapExecutorService {

    protected ExecutorService executor;
    protected ExecutorService wrapped;
    protected AtomicReference<String> value;

    protected ExecutorService wrap(ExecutorService executorService) {
      return CAT.wrap(executorService);
    }

    @BeforeAll
    void initExecutor() {
      executor = Executors.newSingleThreadScheduledExecutor();
      wrapped = wrap(executor);
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
  void wrapScheduledExecutorService() {
    // given
    ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    // when
    ScheduledExecutorService firstWrap = CAT.wrap(executorService);
    ScheduledExecutorService secondWrap = CAT.wrap(firstWrap);

    // then
    assertThat(firstWrap).isInstanceOf(ContextScheduledExecutorService.class);
    assertThat(((ContextScheduledExecutorService) firstWrap).context()).isEqualTo(CAT);
    assertThat(((ContextScheduledExecutorService) firstWrap).delegate()).isEqualTo(executorService);
    assertThat(secondWrap).isInstanceOf(ContextScheduledExecutorService.class);
    assertThat(((ContextScheduledExecutorService) secondWrap).context()).isEqualTo(CAT);
    assertThat(((ContextScheduledExecutorService) secondWrap).delegate())
        .isEqualTo(executorService);
  }

  @Nested
  @TestInstance(Lifecycle.PER_CLASS)
  class WrapScheduledExecutorService {

    protected ScheduledExecutorService executor;
    protected ScheduledExecutorService wrapped;
    protected AtomicReference<String> value;

    protected ScheduledExecutorService wrap(ScheduledExecutorService executorService) {
      return CAT.wrap(executorService);
    }

    @BeforeAll
    void initExecutor() {
      executor = Executors.newSingleThreadScheduledExecutor();
      wrapped = wrap(executor);
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

    @Test
    void scheduleRunnable() {
      Runnable runnable = () -> value.set(Context.current().get(ANIMAL));
      assertThat(Futures.getUnchecked(wrapped.schedule(runnable, 1L, TimeUnit.MILLISECONDS)))
          .isNull();
      assertThat(value).hasValue("cat");
    }

    @Test
    void scheduleCallable() {
      Callable<String> callable =
          () -> {
            value.set(Context.current().get(ANIMAL));
            return "foo";
          };
      assertThat(Futures.getUnchecked(wrapped.schedule(callable, 1L, TimeUnit.MILLISECONDS)))
          .isEqualTo("foo");
      assertThat(value).hasValue("cat");
    }

    @Test
    void scheduleAtFixedRate() {
      LongAdder longAdder = new LongAdder();
      Runnable runnable = longAdder::increment;
      Future<?> future = wrapped.scheduleAtFixedRate(runnable, 1L, 2L, TimeUnit.NANOSECONDS);
      assertThat(future).isNotNull();
      await()
          .await()
          .untilAsserted(
              () -> {
                if (!future.isCancelled()) {
                  future.cancel(true);
                }
                assertThat(longAdder.intValue()).isGreaterThan(1);
              });
      assertThat(longAdder.intValue()).isGreaterThan(1);
    }

    @Test
    void scheduleWithFixedDelay() {
      LongAdder longAdder = new LongAdder();
      Runnable runnable = longAdder::increment;
      Future<?> future = wrapped.scheduleWithFixedDelay(runnable, 1L, 2L, TimeUnit.NANOSECONDS);
      assertThat(future).isNotNull();
      await()
          .await()
          .untilAsserted(
              () -> {
                if (!future.isCancelled()) {
                  future.cancel(true);
                }
                assertThat(longAdder.intValue()).isGreaterThan(1);
              });
    }
  }

  @Nested
  @TestInstance(Lifecycle.PER_CLASS)
  class CurrentContextWrappingExecutorService extends WrapExecutorService {
    @Override
    protected ExecutorService wrap(ExecutorService executorService) {
      return Context.taskWrapping(executorService);
    }

    private Scope scope;

    @BeforeEach
    // Closed in AfterEach
    @SuppressWarnings("MustBeClosedChecker")
    void makeCurrent() {
      scope = CAT.makeCurrent();
    }

    @AfterEach
    void close() {
      scope.close();
      scope = null;
    }
  }

  @Nested
  @TestInstance(Lifecycle.PER_CLASS)
  class CurrentContextWrappingScheduledExecutorService extends WrapScheduledExecutorService {

    @Override
    protected ScheduledExecutorService wrap(ScheduledExecutorService executorService) {
      return Context.taskWrapping(executorService);
    }

    private Scope scope;

    @BeforeEach
    // Closed in AfterEach
    @SuppressWarnings("MustBeClosedChecker")
    void makeCurrent() {
      scope = CAT.makeCurrent();
    }

    @AfterEach
    void close() {
      scope.close();
      scope = null;
    }
  }

  @Test
  void keyToString() {
    assertThat(ANIMAL).hasToString("animal");
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
      doNothing().when(executor).shutdown();
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

  // We test real context-related above but should test cleanup gets delegated, which is best with
  // a mock.
  @Nested
  @TestInstance(Lifecycle.PER_CLASS)
  @SuppressWarnings("MockitoDoSetup")
  class DelegatesToScheduledExecutorService {

    @Mock private ScheduledExecutorService executor;
    @Mock private ScheduledFuture<?> scheduledFuture;

    @Test
    void delegatesCleanupMethods() throws Exception {
      ScheduledExecutorService wrapped = CAT.wrap(executor);

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
      assertThat(wrapped.awaitTermination(1L, TimeUnit.SECONDS)).isTrue();
      verify(executor).awaitTermination(1L, TimeUnit.SECONDS);
      verifyNoMoreInteractions(executor);

      doReturn(scheduledFuture)
          .when(executor)
          .schedule(any(Runnable.class), anyLong(), any(TimeUnit.class));
      assertThat((Future<?>) wrapped.schedule(() -> {}, 1L, TimeUnit.SECONDS))
          .isSameAs(scheduledFuture);
      verify(executor).schedule(any(Runnable.class), anyLong(), any(TimeUnit.class));
      verifyNoMoreInteractions(executor);

      doReturn(scheduledFuture)
          .when(executor)
          .scheduleAtFixedRate(any(Runnable.class), anyLong(), anyLong(), any(TimeUnit.class));
      assertThat((Future<?>) wrapped.scheduleAtFixedRate(() -> {}, 1L, 1L, TimeUnit.SECONDS))
          .isSameAs(scheduledFuture);
      verify(executor)
          .scheduleAtFixedRate(any(Runnable.class), anyLong(), anyLong(), any(TimeUnit.class));
      verifyNoMoreInteractions(executor);

      doReturn(scheduledFuture)
          .when(executor)
          .scheduleWithFixedDelay(any(Runnable.class), anyLong(), anyLong(), any(TimeUnit.class));
      assertThat((Future<?>) wrapped.scheduleWithFixedDelay(() -> {}, 1L, 1L, TimeUnit.SECONDS))
          .isSameAs(scheduledFuture);
      verify(executor)
          .scheduleWithFixedDelay(any(Runnable.class), anyLong(), anyLong(), any(TimeUnit.class));
      verifyNoMoreInteractions(executor);
    }
  }

  @Test
  void emptyContext() {
    assertThat(Context.root().get(new HashCollidingKey())).isNull();
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

  @Test
  void doNotWrapExecutorService() {
    ExecutorService executor = mock(CurrentContextExecutorService.class);
    ExecutorService wrapped = Context.taskWrapping(executor);
    assertThat(wrapped).isSameAs(executor);
  }

  @Test
  void doNotWrapScheduledExecutorService() {
    ScheduledExecutorService executor = mock(CurrentContextScheduledExecutorService.class);
    ScheduledExecutorService wrapped = Context.taskWrapping(executor);
    assertThat(wrapped).isSameAs(executor);
  }

  @SuppressWarnings("HashCodeToString")
  private static class HashCollidingKey implements ContextKey<String> {
    @Override
    public int hashCode() {
      return 1;
    }
  }
}
