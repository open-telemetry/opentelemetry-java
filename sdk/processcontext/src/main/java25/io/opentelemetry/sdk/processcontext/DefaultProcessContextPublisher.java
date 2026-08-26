/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.processcontext;

import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.common.export.MessageWriter;
import io.opentelemetry.sdk.processcontext.data.ProcessContextData;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.StructLayout;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import javax.annotation.Nullable;

@SuppressWarnings({"restricted", "ThrowSpecificExceptions"})
public class DefaultProcessContextPublisher implements ProcessContextPublisher {

  private static final String OTEL_NAME = "OTEL_CTX";

  // System constants
  private static final int PROT_READ = 0x1;
  private static final int PROT_WRITE = 0x2;
  private static final int MAP_PRIVATE = 0x02;
  private static final int MAP_ANONYMOUS = 0x20;
  private static final int MADV_DONTFORK = 0x10;
  private static final int PR_SET_VMA = 0x53564d41;
  private static final int PR_SET_VMA_ANON_NAME = 0;
  private static final int MFD_CLOEXEC = 0x0001;
  private static final int MFD_ALLOW_SEALING = 0x0002;
  private static final int MFD_NOEXEC_SEAL = 0x0008;
  private static final int EINVAL = 22;

  private static final long MAPPING_SIZE = ProcessContextHeader.byteSize();

  private static final Linker LINKER = Linker.nativeLinker();
  private static final SymbolLookup LIBC = LINKER.defaultLookup();

  private static final Linker.Option ERRNO_LINKER_OPTION;
  private static final StructLayout ERRNO_STATE_LAYOUT;
  private static final VarHandle ERRNO_HANDLE;

  private static final MethodHandle MMAP;
  private static final MethodHandle MUNMAP;
  private static final MethodHandle MADVISE;
  private static final MethodHandle PRCTL;
  private static final MethodHandle MEMFD_CREATE;
  private static final MethodHandle FTRUNCATE;
  private static final MethodHandle CLOSE;

  static {
    try {

      // note that whilst all these libc functions support errno,
      // we care about it only on memfd_create
      ERRNO_LINKER_OPTION = Linker.Option.captureCallState("errno");
      ERRNO_STATE_LAYOUT = Linker.Option.captureStateLayout();
      ERRNO_HANDLE = ERRNO_STATE_LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("errno"));

      MMAP =
          LINKER.downcallHandle(
              LIBC.find("mmap").orElseThrow(),
              // void *mmap(void addr[.length], size_t length, int prot, int flags, int fd, off_t
              // offset);
              FunctionDescriptor.of(
                  ValueLayout.ADDRESS,
                  ValueLayout.ADDRESS,
                  ValueLayout.JAVA_LONG,
                  ValueLayout.JAVA_INT,
                  ValueLayout.JAVA_INT,
                  ValueLayout.JAVA_INT,
                  ValueLayout.JAVA_LONG));
      MUNMAP =
          LINKER.downcallHandle(
              LIBC.find("munmap").orElseThrow(),
              // int munmap(void addr[.length], size_t length)
              FunctionDescriptor.of(
                  ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG));
      MADVISE =
          LINKER.downcallHandle(
              LIBC.find("madvise").orElseThrow(),
              // int madvise(void addr[.size], size_t size, int advice)
              FunctionDescriptor.of(
                  ValueLayout.JAVA_INT,
                  ValueLayout.ADDRESS,
                  ValueLayout.JAVA_LONG,
                  ValueLayout.JAVA_INT));
      PRCTL =
          LINKER.downcallHandle(
              LIBC.find("prctl").orElseThrow(),
              // int prctl(int op, ...)
              FunctionDescriptor.of(
                  ValueLayout.JAVA_INT,
                  ValueLayout.JAVA_INT,
                  ValueLayout.JAVA_INT,
                  ValueLayout.ADDRESS,
                  ValueLayout.JAVA_LONG,
                  ValueLayout.ADDRESS));
      MEMFD_CREATE =
          LINKER.downcallHandle(
              LIBC.find("memfd_create").orElseThrow(),
              // int memfd_create(const char *name, unsigned int flags)
              FunctionDescriptor.of(
                  ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT),
              ERRNO_LINKER_OPTION);
      FTRUNCATE =
          LINKER.downcallHandle(
              LIBC.find("ftruncate").orElseThrow(),
              // int ftruncate(int fd, off_t length)
              FunctionDescriptor.of(
                  ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG));
      CLOSE =
          LINKER.downcallHandle(
              LIBC.find("close").orElseThrow(),
              // int close(int fd)
              FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT));
    } catch (Throwable e) {
      throw new RuntimeException("Failed to initialize native method handles", e);
    }
  }

  @Nullable private MemorySegment mapping = null;
  @Nullable private ProcessContextHeader header = null;
  @Nullable private Arena payloadArena = null;

  @Override
  @SuppressWarnings({"unused", "NullAway"})
  public synchronized void publish(ProcessContextData processContextData, Clock clock)
      throws IOException {

    Arena prevPayloadArena = null;

    try {

      if (header == null) {

        // first time - follow 'publication' steps
        mapping = initializeMapping(); // spec: publication steps 1-5
        header = new ProcessContextHeader(mapping);
        // spec publication step 6 (equiv updating step 1). Encode payload
        payloadArena = Arena.ofShared();
        MemorySegment payload = encode(payloadArena, processContextData);
        header.publish(payload, clock.now()); // spec: publication steps 7-9
        nameMapping(); // spec: publication step 10.

      } else {

        // already published - follow 'updating' steps
        prevPayloadArena = payloadArena;
        // spec updating step 1. Encode payload
        payloadArena = Arena.ofShared();
        MemorySegment payload = encode(payloadArena, processContextData);
        header.update(payload, clock.now()); // spec: updating steps 2-6
        prevPayloadArena.close(); // free old payload memory
        nameMapping(); // spec: updating step 7.
      }

    } catch (Throwable t) {
      throw new IOException(t);
    }
  }

  // https://github.com/open-telemetry/opentelemetry-specification/blob/main/oteps/profiles/4719-process-ctx.md#publication-protocol
  private static MemorySegment initializeMapping() throws Throwable {

    MemorySegment mapping;

    // spec: publication step 2a. Allocate new memfd and size it: Create a new memfd using
    // memfd_create("OTEL_CTX", MFD_CLOEXEC | MFD_ALLOW_SEALING | MFD_NOEXEC_SEAL)
    int fd = -1;
    try (Arena arena = Arena.ofConfined()) {
      MemorySegment nameSegment = arena.allocateFrom(OTEL_NAME);
      MemorySegment errnoSegment = arena.allocate(ERRNO_STATE_LAYOUT);

      fd =
          (Integer)
              MEMFD_CREATE.invoke(
                  errnoSegment, nameSegment, MFD_CLOEXEC | MFD_ALLOW_SEALING | MFD_NOEXEC_SEAL);

      if (fd < 0 && ((int) ERRNO_HANDLE.get(errnoSegment, 0) == EINVAL)) {
        // spec: publication step 2a. MFD_NOEXEC_SEAL is only on 6.3+, fallback for older kernels:
        fd =
            (Integer)
                MEMFD_CREATE.invoke(errnoSegment, nameSegment, MFD_CLOEXEC | MFD_ALLOW_SEALING);
      }
    }

    if (fd >= 0) {
      // spec: publication step 2b. size it with ftruncate.
      if ((Integer) FTRUNCATE.invoke(fd, MAPPING_SIZE) == -1) {
        CLOSE.invoke(fd);
        throw new IOException("Failed to truncate memfd");
      }

      // spec: publication step 3. Allocate a new mmap from the memfd then close the memfd: Setup an
      // mmap using mmap(..., PROT_READ | PROT_WRITE, MAP_PRIVATE, memfd, 0).  This makes the memfd
      // show up in /proc/<pid>/maps; afterwards the file descriptor can be closed
      mapping =
          ((MemorySegment)
                  MMAP.invoke(
                      MemorySegment.NULL, // addr
                      MAPPING_SIZE, // length
                      PROT_READ | PROT_WRITE, // prot
                      MAP_PRIVATE, // flags
                      fd, // fd
                      0L // offset
                      ))
              .reinterpret(MAPPING_SIZE);
      CLOSE.invoke(fd);
    } else {
      // spec: publication step 4. If memfd is not available (step 2): If system security
      // restrictions disallow memfd, fall back to creating a new anonymous mapping using mmap(...,
      // PROT_READ | PROT_WRITE, MAP_PRIVATE | MAP_ANONYMOUS, -1, 0) and use that instead
      // Note that's we've just assumed the cause of the memfd failure, we're not sure it's a
      // security issue.
      mapping =
          ((MemorySegment)
                  MMAP.invoke(
                      MemorySegment.NULL, // addr
                      MAPPING_SIZE, // length
                      PROT_READ | PROT_WRITE, // prot
                      MAP_PRIVATE | MAP_ANONYMOUS, // flags
                      -1, // fd
                      0L // offset
                      ))
              .reinterpret(MAPPING_SIZE);
    }

    if (mapping.address() == -1L) {
      throw new IOException("Failed to allocate mapping");
    }

    // spec: publication step 5. Prevent fork inheritance: Apply madvise(..., MADV_DONTFORK) to
    // prevent child processes from inheriting stale data
    int madviseResult = (Integer) MADVISE.invoke(mapping, MAPPING_SIZE, MADV_DONTFORK);
    if (madviseResult == -1) {
      MUNMAP.invoke(mapping, MAPPING_SIZE);
      throw new IOException("Failed to setup MADV_DONTFORK");
    }
    return mapping;
  }

  private void nameMapping() throws Throwable {
    // spec: publication step 10. and updating protocol step 7: Name mapping
    // Use prctl(PR_SET_VMA, PR_SET_VMA_ANON_NAME, ...,"OTEL_CTX") to name the mapping.
    // This step should be done unconditionally, although naming
    // mappings is not always supported by the kernel.
    try (Arena arena = Arena.ofConfined()) {
      MemorySegment nameSegment = arena.allocateFrom(OTEL_NAME);
      PRCTL.invoke(PR_SET_VMA, PR_SET_VMA_ANON_NAME, mapping, MAPPING_SIZE, nameSegment);
    }
  }

  private static MemorySegment encode(Arena arena, ProcessContextData processContextData)
      throws IOException {
    // depressingly clunky, as OutputStream doesn't play nice with MemorySegment.
    ProcessContextMarshaler processContextMarshaler =
        ProcessContextMarshaler.create(processContextData);
    MessageWriter messageWriter = processContextMarshaler.toBinaryMessageWriter();
    ByteArrayOutputStream outputStream =
        new ByteArrayOutputStream(messageWriter.getContentLength());
    messageWriter.writeMessage(outputStream);
    outputStream.close();
    MemorySegment memorySegment = arena.allocate(messageWriter.getContentLength());
    memorySegment.copyFrom(MemorySegment.ofArray(outputStream.toByteArray()));
    return memorySegment;
  }
}
