/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.internal;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Queue;
import org.jctools.queues.MessagePassingQueue;
import org.junit.jupiter.api.Test;

class JcToolsTest {

  ArrayList<String> batch = new ArrayList<>(10);

  @Test
  void drain_MessagePassingQueue() {
    // Arrange
    batch.add("Test3");
    Queue<String> queue = JcTools.newFixedSizeQueue(10);
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
    Queue<String> queue = JcTools.newFixedSizeQueue(10);
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
    assertThat(objects).isInstanceOf(MessagePassingQueue.class);
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
}
