package io.opentelemetry.api.metrics;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class DefaultMeterProviderTest {

  @Test
  void rejectsNullInstrumentationName() {
    assertThrows(NullPointerException.class, () -> DefaultMeterProvider.getInstance().get(null));
    assertThrows(NullPointerException.class, () -> DefaultMeterProvider.getInstance().get(null, "1.0.0"));
  }
}
