package io.opentelemetry.sdk.trace.export;

import java.util.Collection;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.data.SpanData;

final class NoopSpanExporter implements SpanExporter {

  private static final SpanExporter INSTANCE = new NoopSpanExporter();

  static SpanExporter getInstance() {
    return INSTANCE;
  }

  @Override
  public CompletableResultCode export(Collection<SpanData> spans) {
    return CompletableResultCode.ofSuccess();
  }

  @Override
  public CompletableResultCode flush() {
    return CompletableResultCode.ofSuccess();
  }

  @Override
  public CompletableResultCode shutdown() {
    return CompletableResultCode.ofSuccess();
  }
}
