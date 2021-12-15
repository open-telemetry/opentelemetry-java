package io.opentelemetry.sdk.logs;

import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

class DefaultLogEmitterProviderTest {

  private final LogEmitterProvider logEmitterProvider = DefaultLogEmitterProvider.getInstance();

  @Test
  void builder_doesNotThrow() {
    DefaultLogEmitterProvider.getInstance().logEmitterBuilder("test")
        .setSchemaUrl("http")
        .setInstrumentationVersion("").build();
  }

  @Test
  void builder_default() {
    assertSame(logEmitterProvider.get("test"), DefaultLogEmitter.getInstance());
  }
}
