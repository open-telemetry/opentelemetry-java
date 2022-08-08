/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.internal;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import org.jctools.queues.MpscArrayQueue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class JcToolsTest {

  ArrayList<String> batch = new ArrayList<>(10);

  @Test
  void drain_ArrayBlockingQueue() {
    // Arrange
    batch.add("Test3");
    Queue<String> queue = new ArrayBlockingQueue<>(10);
    queue.add("Test1");
    queue.add("Test2");

    // Act
    JcTools.drain(queue, 5, batch::add);

    // Assert
    assertThat(batch).hasSize(3);
    assertThat(queue).hasSize(0);
  }

  @Test
  void drain_MessagePassingQueue() {
    // Arrange
    batch.add("Test3");
    Queue<String> queue = new MpscArrayQueue<>(10);
    queue.add("Test1");
    queue.add("Test2");

    // Act
    JcTools.drain(queue, 5, batch::add);

    // Assert
    assertThat(batch).hasSize(3);
    assertThat(queue).hasSize(0);
  }

  @Test
  void drain_MaxBatch() {
    // Arrange
    Queue<String> queue = new MpscArrayQueue<>(10);
    queue.add("Test1");
    queue.add("Test2");

    // Act
    JcTools.drain(queue, 1, batch::add);

    // Assert
    assertThat(batch).hasSize(1);
    assertThat(queue).hasSize(1);
  }

  @Test
  void newFixedSize_MpscQueue() {
    // Arrange
    int capacity = 10;

    // Act
    Queue<Object> objects = JcTools.newFixedSizeQueue(capacity);

    // Assert
    assertThat(objects).isInstanceOf(MpscArrayQueue.class);
  }

  @Test
  void capacity_MpscQueue() {
    // Arrange
    int capacity = 10;
    Queue<Object> queue = JcTools.newFixedSizeQueue(capacity);

    // Act
    long queueSize = JcTools.capacity(queue);

    // Assert
    assertThat(queueSize).isGreaterThan(capacity);
  }

  @Test
  void capacity_ArrayBlockingQueue() {
    // Arrange
    Queue<String> queue = new ArrayBlockingQueue<>(10);

    // Act
    long queueSize = JcTools.capacity(queue);

    // Assert
    assertThat(queueSize).isEqualTo(10);
  }
}
