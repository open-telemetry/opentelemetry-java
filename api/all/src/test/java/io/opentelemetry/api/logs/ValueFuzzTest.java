/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.logs;

import static org.assertj.core.api.Assertions.assertThat;

import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;
import edu.berkeley.cs.jqf.fuzz.junit.GuidedFuzzing;
import edu.berkeley.cs.jqf.fuzz.random.NoGuidance;
import io.opentelemetry.api.common.Value;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.Test;
import org.junit.runner.Result;
import org.junit.runner.RunWith;

public class ValueFuzzTest {
  @RunWith(JQF.class)
  public static class FuzzTestCases {
    @Fuzz
    public void valueByteAsStringFuzz(String randomString) {
      String base64Encoded = Value.of(randomString.getBytes(StandardCharsets.UTF_8)).asString();
      byte[] decodedBytes = Base64.getDecoder().decode(base64Encoded);
      assertThat(new String(decodedBytes, StandardCharsets.UTF_8)).isEqualTo(randomString);
    }
  }

  @SuppressWarnings("SystemOut")
  @Test
  void valueByteAsStringFuzzing() {
    Result result =
        GuidedFuzzing.run(
            FuzzTestCases.class,
            "valueByteAsStringFuzz",
            new NoGuidance(10000, System.out),
            System.out);
    assertThat(result.wasSuccessful()).isTrue();
  }
}
