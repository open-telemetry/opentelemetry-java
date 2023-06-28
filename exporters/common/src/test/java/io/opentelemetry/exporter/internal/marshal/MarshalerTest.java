/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.marshal;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.io.OutputStream;
import org.junit.jupiter.api.Test;

class MarshalerTest {

  @Test
  void writeTo_NoSelfSuppressionError() throws IOException {
    Marshaler marshaler =
        new Marshaler() {
          @Override
          public int getBinarySerializedSize() {
            return 0;
          }

          @Override
          protected void writeTo(Serializer output) throws IOException {
            for (int i = 0; i < (50 * 1024 + 100) / 8; i++) {
              output.writeFixed64Value(i);
            }
          }
        };
    OutputStream os = mock(OutputStream.class);

    IOException error = new IOException("error!");
    doThrow(error).when(os).write(any(), anyInt(), anyInt());
    doThrow(error).when(os).write(any());
    doThrow(error).when(os).write(anyInt());
    doThrow(error).when(os).flush();

    // If an exception is thrown writing bytes, and that same exception is thrown in the #close
    // method cleaning up the AutoCloseable resource, an IllegalArgumentException will be thrown
    // indicating illegal self suppression. Ensure an IOException is thrown instead.
    assertThatThrownBy(() -> marshaler.writeBinaryTo(os)).isInstanceOf(IOException.class);
    assertThatThrownBy(() -> marshaler.writeJsonTo(os)).isInstanceOf(IOException.class);
  }
}
