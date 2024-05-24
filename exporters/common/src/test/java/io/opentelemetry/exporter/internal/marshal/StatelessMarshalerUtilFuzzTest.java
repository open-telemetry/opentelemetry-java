/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.marshal;

import static io.opentelemetry.exporter.internal.marshal.StatelessMarshalerUtil.getUtf8Size;
import static io.opentelemetry.exporter.internal.marshal.StatelessMarshalerUtilTest.testUtf8;
import static org.assertj.core.api.Assertions.assertThat;

import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;
import edu.berkeley.cs.jqf.fuzz.junit.GuidedFuzzing;
import edu.berkeley.cs.jqf.fuzz.random.NoGuidance;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.junit.runner.Result;
import org.junit.runner.RunWith;

@SuppressWarnings("SystemOut")
class StatelessMarshalerUtilFuzzTest {

  @RunWith(JQF.class)
  public static class EncodeUf8 {

    @Fuzz
    public void encodeRandomString(String value) {
      int utf8Size = value.getBytes(StandardCharsets.UTF_8).length;
      assertThat(getUtf8Size(value, false)).isEqualTo(utf8Size);
      assertThat(getUtf8Size(value, true)).isEqualTo(utf8Size);
      assertThat(testUtf8(value, utf8Size, /* useUnsafe= */ false)).isEqualTo(value);
      assertThat(testUtf8(value, utf8Size, /* useUnsafe= */ true)).isEqualTo(value);
    }
  }

  // driver methods to avoid having to use the vintage junit engine, and to enable increasing the
  // number of iterations:

  @Test
  void encodeUf8WithFuzzing() {
    Result result =
        GuidedFuzzing.run(
            EncodeUf8.class,
            "encodeRandomString",
            new NoGuidance(10000, System.out),
            System.out);
    assertThat(result.wasSuccessful()).isTrue();
  }
}
