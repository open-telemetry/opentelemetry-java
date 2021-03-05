/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.google.common.util.concurrent.Uninterruptibles;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class CompletableResultCodeTest {

  @Test
  void ofSuccess() {
    assertThat(CompletableResultCode.ofSuccess().isSuccess()).isTrue();
  }

  @Test
  void ofFailure() {
    assertThat(CompletableResultCode.ofFailure().isSuccess()).isFalse();
  }

  @Test
  void succeed() throws InterruptedException {
    CompletableResultCode resultCode = new CompletableResultCode();

    CountDownLatch completions = new CountDownLatch(1);

    new Thread(resultCode::succeed).start();

    resultCode.whenComplete(completions::countDown);

    completions.await(3, TimeUnit.SECONDS);

    assertThat(resultCode.isSuccess()).isTrue();
  }

  @Test
  void fail() throws InterruptedException {
    CompletableResultCode resultCode = new CompletableResultCode();

    CountDownLatch completions = new CountDownLatch(1);

    new Thread(resultCode::fail).start();

    resultCode.whenComplete(completions::countDown);

    completions.await(3, TimeUnit.SECONDS);

    assertThat(resultCode.isSuccess()).isFalse();
  }

  @Test
  void whenDoublyCompleteSuccessfully() throws InterruptedException {
    CompletableResultCode resultCode = new CompletableResultCode();

    CountDownLatch completions = new CountDownLatch(2);

    new Thread(resultCode::succeed).start();

    resultCode.whenComplete(completions::countDown).whenComplete(completions::countDown);

    completions.await(3, TimeUnit.SECONDS);

    assertThat(resultCode.isSuccess()).isTrue();
  }

  @Test
  void whenDoublyNestedComplete() throws InterruptedException {
    CompletableResultCode resultCode = new CompletableResultCode();

    CountDownLatch completions = new CountDownLatch(2);

    new Thread(resultCode::succeed).start();

    resultCode.whenComplete(
        () -> {
          completions.countDown();

          resultCode.whenComplete(completions::countDown);
        });

    completions.await(3, TimeUnit.SECONDS);

    assertThat(resultCode.isSuccess()).isTrue();
  }

  @Test
  void whenSuccessThenFailure() throws InterruptedException {
    CompletableResultCode resultCode = new CompletableResultCode();

    CountDownLatch completions = new CountDownLatch(1);

    new Thread(() -> resultCode.succeed().fail()).start();

    resultCode.whenComplete(completions::countDown);

    completions.await(3, TimeUnit.SECONDS);

    assertThat(resultCode.isSuccess()).isTrue();
  }

  @Test
  void isDone() {
    CompletableResultCode result = new CompletableResultCode();
    assertThat(result.isDone()).isFalse();
    result.fail();
    assertThat(result.isDone()).isTrue();
  }

  @Test
  void ofAll() {
    CompletableResultCode result1 = new CompletableResultCode();
    CompletableResultCode result2 = new CompletableResultCode();
    CompletableResultCode result3 = new CompletableResultCode();

    CompletableResultCode all =
        CompletableResultCode.ofAll(Arrays.asList(result1, result2, result3));
    assertThat(all.isDone()).isFalse();
    result1.succeed();
    assertThat(all.isDone()).isFalse();
    result2.succeed();
    assertThat(all.isDone()).isFalse();
    result3.succeed();
    assertThat(all.isDone()).isTrue();
    assertThat(all.isSuccess()).isTrue();
  }

  @Test
  void ofAllWithFailure() {
    assertThat(
            CompletableResultCode.ofAll(
                    Arrays.asList(
                        CompletableResultCode.ofSuccess(),
                        CompletableResultCode.ofFailure(),
                        CompletableResultCode.ofSuccess()))
                .isSuccess())
        .isFalse();
  }

  @Test
  void join() {
    CompletableResultCode result = new CompletableResultCode();
    new Thread(
            () -> {
              Uninterruptibles.sleepUninterruptibly(Duration.ofMillis(50));
              result.succeed();
            })
        .start();
    assertThat(result.join(10, TimeUnit.SECONDS).isSuccess()).isTrue();
    // Already completed, synchronous call.
    assertThat(result.join(0, TimeUnit.NANOSECONDS).isSuccess()).isTrue();
  }

  @Test
  void joinTimesOut() {
    CompletableResultCode result = new CompletableResultCode();
    assertThat(result.join(1, TimeUnit.MILLISECONDS).isSuccess()).isFalse();
    assertThat(result.isDone()).isFalse();
  }

  @Test
  void joinInterrupted() {
    CompletableResultCode result = new CompletableResultCode();
    AtomicReference<Boolean> interrupted = new AtomicReference<>();
    Thread thread =
        new Thread(
            () -> {
              result.join(10, TimeUnit.SECONDS);
              interrupted.set(Thread.currentThread().isInterrupted());
            });
    thread.start();
    thread.interrupt();
    // Different thread so wait a bit for result to be propagated.
    await().untilAsserted(() -> assertThat(interrupted).hasValue(true));
    assertThat(result.isSuccess()).isFalse();
    assertThat(result.isDone()).isFalse();
  }
}
