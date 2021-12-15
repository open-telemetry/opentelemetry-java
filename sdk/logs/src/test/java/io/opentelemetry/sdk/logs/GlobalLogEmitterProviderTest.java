package io.opentelemetry.sdk.logs;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;

class GlobalLogEmitterProviderTest {

  @Test
  void canSetProvider() {
    GlobalLogEmitterProvider.set(null);
    assertSame(LogEmitterProvider.noop(), GlobalLogEmitterProvider.get());
    LogEmitterProvider provider = mock(LogEmitterProvider.class);
    GlobalLogEmitterProvider.set(provider);
    assertSame(provider, GlobalLogEmitterProvider.get());
  }
}
