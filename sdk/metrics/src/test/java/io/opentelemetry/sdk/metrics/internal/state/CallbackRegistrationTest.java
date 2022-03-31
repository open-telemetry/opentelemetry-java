/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.google.common.util.concurrent.AtomicDouble;
import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.ObservableDoubleMeasurement;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.InstrumentValueType;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@SuppressLogger(CallbackRegistration.class)
@ExtendWith(MockitoExtension.class)
class CallbackRegistrationTest {

  private static final InstrumentDescriptor LONG_INSTRUMENT =
      InstrumentDescriptor.create(
          "name",
          "description",
          "unit",
          InstrumentType.OBSERVABLE_COUNTER,
          InstrumentValueType.LONG);
  private static final InstrumentDescriptor DOUBLE_INSTRUMENT =
      InstrumentDescriptor.create(
          "name",
          "description",
          "unit",
          InstrumentType.OBSERVABLE_COUNTER,
          InstrumentValueType.LONG);

  @RegisterExtension
  LogCapturer logs = LogCapturer.create().captureForType(CallbackRegistration.class);

  @Mock private AsynchronousMetricStorage<?> storage1;
  @Mock private AsynchronousMetricStorage<?> storage2;

  @Test
  void invokeCallback_Double() {
    AtomicDouble counter = new AtomicDouble();
    Consumer<ObservableDoubleMeasurement> callback =
        measurement ->
            measurement.record(
                counter.addAndGet(1.1), Attributes.builder().put("key", "val").build());
    CallbackRegistration<?> callbackRegistration =
        CallbackRegistration.createDouble(
            DOUBLE_INSTRUMENT, callback, Arrays.asList(storage1, storage2));

    callbackRegistration.invokeCallback();

    assertThat(counter.get()).isEqualTo(1.1);
    verify(storage1).recordDouble(1.1, Attributes.builder().put("key", "val").build());
    verify(storage2).recordDouble(1.1, Attributes.builder().put("key", "val").build());
  }

  @Test
  void invokeCallback_Long() {
    AtomicLong counter = new AtomicLong();
    Consumer<ObservableDoubleMeasurement> callback =
        measurement ->
            measurement.record(
                counter.incrementAndGet(), Attributes.builder().put("key", "val").build());
    CallbackRegistration<?> callbackRegistration =
        CallbackRegistration.createDouble(
            LONG_INSTRUMENT, callback, Arrays.asList(storage1, storage2));

    callbackRegistration.invokeCallback();

    assertThat(counter.get()).isEqualTo(1);
    verify(storage1).recordDouble(1, Attributes.builder().put("key", "val").build());
    verify(storage2).recordDouble(1, Attributes.builder().put("key", "val").build());
  }

  @Test
  void invokeCallback_NoStorage() {
    AtomicLong counter = new AtomicLong();
    Consumer<ObservableDoubleMeasurement> callback =
        measurement ->
            measurement.record(
                counter.incrementAndGet(), Attributes.builder().put("key", "val").build());
    CallbackRegistration<?> callbackRegistration =
        CallbackRegistration.createDouble(LONG_INSTRUMENT, callback, Collections.emptyList());

    callbackRegistration.invokeCallback();

    assertThat(counter.get()).isEqualTo(0);
  }

  @Test
  void invokeCallback_ThrowsException() {
    Consumer<ObservableDoubleMeasurement> callback =
        unused -> {
          throw new RuntimeException("Error!");
        };
    CallbackRegistration<?> callbackRegistration =
        CallbackRegistration.createDouble(
            LONG_INSTRUMENT, callback, Arrays.asList(storage1, storage2));

    callbackRegistration.invokeCallback();

    verify(storage1, never()).recordDouble(anyDouble(), any());
    verify(storage2, never()).recordDouble(anyDouble(), any());
    logs.assertContains("An exception occurred invoking callback for instrument name");
  }

  @Test
  void invokeCallback_NoConcurrentCalls()
      throws ExecutionException, InterruptedException, TimeoutException {
    AtomicBoolean inProgress = new AtomicBoolean(false);
    // Callback records if not called concurrently
    Consumer<ObservableDoubleMeasurement> callback =
        measurement -> {
          if (inProgress.compareAndSet(false, true)) {
            measurement.record(1.1);
          }
          try {
            Thread.sleep(100);
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
          }
          inProgress.set(false);
        };
    CallbackRegistration<?> callbackRegistration =
        CallbackRegistration.createDouble(
            DOUBLE_INSTRUMENT, callback, Collections.singletonList(storage1));

    List<Future<?>> futures = new ArrayList<>();
    ExecutorService executorService = Executors.newFixedThreadPool(2);
    futures.add(executorService.submit(callbackRegistration::invokeCallback));
    futures.add(executorService.submit(callbackRegistration::invokeCallback));
    for (Future<?> future : futures) {
      future.get(10, TimeUnit.SECONDS);
    }

    verify(storage1, times(2)).recordDouble(1.1, Attributes.empty());
  }
}
