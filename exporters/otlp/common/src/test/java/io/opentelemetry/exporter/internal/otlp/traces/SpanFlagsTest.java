/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp.traces;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.trace.TraceFlags;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link SpanFlags}. */
public class SpanFlagsTest {

  @Test
  void withParentIsRemoteFlags() {
    assertThat(SpanFlags.withParentIsRemoteFlags(TraceFlags.fromByte((byte) 0xff), false))
        .isEqualTo(0x1ff);
    assertThat(SpanFlags.withParentIsRemoteFlags(TraceFlags.fromByte((byte) 0x01), false))
        .isEqualTo(0x101);
    assertThat(SpanFlags.withParentIsRemoteFlags(TraceFlags.fromByte((byte) 0x05), false))
        .isEqualTo(0x105);
    assertThat(SpanFlags.withParentIsRemoteFlags(TraceFlags.fromByte((byte) 0x00), false))
        .isEqualTo(0x100);

    assertThat(SpanFlags.withParentIsRemoteFlags(TraceFlags.fromByte((byte) 0xff), true))
        .isEqualTo(0x3ff);
    assertThat(SpanFlags.withParentIsRemoteFlags(TraceFlags.fromByte((byte) 0x01), true))
        .isEqualTo(0x301);
    assertThat(SpanFlags.withParentIsRemoteFlags(TraceFlags.fromByte((byte) 0x05), true))
        .isEqualTo(0x305);
    assertThat(SpanFlags.withParentIsRemoteFlags(TraceFlags.fromByte((byte) 0x00), true))
        .isEqualTo(0x300);
  }

  @Test
  void getTraceFlags() {
    assertThat(SpanFlags.getTraceFlags(0x1ff)).isEqualTo(TraceFlags.fromByte((byte) 0xff));
    assertThat(SpanFlags.getTraceFlags(0xffffffff)).isEqualTo(TraceFlags.fromByte((byte) 0xff));
    assertThat(SpanFlags.getTraceFlags(0x000000ff)).isEqualTo(TraceFlags.fromByte((byte) 0xff));

    assertThat(SpanFlags.getTraceFlags(0x100)).isEqualTo(TraceFlags.fromByte((byte) 0x00));
    assertThat(SpanFlags.getTraceFlags(0xffffff00)).isEqualTo(TraceFlags.fromByte((byte) 0x00));
    assertThat(SpanFlags.getTraceFlags(0x00000000)).isEqualTo(TraceFlags.fromByte((byte) 0x00));

    assertThat(SpanFlags.getTraceFlags(0x101)).isEqualTo(TraceFlags.fromByte((byte) 0x01));
    assertThat(SpanFlags.getTraceFlags(0xffffff01)).isEqualTo(TraceFlags.fromByte((byte) 0x01));
    assertThat(SpanFlags.getTraceFlags(0x00000001)).isEqualTo(TraceFlags.fromByte((byte) 0x01));
  }

  @Test
  void isKnownWhetherParentIsRemote() {
    assertThat(SpanFlags.isKnownWhetherParentIsRemote(SpanFlags.CONTEXT_HAS_IS_REMOTE_BIT))
        .isTrue();
    assertThat(
            SpanFlags.isKnownWhetherParentIsRemote(
                0x00000001 | SpanFlags.CONTEXT_HAS_IS_REMOTE_BIT))
        .isTrue();
    assertThat(
            SpanFlags.isKnownWhetherParentIsRemote(
                0x10000000 | SpanFlags.CONTEXT_HAS_IS_REMOTE_BIT))
        .isTrue();
    assertThat(
            SpanFlags.isKnownWhetherParentIsRemote(
                0x00000200 | SpanFlags.CONTEXT_HAS_IS_REMOTE_BIT))
        .isTrue();
    assertThat(SpanFlags.isKnownWhetherParentIsRemote(SpanFlags.CONTEXT_IS_REMOTE_MASK)).isTrue();
    assertThat(SpanFlags.isKnownWhetherParentIsRemote(0xffffffff)).isTrue();

    assertThat(SpanFlags.isKnownWhetherParentIsRemote(~SpanFlags.CONTEXT_HAS_IS_REMOTE_BIT))
        .isFalse();
    assertThat(
            SpanFlags.isKnownWhetherParentIsRemote(
                0x00000001 & ~SpanFlags.CONTEXT_HAS_IS_REMOTE_BIT))
        .isFalse();
    assertThat(
            SpanFlags.isKnownWhetherParentIsRemote(
                0x10000000 & ~SpanFlags.CONTEXT_HAS_IS_REMOTE_BIT))
        .isFalse();
    assertThat(
            SpanFlags.isKnownWhetherParentIsRemote(
                0x00000200 & ~SpanFlags.CONTEXT_HAS_IS_REMOTE_BIT))
        .isFalse();
    assertThat(SpanFlags.isKnownWhetherParentIsRemote(0x00000000)).isFalse();
  }

  @Test
  void isParentRemote() {
    assertThat(
            SpanFlags.isParentRemote(
                SpanFlags.CONTEXT_HAS_IS_REMOTE_BIT | SpanFlags.CONTEXT_IS_REMOTE_BIT))
        .isTrue();
    assertThat(
            SpanFlags.isParentRemote(
                0x00000001 | SpanFlags.CONTEXT_HAS_IS_REMOTE_BIT | SpanFlags.CONTEXT_IS_REMOTE_BIT))
        .isTrue();
    assertThat(
            SpanFlags.isParentRemote(
                0x10000000 | SpanFlags.CONTEXT_HAS_IS_REMOTE_BIT | SpanFlags.CONTEXT_IS_REMOTE_BIT))
        .isTrue();
    assertThat(
            SpanFlags.isParentRemote(
                0x00000200 | SpanFlags.CONTEXT_HAS_IS_REMOTE_BIT | SpanFlags.CONTEXT_IS_REMOTE_BIT))
        .isTrue();
    assertThat(SpanFlags.isParentRemote(SpanFlags.CONTEXT_IS_REMOTE_MASK)).isTrue();
    assertThat(SpanFlags.isParentRemote(0xffffffff)).isTrue();

    assertThat(SpanFlags.isParentRemote(SpanFlags.CONTEXT_HAS_IS_REMOTE_BIT)).isFalse();
    assertThat(SpanFlags.isParentRemote(~SpanFlags.CONTEXT_HAS_IS_REMOTE_BIT)).isFalse();
    assertThat(SpanFlags.isParentRemote(SpanFlags.CONTEXT_IS_REMOTE_BIT)).isFalse();
    assertThat(SpanFlags.isParentRemote(~SpanFlags.CONTEXT_IS_REMOTE_BIT)).isFalse();
    assertThat(
            SpanFlags.isParentRemote(
                ~SpanFlags.CONTEXT_HAS_IS_REMOTE_BIT & ~SpanFlags.CONTEXT_IS_REMOTE_BIT))
        .isFalse();
    assertThat(SpanFlags.isParentRemote(0x00000200 & ~SpanFlags.CONTEXT_HAS_IS_REMOTE_BIT))
        .isFalse();
    assertThat(SpanFlags.isParentRemote(0x00000000)).isFalse();
  }
}
