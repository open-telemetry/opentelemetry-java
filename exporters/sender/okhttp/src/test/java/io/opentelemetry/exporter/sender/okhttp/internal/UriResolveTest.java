package io.opentelemetry.exporter.sender.okhttp.internal;

import org.junit.jupiter.api.Test;
import java.net.URI;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class UriResolveTest {

  @Test
  void resolve() {
    assertThat(URI.create("http://localhost:8080").resolve("path").toString()).isEqualTo("http://localhost:8080/path");
  }
}
