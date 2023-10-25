/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.sender.okhttp.internal;

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.opentelemetry.exporter.internal.InstrumentationUtil;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

abstract class OkHttpSenderTest<T> {

  @BeforeEach
  void setUp() {
    OkHttpUtil.propagateContextInDispatcher = true;
  }

  @AfterEach
  void tearDown() {
    OkHttpUtil.propagateContextInDispatcher = false;
  }

  @Test
  void testSuppressInstrumentation() throws InterruptedException {
    CountDownLatch lock = new CountDownLatch(1);
    AtomicBoolean suppressInstrumentation = new AtomicBoolean(false);

    Runnable onSuccess = Assertions::fail;
    Runnable onFailure =
        () -> {
          suppressInstrumentation.set(InstrumentationUtil.shouldSuppressInstrumentation());
          lock.countDown();
        };

    send(getSender(), onSuccess, onFailure);

    lock.await();

    assertTrue(suppressInstrumentation.get());
  }

  abstract void send(T sender, Runnable onSuccess, Runnable onFailure);

  private T getSender() {
    return createSender("https://none");
  }

  abstract T createSender(String endpoint);
}
