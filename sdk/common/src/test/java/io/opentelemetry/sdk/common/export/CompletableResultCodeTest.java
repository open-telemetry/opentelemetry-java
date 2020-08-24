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

package io.opentelemetry.sdk.common.export;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
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

    new Thread(
            new Runnable() {
              @Override
              public void run() {
                resultCode.succeed();
              }
            })
        .start();

    resultCode.whenComplete(
        new Runnable() {
          @Override
          public void run() {
            completions.countDown();
          }
        });

    completions.await(3, TimeUnit.SECONDS);

    assertThat(resultCode.isSuccess()).isTrue();
  }

  @Test
  void fail() throws InterruptedException {
    CompletableResultCode resultCode = new CompletableResultCode();

    CountDownLatch completions = new CountDownLatch(1);

    new Thread(
            new Runnable() {
              @Override
              public void run() {
                resultCode.fail();
              }
            })
        .start();

    resultCode.whenComplete(
        new Runnable() {
          @Override
          public void run() {
            completions.countDown();
          }
        });

    completions.await(3, TimeUnit.SECONDS);

    assertThat(resultCode.isSuccess()).isFalse();
  }

  @Test
  void whenDoublyCompleteSuccessfully() throws InterruptedException {
    CompletableResultCode resultCode = new CompletableResultCode();

    CountDownLatch completions = new CountDownLatch(2);

    new Thread(
            new Runnable() {
              @Override
              public void run() {
                resultCode.succeed();
              }
            })
        .start();

    resultCode
        .whenComplete(
            new Runnable() {
              @Override
              public void run() {
                completions.countDown();
              }
            })
        .whenComplete(
            new Runnable() {
              @Override
              public void run() {
                completions.countDown();
              }
            });

    completions.await(3, TimeUnit.SECONDS);

    assertThat(resultCode.isSuccess()).isTrue();
  }

  @Test
  void whenDoublyNestedComplete() throws InterruptedException {
    CompletableResultCode resultCode = new CompletableResultCode();

    CountDownLatch completions = new CountDownLatch(2);

    new Thread(
            new Runnable() {
              @Override
              public void run() {
                resultCode.succeed();
              }
            })
        .start();

    resultCode.whenComplete(
        new Runnable() {
          @Override
          public void run() {
            completions.countDown();

            resultCode.whenComplete(
                new Runnable() {
                  @Override
                  public void run() {
                    completions.countDown();
                  }
                });
          }
        });

    completions.await(3, TimeUnit.SECONDS);

    assertThat(resultCode.isSuccess()).isTrue();
  }

  @Test
  void whenSuccessThenFailure() throws InterruptedException {
    CompletableResultCode resultCode = new CompletableResultCode();

    CountDownLatch completions = new CountDownLatch(1);

    new Thread(
            new Runnable() {
              @Override
              public void run() {
                resultCode.succeed().fail();
              }
            })
        .start();

    resultCode.whenComplete(
        new Runnable() {
          @Override
          public void run() {
            completions.countDown();
          }
        });

    completions.await(3, TimeUnit.SECONDS);

    assertThat(resultCode.isSuccess()).isTrue();
  }
}
