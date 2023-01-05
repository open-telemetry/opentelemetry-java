@SuppressWarnings({"requires-automatic", "requires-transitive-automatic"})
module io.opentelemetry.exporter.prometheus {
  exports io.opentelemetry.exporter.prometheus;

  requires transitive io.opentelemetry.sdk.metrics;
  requires jdk.httpserver;
  requires java.logging;
}
