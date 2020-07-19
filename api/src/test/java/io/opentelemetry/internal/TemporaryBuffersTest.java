package io.opentelemetry.internal;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;

public class TemporaryBuffersTest {

  @Test
  public void chars() {
    TemporaryBuffers.clearChars();
    char[] buffer10 = TemporaryBuffers.chars(10);
    assertThat(buffer10).hasLength(10);
    char[] buffer8 = TemporaryBuffers.chars(8);
    // Buffer was reused even though smaller.
    assertThat(buffer8).isSameInstanceAs(buffer10);
    char[] buffer20 = TemporaryBuffers.chars(20);
    assertThat(buffer20).hasLength(20);
  }
}
