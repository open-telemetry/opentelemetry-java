/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.processcontext;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.testing.time.TestClock;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class ProcessContextHeaderTest {

  @Test
  void emptyHeader() {
    try (Arena arena = Arena.ofConfined()) {
      ProcessContextHeader processContextHeader =
          new ProcessContextHeader(arena.allocate(ProcessContextHeader.byteSize()));
      byte[] content = processContextHeader.getMemorySegment().toArray(ValueLayout.JAVA_BYTE);
      assertThat(content).isEqualTo(new byte[32]);
    }
  }

  @Test
  void headerPublication() {
    try (Arena arena = Arena.ofConfined()) {
      ProcessContextHeader processContextHeader =
          new ProcessContextHeader(arena.allocate(ProcessContextHeader.byteSize()));

      TestClock clock = TestClock.create();

      MemorySegment payloadA = arena.allocate(20);
      processContextHeader.publish(payloadA, clock.now()); // initial publication
      validateContent(processContextHeader.getMemorySegment(), payloadA, clock.now());

      clock.advance(Duration.ofSeconds(1));

      MemorySegment payloadB = arena.allocate(30);
      processContextHeader.publish(payloadB, clock.now()); // update publication
      validateContent(processContextHeader.getMemorySegment(), payloadB, clock.now());
    }
  }

  private static void validateContent(MemorySegment published, MemorySegment payload, long now) {
    byte[] actualSignature = published.asSlice(0, 8).toArray(ValueLayout.JAVA_BYTE);
    assertThat(actualSignature).isEqualTo("OTEL_CTX".getBytes(StandardCharsets.UTF_8));

    assertThat(ValueLayout.JAVA_INT.order()).isEqualTo(ByteOrder.nativeOrder());

    int actualVersion = published.get(ValueLayout.JAVA_INT, 8);
    assertThat(actualVersion).isEqualTo(2);

    int actualPayloadSize = published.get(ValueLayout.JAVA_INT, 12);
    assertThat(actualPayloadSize).isEqualTo(payload.byteSize());

    long actualTimestamp = published.get(ValueLayout.JAVA_LONG, 16);
    assertThat(actualTimestamp).isEqualTo(now);

    long actualAddress = published.get(ValueLayout.ADDRESS, 24).address();
    assertThat(actualAddress).isEqualTo(payload.address());
  }
}
