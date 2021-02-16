package io.oopentelemetry.example.httpserver.instrumented;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import io.opentelemetry.api.metrics.GlobalMetricsProvider;
import io.opentelemetry.api.metrics.LongValueRecorder;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.common.Labels;
import io.opentelemetry.api.metrics.common.LabelsBuilder;
import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * An instrumented version of an HttpServer.
 *
 * <p>This class demonstrates how someone could instrument the sun http library with metrics that
 * are later coonsumed by a client. Theoretically this would be an API (like spring-web) that users
 * are using to write HTTP servers. This code is expected to be written by library authors, not by
 * Application developers.
 *
 * <p>For example, if using spring-web, this code would already been written by Spring framework and
 * not live in the user's application code at all. We pretend this Handler class is part of the
 * public API of some framework.
 */
public class InstrumentedHttpServer {
  private HttpServer actualServer;
  /** The meter to use in our fake HTTP server to grab all metrics types. */
  private static final Meter httpMeter =
      // Pull metrics from the "global" OpenTelemetry.  This is because we are
      // a "library" author, and we aren't configuring how/where metrics are exported.
      GlobalMetricsProvider
          // Construct a meter that lets users know the metrics came from our library, and
          // not their own code.  This allows downstream filtering/altering of "built-in" metrics
          // a bit easier, as you can find/filter what you need from various sources, similar to
          // "Logger.getLogger(name)" in logging libraries.
          .getMeter("io.opentelemetry.example.metrics-http.server", "0.1.0");

  /** The description of the metric where we'll be storing durations values. */
  private static final LongValueRecorder durationMetric =
      httpMeter
          .longValueRecorderBuilder("http.server.duration")
          .setDescription("Length of time for handling an HTTP request")
          .setUnit("ms")
          .build();

  public InstrumentedHttpServer(int port) throws IOException {
    this.actualServer = HttpServer.create(new InetSocketAddress(port), 0);
  }

  /** Registers a handler for a given HTTP resource path. */
  public void creeateContext(String path, HttpHandler handler) {
    this.actualServer.createContext(path, new InstrumentedHttpHandler(handler));
  }

  /** Implementation of HttpHandler that calculates duration metrics and delegates. */
  private static class InstrumentedHttpHandler implements HttpHandler {
    private final HttpHandler delegate;

    public InstrumentedHttpHandler(HttpHandler delegate) {
      this.delegate = delegate;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
      // TODO - text map propagator?
      long start = System.currentTimeMillis();
      LabelsBuilder labels =
          Labels.builder()
              .put("http.flavor", exchange.getProtocol())
              .put("http.method", exchange.getRequestMethod())
              .put("http.scheme", "http") // TODO - is https?
              .put("http.host", "localhost")
              .put("http.target", exchange.getHttpContext().getPath());
      try {
        delegate.handle(exchange);
        if (exchange.getResponseCode() != -1) {
          labels.put("http.status_code", Integer.toString(exchange.getResponseCode()));
        }
      } catch (IOException e) {
        // Note: here we're just making sure errors are reported appropriately.
        if (exchange.getResponseCode() != -1) {
          labels.put("http.status_code", Integer.toString(exchange.getResponseCode()));
        } else {
          labels.put("http.status_code", "500");
        }
        throw e;
      } finally {
        long end = System.currentTimeMillis();
        durationMetric.record(end - start, labels.build());
      }
    }
  }

  /** Starts the HTTP server. */
  public void start() {
    this.actualServer.start();
  }

  /** Stops the HTTP server. */
  public void stop(int value) {
    this.actualServer.stop(value);
  }
}
