package io.opentelemetry.api.baggage.propagation;

import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;
import org.junit.runner.RunWith;

import java.net.URLDecoder;

import static org.assertj.core.api.Assertions.assertThat;

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
