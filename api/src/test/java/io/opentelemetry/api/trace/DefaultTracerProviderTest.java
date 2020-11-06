package io.opentelemetry.api.trace;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class DefaultTracerProviderTest {

  @Test
  void rejectsNullInstrumentationName() {
    assertThrows(NullPointerException.class, () -> DefaultTracerProvider.getInstance().get(null));
    assertThrows(NullPointerException.class, () -> DefaultTracerProvider.getInstance().get(null, "1.0.0"));
  }
}
