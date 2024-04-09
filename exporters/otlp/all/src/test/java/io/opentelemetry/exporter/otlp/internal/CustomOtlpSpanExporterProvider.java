package io.opentelemetry.exporter.otlp.internal;

import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.util.Collections;
import java.util.Map;

/**
 * This is just a demo - not a test (yet).
 */
public class CustomOtlpSpanExporterProvider extends OtlpSpanExporterProvider {
  @Override
  public SpanExporter createExporter(ConfigProperties config) {
    return createHttpProtobufExporter(config, c -> {
      c.setHeaders(this::headers);
    });
  }

  private Map<String, String> headers() {
    return Collections.singletonMap("Authorization", "Bearer " + refreshToken());
  }

  private String refreshToken() {
    // e.g. read the token from a kubernetes secret
    return "token";
  }
}
