/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.context;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;
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
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@SuppressWarnings("ClassCanBeStatic")
class ContextTest {

  private static final ContextKey<String> ANIMAL = ContextKey.named("animal");
  private static final ContextKey<Object> BAG = ContextKey.named("bag");
  private static final ContextKey<String> FOOD = ContextKey.named("food");
  private static final ContextKey<Integer> COOKIES = ContextKey.named("cookies");

  private static final Context CAT = Context.current().withValues(ANIMAL, "cat");

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
    Context context = Context.current().withValues(ANIMAL, "cat");
    assertThat(Context.current().getValue(ANIMAL)).isNull();
    try (Scope ignored = context.makeCurrent()) {
      assertThat(Context.current().getValue(ANIMAL)).isEqualTo("cat");

      try (Scope ignored2 = Context.root().makeCurrent()) {
        assertThat(Context.current().getValue(ANIMAL)).isNull();
      }

      assertThat(Context.current().getValue(ANIMAL)).isEqualTo("cat");
    }
    assertThat(Context.current().getValue(ANIMAL)).isNull();
  }

  @Test
  void attachSameTwice() {
    Context context = Context.current().withValues(ANIMAL, "cat");
    assertThat(Context.current().getValue(ANIMAL)).isNull();
    try (Scope ignored = context.makeCurrent()) {
      assertThat(Context.current().getValue(ANIMAL)).isEqualTo("cat");

      try (Scope ignored2 = context.makeCurrent()) {
        assertThat(Context.current().getValue(ANIMAL)).isEqualTo("cat");
      }

      assertThat(Context.current().getValue(ANIMAL)).isEqualTo("cat");
    }
    assertThat(Context.current().getValue(ANIMAL)).isNull();
  }

  @Test
  void newThreadStartsWithRoot() throws Exception {
    Context context = Context.current().withValues(ANIMAL, "cat");
    try (Scope ignored = context.makeCurrent()) {
      assertThat(Context.current().getValue(ANIMAL)).isEqualTo("cat");
      AtomicReference<Context> current = new AtomicReference<>();
      Thread thread = new Thread(() -> current.set(Context.current()));
      thread.start();
      thread.join();
      assertThat(current.get()).isEqualTo(Context.root());
    }
  }

  @Test
  public void closingScopeWhenNotActiveIsLogged() {
    final AtomicReference<LogRecord> logRef = new AtomicReference<>();
    Handler handler =
        new Handler() {
          @Override
          public void publish(LogRecord record) {
            logRef.set(record);
          }

          @Override
          public void flush() {}

          @Override
          public void close() {}
        };
    Logger logger = Logger.getLogger(ThreadLocalContextStorage.class.getName());
    Level level = logger.getLevel();
    logger.setLevel(Level.ALL);
    try {
      logger.addHandler(handler);
      Context initial = Context.current();
      Context context = initial.withValues(ANIMAL, "cat");
      try (Scope scope = context.makeCurrent()) {
        Context context2 = context.withValues(ANIMAL, "dog");
        try (Scope ignored = context2.makeCurrent()) {
          assertThat(Context.current().getValue(ANIMAL)).isEqualTo("dog");
          scope.close();
        }
      }
      assertThat(Context.current()).isEqualTo(initial);
      assertThat(logRef.get()).isNotNull();
      assertThat(logRef.get().getMessage()).contains("Context in storage not the expected context");
    } finally {
      logger.removeHandler(handler);
      logger.setLevel(level);
    }
  }

  @Test
  void withValues() {
    Context context1 = Context.current().withValues(ANIMAL, "cat");
    assertThat(context1.getValue(ANIMAL)).isEqualTo("cat");

    Context context2 = context1.withValues(BAG, 100);
    // Old unaffected
    assertThat(context1.getValue(ANIMAL)).isEqualTo("cat");
    assertThat(context1.getValue(BAG)).isNull();

    assertThat(context2.getValue(ANIMAL)).isEqualTo("cat");
    assertThat(context2.getValue(BAG)).isEqualTo(100);

    Context context3 = context2.withValues(ANIMAL, "dog");
    // Old unaffected
    assertThat(context2.getValue(ANIMAL)).isEqualTo("cat");
    assertThat(context2.getValue(BAG)).isEqualTo(100);

    assertThat(context3.getValue(ANIMAL)).isEqualTo("dog");
    assertThat(context3.getValue(BAG)).isEqualTo(100);

    Context context4 = context3.withValues(BAG, null);
    // Old unaffected
    assertThat(context3.getValue(ANIMAL)).isEqualTo("dog");
    assertThat(context3.getValue(BAG)).isEqualTo(100);

    assertThat(context4.getValue(ANIMAL)).isEqualTo("dog");
    assertThat(context4.getValue(BAG)).isNull();
  }

  @Test
  void withTwoValues() {
    Context context = Context.current().withValues(ANIMAL, "cat", FOOD, "hot dog");
    assertThat(context.getValue(ANIMAL)).isEqualTo("cat");
    assertThat(context.getValue(FOOD)).isEqualTo("hot dog");
  }

  @Test
  void withThreeValues() {
    Context context = Context.current().withValues(ANIMAL, "cat", FOOD, "hot dog", COOKIES, 100);
    assertThat(context.getValue(ANIMAL)).isEqualTo("cat");
    assertThat(context.getValue(FOOD)).isEqualTo("hot dog");
    assertThat(context.getValue(COOKIES)).isEqualTo(100);
  }

  @Test
  void withFourValues() {
    Context context =
        Context.current().withValues(ANIMAL, "cat", FOOD, "hot dog", COOKIES, 100, BAG, "prada");
    assertThat(context.getValue(ANIMAL)).isEqualTo("cat");
    assertThat(context.getValue(FOOD)).isEqualTo("hot dog");
    assertThat(context.getValue(COOKIES)).isEqualTo(100);
    assertThat(context.getValue(BAG)).isEqualTo("prada");
  }

  @Test
  void wrapRunnable() {
    AtomicReference<String> value = new AtomicReference<>();
    Runnable callback = () -> value.set(Context.current().getValue(ANIMAL));

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
          value.set(Context.current().getValue(ANIMAL));
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
    Runnable callback = () -> value.set(Context.current().getValue(ANIMAL));

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
      Runnable runnable = () -> value.set(Context.current().getValue(ANIMAL));
      wrapped.execute(runnable);
      await().untilAsserted(() -> assertThat(value).hasValue("cat"));
    }

    @Test
    void submitRunnable() {
      Runnable runnable = () -> value.set(Context.current().getValue(ANIMAL));
      Futures.getUnchecked(wrapped.submit(runnable));
      assertThat(value).hasValue("cat");
    }

    @Test
    void submitRunnableResult() {
      Runnable runnable = () -> value.set(Context.current().getValue(ANIMAL));
      assertThat(Futures.getUnchecked(wrapped.submit(runnable, "foo"))).isEqualTo("foo");
      assertThat(value).hasValue("cat");
    }

    @Test
    void submitCallable() {
      Callable<String> callable =
          () -> {
            value.set(Context.current().getValue(ANIMAL));
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
            value1.set(Context.current().getValue(ANIMAL));
            return "foo";
          };
      Callable<String> callable2 =
          () -> {
            value2.set(Context.current().getValue(ANIMAL));
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
            value1.set(Context.current().getValue(ANIMAL));
            return "foo";
          };
      Callable<String> callable2 =
          () -> {
            value2.set(Context.current().getValue(ANIMAL));
            return "bar";
          };
      List<Future<String>> futures =
          wrapped.invokeAll(Arrays.asList(callable1, callable2), 1, TimeUnit.SECONDS);
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
            value1.set(Context.current().getValue(ANIMAL));
            throw new IllegalStateException("callable2 wins");
          };
      Callable<String> callable2 =
          () -> {
            value2.set(Context.current().getValue(ANIMAL));
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
            value1.set(Context.current().getValue(ANIMAL));
            throw new IllegalStateException("callable2 wins");
          };
      Callable<String> callable2 =
          () -> {
            value2.set(Context.current().getValue(ANIMAL));
            return "bar";
          };
      assertThat(wrapped.invokeAny(Arrays.asList(callable1, callable2), 1, TimeUnit.SECONDS))
          .isEqualTo("bar");
      assertThat(value1).hasValue("cat");
      assertThat(value2).hasValue("cat");
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
      Runnable runnable = () -> value.set(Context.current().getValue(ANIMAL));
      wrapScheduled.schedule(runnable, 0, TimeUnit.SECONDS).get();
      assertThat(value).hasValue("cat");
    }

    @Test
    void scheduleCallable() throws Exception {
      Callable<String> callable =
          () -> {
            value.set(Context.current().getValue(ANIMAL));
            return "foo";
          };
      assertThat(wrapScheduled.schedule(callable, 0, TimeUnit.SECONDS).get()).isEqualTo("foo");
      assertThat(value).hasValue("cat");
    }

    @Test
    void scheduleAtFixedRate() {
      Runnable runnable = () -> value.set(Context.current().getValue(ANIMAL));
      ScheduledFuture<?> future =
          wrapScheduled.scheduleAtFixedRate(runnable, 0, 10, TimeUnit.SECONDS);
      await().untilAsserted(() -> assertThat(value).hasValue("cat"));
      future.cancel(true);
    }

    @Test
    void scheduleWithFixedDelay() {
      Runnable runnable = () -> value.set(Context.current().getValue(ANIMAL));
      ScheduledFuture<?> future =
          wrapScheduled.scheduleWithFixedDelay(runnable, 0, 10, TimeUnit.SECONDS);
      await().untilAsserted(() -> assertThat(value).hasValue("cat"));
      future.cancel(true);
    }
  }
}
