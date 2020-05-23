package io.opentelemetry.sdk.trace;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.when;

import io.opentelemetry.sdk.trace.RandomIdsGenerator.RandomSupplier;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.TraceId;
import java.util.Random;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@RunWith(JUnit4.class)
public class RandomIdsGeneratorTest {

  @Mock private Random random;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void defaults() {
    RandomIdsGenerator generator = new RandomIdsGenerator();

    // Can't assert values but can assert they're valid.
    TraceId traceId = generator.generateTraceId();
    assertThat(traceId).isNotEqualTo(TraceId.getInvalid());

    SpanId spanId = generator.generateSpanId();
    assertThat(spanId).isNotEqualTo(SpanId.getInvalid());
  }

  @Test
  public void customRandom() {
    RandomIdsGenerator generator =
        new RandomIdsGenerator(
            new RandomSupplier() {
              @Override
              public Random get() {
                return random;
              }
            });

    when(random.nextLong()).thenReturn(1L).thenReturn(2L);
    TraceId traceId = generator.generateTraceId();
    assertThat(traceId.toLowerBase16()).isEqualTo("00000000000000010000000000000002");

    when(random.nextLong()).thenReturn(3L);
    SpanId spanId = generator.generateSpanId();
    assertThat(spanId.toLowerBase16()).isEqualTo("0000000000000003");
  }

  @Test
  public void ignoresInvalid() {
    RandomIdsGenerator generator =
        new RandomIdsGenerator(
            new RandomSupplier() {
              @Override
              public Random get() {
                return random;
              }
            });

    // First two zeros skipped
    when(random.nextLong()).thenReturn(0L).thenReturn(0L).thenReturn(0L).thenReturn(4L);
    TraceId traceId = generator.generateTraceId();
    assertThat(traceId.toLowerBase16()).isEqualTo("00000000000000000000000000000004");

    when(random.nextLong()).thenReturn(0L).thenReturn(5L);
    SpanId spanId = generator.generateSpanId();
    assertThat(spanId.toLowerBase16()).isEqualTo("0000000000000005");
  }
}
