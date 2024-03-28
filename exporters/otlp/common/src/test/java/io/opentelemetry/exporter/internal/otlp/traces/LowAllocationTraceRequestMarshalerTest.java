/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp.traces;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class LowAllocationTraceRequestMarshalerTest {

  @Test
  void validateOutput() throws Exception {
    RequestMarshalState state = new RequestMarshalState();
    state.setup();

    byte[] result;
    {
      TraceRequestMarshaler requestMarshaler = TraceRequestMarshaler.create(state.spanDataList);
      ByteArrayOutputStream customOutput =
          new ByteArrayOutputStream(requestMarshaler.getBinarySerializedSize());
      requestMarshaler.writeBinaryTo(customOutput);
      result = customOutput.toByteArray();
    }

    byte[] lowAllocationResult;
    {
      LowAllocationTraceRequestMarshaler requestMarshaler =
          new LowAllocationTraceRequestMarshaler();
      requestMarshaler.initialize(state.spanDataList);
      ByteArrayOutputStream customOutput =
          new ByteArrayOutputStream(requestMarshaler.getBinarySerializedSize());
      requestMarshaler.writeBinaryTo(customOutput);
      lowAllocationResult = customOutput.toByteArray();
    }

    assertThat(lowAllocationResult).isEqualTo(result);
  }

  @Test
  void validateJsonOutput() throws Exception {
    RequestMarshalState state = new RequestMarshalState();
    state.setup();

    String result;
    {
      TraceRequestMarshaler requestMarshaler = TraceRequestMarshaler.create(state.spanDataList);
      ByteArrayOutputStream customOutput =
          new ByteArrayOutputStream(requestMarshaler.getBinarySerializedSize());
      requestMarshaler.writeJsonTo(customOutput);
      result = new String(customOutput.toByteArray(), StandardCharsets.UTF_8);
    }

    String lowAllocationResult;
    {
      LowAllocationTraceRequestMarshaler requestMarshaler =
          new LowAllocationTraceRequestMarshaler();
      requestMarshaler.initialize(state.spanDataList);
      ByteArrayOutputStream customOutput =
          new ByteArrayOutputStream(requestMarshaler.getBinarySerializedSize());
      requestMarshaler.writeJsonTo(customOutput);
      lowAllocationResult = new String(customOutput.toByteArray(), StandardCharsets.UTF_8);
    }

    assertThat(lowAllocationResult).isEqualTo(result);
  }
}
