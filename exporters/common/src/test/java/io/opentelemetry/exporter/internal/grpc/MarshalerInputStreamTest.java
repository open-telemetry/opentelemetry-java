/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.grpc;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.common.export.MessageWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class MarshalerInputStreamTest {

  private static final byte[] CONTENT = "grpc-test".getBytes(StandardCharsets.UTF_8);

  private static MessageWriter messageWriter(byte[] bytes) {
    return new TestMessageWriter(bytes);
  }

  private static MarshalerInputStream streamWithPartial(byte[] partialBytes) {
    try {
      MarshalerInputStream stream = new MarshalerInputStream(messageWriter(new byte[0]));
      Field messageField = MarshalerInputStream.class.getDeclaredField("message");
      messageField.setAccessible(true);
      messageField.set(stream, null);

      Field partialField = MarshalerInputStream.class.getDeclaredField("partial");
      partialField.setAccessible(true);
      partialField.set(stream, new ByteArrayInputStream(partialBytes));
      return stream;
    } catch (ReflectiveOperationException e) {
      throw new LinkageError("Failed to set partial field via reflection", e);
    }
  }

  private static final class TestMessageWriter implements MessageWriter {
    private final byte[] data;

    TestMessageWriter(byte[] data) {
      this.data = data;
    }

    @Override
    public int getContentLength() {
      return data.length;
    }

    @Override
    public void writeMessage(OutputStream out) throws IOException {
      out.write(data);
    }
  }

  @Test
  void available_withMessage() {
    MarshalerInputStream stream = new MarshalerInputStream(messageWriter(CONTENT));
    assertThat(stream.available()).isEqualTo(CONTENT.length);
  }

  @Test
  void available_withPartial() {
    MarshalerInputStream stream = streamWithPartial(CONTENT);
    assertThat(stream.available()).isEqualTo(CONTENT.length);
  }

  @Test
  void available_afterFullyDrained() throws IOException {
    MarshalerInputStream stream = new MarshalerInputStream(messageWriter(CONTENT));
    stream.drainTo(new ByteArrayOutputStream());
    assertThat(stream.available()).isEqualTo(0);
  }

  @Test
  void drainTo_fromMessage() throws IOException {
    MarshalerInputStream stream = new MarshalerInputStream(messageWriter(CONTENT));
    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    int written = stream.drainTo(baos);

    assertThat(written).isEqualTo(CONTENT.length);
    assertThat(baos.toByteArray()).isEqualTo(CONTENT);
  }

  @Test
  void drainTo_fromPartial() throws IOException {
    MarshalerInputStream stream = streamWithPartial(CONTENT);

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    int written = stream.drainTo(baos);

    assertThat(written).isEqualTo(CONTENT.length);
    assertThat(baos.toByteArray()).isEqualTo(CONTENT);
  }

  @Test
  void drainTo_afterAlreadyDrained() throws IOException {
    MarshalerInputStream stream = new MarshalerInputStream(messageWriter(CONTENT));
    stream.drainTo(new ByteArrayOutputStream());

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    int written = stream.drainTo(baos);

    assertThat(written).isEqualTo(0);
    assertThat(baos.size()).isEqualTo(0);
  }

  @Test
  void readSingleByte_fromMessage() throws IOException {
    MarshalerInputStream stream = new MarshalerInputStream(messageWriter(CONTENT));

    int firstByte = stream.read();
    assertThat(firstByte).isEqualTo(CONTENT[0] & 0xFF);

    // Read remaining bytes
    for (int i = 1; i < CONTENT.length; i++) {
      assertThat(stream.read()).isEqualTo(CONTENT[i] & 0xFF);
    }
    assertThat(stream.read()).isEqualTo(-1);
  }

  @Test
  void readSingleByte_fromPartial() throws IOException {
    MarshalerInputStream stream = streamWithPartial(CONTENT);

    int firstByte = stream.read();
    assertThat(firstByte).isEqualTo(CONTENT[0] & 0xFF);
  }

  @Test
  void readSingleByte_afterFullyDrained() throws IOException {
    MarshalerInputStream stream = new MarshalerInputStream(messageWriter(CONTENT));
    stream.drainTo(new ByteArrayOutputStream());

    assertThat(stream.read()).isEqualTo(-1);
  }

  @Test
  void readBulk_fromMessage() throws IOException {
    MarshalerInputStream stream = new MarshalerInputStream(messageWriter(CONTENT));

    byte[] buf = new byte[CONTENT.length];
    int bytesRead = stream.read(buf, 0, buf.length);

    assertThat(bytesRead).isEqualTo(CONTENT.length);
    assertThat(buf).isEqualTo(CONTENT);

    // Subsequent read must return -1
    assertThat(stream.read(buf, 0, buf.length)).isEqualTo(-1);
  }

  @Test
  void readBulk_zeroLengthMessage() throws IOException {
    MarshalerInputStream stream = new MarshalerInputStream(messageWriter(new byte[0]));

    byte[] buf = new byte[10];
    int bytesRead = stream.read(buf, 0, buf.length);

    assertThat(bytesRead).isEqualTo(-1);
  }

  @Test
  void readBulk_fromPartial() throws IOException {
    MarshalerInputStream stream = streamWithPartial(CONTENT);

    byte[] buf = new byte[CONTENT.length];
    int bytesRead = stream.read(buf, 0, buf.length);

    assertThat(bytesRead).isEqualTo(CONTENT.length);
    assertThat(buf).isEqualTo(CONTENT);
  }

  @Test
  void readBulk_afterFullyDrained() throws IOException {
    MarshalerInputStream stream = new MarshalerInputStream(messageWriter(CONTENT));
    stream.drainTo(new ByteArrayOutputStream());

    byte[] buf = new byte[CONTENT.length];
    int bytesRead = stream.read(buf, 0, buf.length);

    assertThat(bytesRead).isEqualTo(-1);
  }
}
