package io.opentelemetry.sdk.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import org.junit.jupiter.api.Test;

class OpenTelemetrySdkAutoConfigurationTest {

  @Test
  void initializeAndGet() {
    OpenTelemetrySdk sdk = OpenTelemetrySdkAutoConfiguration.initialize();
    assertThat(GlobalOpenTelemetry.get()).isSameAs(sdk);
  }
}
