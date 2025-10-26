/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.marshal;

import static io.opentelemetry.exporter.internal.marshal.StringEncoderTest.testUtf8;
import static org.assertj.core.api.Assertions.assertThat;

import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;
import edu.berkeley.cs.jqf.fuzz.junit.GuidedFuzzing;
import edu.berkeley.cs.jqf.fuzz.random.NoGuidance;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnJre;
import org.junit.jupiter.api.condition.JRE;
import org.junit.runner.Result;
import org.junit.runner.RunWith;

@SuppressWarnings("SystemOut")
class StringEncoderFuzzTest {

  private static final StringEncoder fallbackStringEncoder =
      StringEncoderHolder.createFallbackEncoder();
  private static final StringEncoder unsafeStringEncoder =
      StringEncoderHolder.createUnsafeEncoder();
  private static final StringEncoder varHandleStringEncoder =
      StringEncoderHolder.createVarHandleEncoder();

  @RunWith(JQF.class)
  public static class EncodeUf8 {

    @Fuzz
    public void encodeRandomString_Fallback(String value) {
      assertThat(fallbackStringEncoder).isNotNull();
      int utf8Size = value.getBytes(StandardCharsets.UTF_8).length;
      assertThat(fallbackStringEncoder.getUtf8Size(value)).isEqualTo(utf8Size);
      assertThat(testUtf8(value, utf8Size, fallbackStringEncoder)).isEqualTo(value);
    }

    @Fuzz
    public void encodeRandomString_Unsafe(String value) {
      assertThat(unsafeStringEncoder).isNotNull();
      int utf8Size = value.getBytes(StandardCharsets.UTF_8).length;
      assertThat(unsafeStringEncoder.getUtf8Size(value)).isEqualTo(utf8Size);
      assertThat(testUtf8(value, utf8Size, unsafeStringEncoder)).isEqualTo(value);
    }

    @Fuzz
    public void encodeRandomString_VarHandle(String value) {
      assertThat(varHandleStringEncoder).isNotNull();
      int utf8Size = value.getBytes(StandardCharsets.UTF_8).length;
      assertThat(varHandleStringEncoder.getUtf8Size(value)).isEqualTo(utf8Size);
      assertThat(testUtf8(value, utf8Size, varHandleStringEncoder)).isEqualTo(value);
    }
  }

  @Test
  void encodeUf8WithFuzzing_Fallback() {
    Result result =
        GuidedFuzzing.run(
            EncodeUf8.class,
            "encodeRandomString_Fallback",
            new NoGuidance(10000, System.out),
            System.out);
    assertThat(result.wasSuccessful()).isTrue();
  }

  @Test
  @DisabledOnJre(JRE.JAVA_8)
  void encodeUf8WithFuzzing_Unsafe() {
    Result result =
        GuidedFuzzing.run(
            EncodeUf8.class,
            "encodeRandomString_Unsafe",
            new NoGuidance(10000, System.out),
            System.out);
    assertThat(result.wasSuccessful()).isTrue();
  }

  @Test
  @DisabledOnJre(JRE.JAVA_8)
  void encodeUf8WithFuzzing_VarHandle() {
    Result result =
        GuidedFuzzing.run(
            EncodeUf8.class,
            "encodeRandomString_VarHandle",
            new NoGuidance(10000, System.out),
            System.out);
    assertThat(result.wasSuccessful()).isTrue();
  }
}
