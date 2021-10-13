/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.baggage.propagation;

import static org.assertj.core.api.Assertions.assertThat;

import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;
import java.net.URLDecoder;
import org.junit.runner.RunWith;

@RunWith(JQF.class)
public class PercentEscaperFuzzTest {

  private final PercentEscaper percentEscaper = new PercentEscaper();

  @Fuzz
  public void roundTripWithUrlDecoder(String value) throws Exception {
    String escaped = percentEscaper.escape(value);
    String decoded = URLDecoder.decode(escaped, "UTF-8");
    assertThat(decoded).isEqualTo(value);
  }
}
