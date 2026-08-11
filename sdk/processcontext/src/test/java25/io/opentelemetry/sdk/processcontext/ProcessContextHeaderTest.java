/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.processcontext;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class ProcessContextHeaderTest {

  @Test
  void testEmptyHeader() {
    try (Arena arena = Arena.ofConfined()) {
      ProcessContextHeader processContextHeader =
          new ProcessContextHeader(arena.allocate(ProcessContextHeader.byteSize()));
      byte[] content = processContextHeader.getMemorySegment().toArray(ValueLayout.JAVA_BYTE);
      assertArrayEquals(new byte[32], content);
    }
  }

  @Test
  void testHeaderPublication() {
    try (Arena arena = Arena.ofConfined()) {
      ProcessContextHeader processContextHeader =
          new ProcessContextHeader(arena.allocate(ProcessContextHeader.byteSize()));

      MemorySegment payloadA = arena.allocate(20);
      processContextHeader.publish(payloadA); // initial publication
      validateContent(processContextHeader.getMemorySegment(), payloadA);

      MemorySegment payloadB = arena.allocate(30);
      processContextHeader.publish(payloadB); // update publication
      validateContent(processContextHeader.getMemorySegment(), payloadB);
    }
  }

  private static void validateContent(MemorySegment published, MemorySegment payload) {
    byte[] actualSignature = published.asSlice(0, 8).toArray(ValueLayout.JAVA_BYTE);
    assertArrayEquals("OTEL_CTX".getBytes(StandardCharsets.UTF_8), actualSignature);

    assertEquals(ByteOrder.nativeOrder(), ValueLayout.JAVA_INT.order());

    int actualVersion = published.get(ValueLayout.JAVA_INT, 8);
    assertEquals(2, actualVersion);

    int actualPayloadSize = published.get(ValueLayout.JAVA_INT, 12);
    assertEquals(payload.byteSize(), actualPayloadSize);

    long actualAddress = published.get(ValueLayout.ADDRESS, 24).address();
    assertEquals(payload.address(), actualAddress);
  }
}
