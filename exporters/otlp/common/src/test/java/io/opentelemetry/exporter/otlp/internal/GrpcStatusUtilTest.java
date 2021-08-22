/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.rpc.Status;
import java.io.IOException;
import org.junit.jupiter.api.Test;

class GrpcStatusUtilTest {

  @Test
  void parseMessage() throws Exception {
    assertThat(
            GrpcStatusUtil.getStatusMessage(
                Status.newBuilder().setMessage("test").build().toByteArray()))
        .isEqualTo("test");
    assertThat(
            GrpcStatusUtil.getStatusMessage(
                Status.newBuilder().setCode(2).setMessage("test2").build().toByteArray()))
        .isEqualTo("test2");
    assertThat(
            GrpcStatusUtil.getStatusMessage(
                Status.newBuilder()
                    .setCode(2)
                    .setMessage("test3")
                    .addDetails(Any.newBuilder().setValue(ByteString.copyFromUtf8("any")).build())
                    .build()
                    .toByteArray()))
        .isEqualTo("test3");
    assertThat(
            GrpcStatusUtil.getStatusMessage(
                Status.newBuilder()
                    .setCode(2)
                    .addDetails(Any.newBuilder().setValue(ByteString.copyFromUtf8("any")).build())
                    .build()
                    .toByteArray()))
        .isEmpty();
    assertThat(GrpcStatusUtil.getStatusMessage(Status.getDefaultInstance().toByteArray()))
        .isEmpty();
    assertThatThrownBy(() -> GrpcStatusUtil.getStatusMessage(new byte[] {0, 1, 3, 0}))
        .isInstanceOf(IOException.class);
  }
}
