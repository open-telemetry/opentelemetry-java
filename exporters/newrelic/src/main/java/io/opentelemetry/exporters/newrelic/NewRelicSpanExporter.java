package io.opentelemetry.exporters.newrelic;

import io.opentelemetry.proto.trace.v1.Span;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.util.List;

public class NewRelicSpanExporter implements SpanExporter {

  @Override
  public ResultCode export(List<Span> spans) {
    return null;
  }

  @Override
  public void shutdown() {

  }
}
