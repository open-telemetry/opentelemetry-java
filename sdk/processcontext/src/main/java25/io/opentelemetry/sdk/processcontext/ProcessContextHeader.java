/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.processcontext;

import java.lang.foreign.GroupLayout;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.charset.StandardCharsets;

/**
 * Provides a typesafe overlay on a region of memory, with layout conforming to the process context
 * header specification provided in OTEP-4719.
 *
 * <p>This class is not threadsafe and must be externally synchronized.
 *
 * @see <a
 *     href="https://github.com/open-telemetry/opentelemetry-specification/blob/main/oteps/profiles/4719-process-ctx.md">OTEP
 *     4719</a>
 */
final class ProcessContextHeader {

  private static final byte[] OTEL_CTX_SIGNATURE = "OTEL_CTX".getBytes(StandardCharsets.UTF_8);
  private static final int VERSION = 2;

  // https://github.com/open-telemetry/opentelemetry-specification/blob/main/oteps/profiles/4719-process-ctx.md#header-structure
  private static final GroupLayout HEADER_LAYOUT =
      MemoryLayout.structLayout(
          MemoryLayout.sequenceLayout(8, ValueLayout.JAVA_BYTE).withName("signature"),
          ValueLayout.JAVA_INT.withName("version"),
          ValueLayout.JAVA_INT.withName("payload_size"),
          ValueLayout.JAVA_LONG.withName("monotonic_published_at_ns"),
          ValueLayout.ADDRESS.withName("payload"));

  private static final VarHandle VERSION_HANDLE =
      MethodHandles.insertCoordinates(
          HEADER_LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("version")), 1, 0L);
  private static final VarHandle PAYLOAD_SIZE_HANDLE =
      MethodHandles.insertCoordinates(
          HEADER_LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("payload_size")), 1, 0L);
  private static final VarHandle TIMESTAMP_HANDLE =
      MethodHandles.insertCoordinates(
          HEADER_LAYOUT.varHandle(
              MemoryLayout.PathElement.groupElement("monotonic_published_at_ns")),
          1,
          0L);
  private static final VarHandle PAYLOAD_ADDRESS_HANDLE =
      MethodHandles.insertCoordinates(
          HEADER_LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("payload")), 1, 0L);

  static long byteSize() {
    return HEADER_LAYOUT.byteSize();
  }

  private final MemorySegment memorySegment;

  /**
   * Wraps the given region of memory.
   *
   * @param memorySegment the underlying memory.
   */
  ProcessContextHeader(MemorySegment memorySegment) {
    this.memorySegment = memorySegment;
  }

  // for test integration
  MemorySegment getMemorySegment() {
    return memorySegment;
  }

  /**
   * Runs the header-specific steps of the publication protocol.
   *
   * @param payload the memory holding the serialized payload information.
   * @param now the timestamp to publish.
   * @see <a
   *     href="https://github.com/open-telemetry/opentelemetry-specification/blob/main/oteps/profiles/4719-process-ctx.md#publication-protocol">OTEP
   *     4719 publication protocol</a>
   */
  void publish(MemorySegment payload, long now) {

    // spec: publication step 7. Write header fields: Populate signature, version, payload_size,
    // payload but not yet
    // monotonic_published_at_ns
    MemorySegment.copy(
        OTEL_CTX_SIGNATURE, 0, memorySegment.asSlice(0, 8), ValueLayout.JAVA_BYTE, 0, 8);
    VERSION_HANDLE.set(memorySegment, VERSION);
    publishPayload(payload);

    // spec: publication step 8. Memory barrier: Use language/compiler-specific techniques to ensure
    // all previous
    // writes complete before proceeding (atomic_thread_fence(memory_order_seq_cst) or equivalent)
    VarHandle.fullFence();

    // spec: publication step 9. Write timestamp: Write monotonic_published_at_ns last. This field
    // is used to detect
    // that the context is ready for use by the reader.
    publishTimestamp(now);
  }

  /**
   * Runs the header-specific steps of the updating protocol.
   *
   * @param payload the memory holding the serialized payload information.
   * @param now the timestamp to publish.
   * @see <a
   *     href="https://github.com/open-telemetry/opentelemetry-specification/blob/main/oteps/profiles/4719-process-ctx.md##updating-protocol">OTEP
   *     4719 updating protocol</a>
   */
  void update(MemorySegment payload, long now) {

    // spec: updating step 2. Signal update start: Write 0 to the monotonic_published_at_ns field.
    // This signals to
    // readers that an update is in progress (readers verify this field is non-zero).
    TIMESTAMP_HANDLE.set(memorySegment, 0L);

    // spec: updating step 3. Memory barrier: Ensure the write to monotonic_published_at_ns is
    // visible before
    // proceeding (atomic_thread_fence(memory_order_seq_cst) or equivalent).
    VarHandle.fullFence();

    // spec: updating step 4. Update payload fields: Update the payload pointer and payload_size
    // fields to point
    // to the new payload.
    publishPayload(payload);

    // spec: updating step 5. Memory barrier: Ensure the payload fields are updated before
    // finalizing the
    // timestamp (atomic_thread_fence(memory_order_seq_cst) or equivalent).
    VarHandle.fullFence();

    // spec: updating step 6. Signal update complete: Write the new timestamp to
    // monotonic_published_at_ns; this
    // is an aligned 64-bit write and thus expected to be atomic.
    publishTimestamp(now);
  }

  private void publishPayload(MemorySegment payload) {
    PAYLOAD_SIZE_HANDLE.set(memorySegment, (int) payload.byteSize());
    PAYLOAD_ADDRESS_HANDLE.set(memorySegment, payload);
  }

  private void publishTimestamp(long now) {
    TIMESTAMP_HANDLE.set(memorySegment, now);
  }
}
