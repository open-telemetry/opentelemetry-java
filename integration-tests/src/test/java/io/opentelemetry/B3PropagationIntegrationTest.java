/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry;

import static org.assertj.core.api.Assertions.assertThat;

import com.linecorp.armeria.client.ClientRequestContext;
import com.linecorp.armeria.client.WebClient;
import com.linecorp.armeria.common.HttpRequest;
import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.server.HttpService;
import com.linecorp.armeria.server.ServerBuilder;
import com.linecorp.armeria.server.ServiceRequestContext;
import com.linecorp.armeria.server.logging.LoggingService;
import com.linecorp.armeria.testing.junit5.server.ServerExtension;
import io.netty.util.AsciiString;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.extension.trace.propagation.B3Propagator;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

/** Integration tests for the B3 propagators, in various configurations. */
class B3PropagationIntegrationTest {

  private static final InMemorySpanExporter spanExporter = InMemorySpanExporter.create();

  static WebClient b3MultiClient;
  static WebClient b3SingleClient;

  private static class FrontendService implements HttpService {
    private final OpenTelemetry openTelemetry;
    private final Supplier<WebClient> client;

    FrontendService(OpenTelemetry openTelemetry, Supplier<WebClient> client) {
      this.openTelemetry = openTelemetry;
      this.client = client;
    }

    @Override
    public HttpResponse serve(ServiceRequestContext ctx, HttpRequest req) {
      Context incomingContext = extract(req, openTelemetry);
      Span span =
          openTelemetry
              .getTracer("server1Tracer")
              .spanBuilder("server1Span")
              .setParent(incomingContext)
              .startSpan();
      try (Scope ignored = span.makeCurrent()) {
        return HttpResponse.from(
            client
                .get()
                .get("/backend")
                .aggregate()
                .thenApply(
                    unused -> {
                      span.end();
                      return HttpResponse.of("OK");
                    }));
      }
    }
  }

  private static class BackendService implements HttpService {
    private final OpenTelemetry openTelemetry;

    BackendService(OpenTelemetry openTelemetry) {
      this.openTelemetry = openTelemetry;
    }

    @Override
    public HttpResponse serve(ServiceRequestContext ctx, HttpRequest req) throws Exception {
      Context incomingContext = extract(req, openTelemetry);

      Span span =
          openTelemetry
              .getTracer("server2Tracer")
              .spanBuilder("server2Span")
              .setParent(incomingContext)
              .startSpan();
      try (Scope ignored = span.makeCurrent()) {
        return HttpResponse.of("OK");
      } finally {
        span.end();
      }
    }
  }

  @RegisterExtension
  static final ServerExtension server =
      new ServerExtension() {
        @Override
        protected void configure(ServerBuilder sb) {
          SdkTracerProvider tracerProvider =
              SdkTracerProvider.builder()
                  .addSpanProcessor(SimpleSpanProcessor.create(spanExporter))
                  .build();
          OpenTelemetry b3MultiOpenTelemetry =
              OpenTelemetrySdk.builder()
                  .setTracerProvider(tracerProvider)
                  .setPropagators(ContextPropagators.create(B3Propagator.injectingMultiHeaders()))
                  .build();
          OpenTelemetry b3SingleOpenTelemetry =
              OpenTelemetrySdk.builder()
                  .setTracerProvider(tracerProvider)
                  .setPropagators(ContextPropagators.create(B3Propagator.injectingSingleHeader()))
                  .build();
          sb.service(
              "/multi/frontend", new FrontendService(b3MultiOpenTelemetry, () -> b3MultiClient));
          sb.service("/multi/backend", new BackendService(b3MultiOpenTelemetry));
          sb.service(
              "/single/frontend", new FrontendService(b3SingleOpenTelemetry, () -> b3SingleClient));
          sb.service("/single/backend", new BackendService(b3SingleOpenTelemetry));

          sb.decorator(LoggingService.newDecorator());
        }
      };

  @BeforeAll
  static void setup() {
    b3MultiClient =
        createPropagatingClient(
            server.httpUri().resolve("/multi"), B3Propagator.injectingMultiHeaders());
    b3SingleClient =
        createPropagatingClient(
            server.httpUri().resolve("/single"), B3Propagator.injectingSingleHeader());
  }

  @AfterEach
  void shutdown() {
    spanExporter.reset();
  }

  @ParameterizedTest
  @ArgumentsSource(WebClientArgumentSupplier.class)
  void propagation(String testType, WebClient client) throws IOException {
    OpenTelemetrySdk clientSdk = setupClient();

    Span span = clientSdk.getTracer("testTracer").spanBuilder("clientSpan").startSpan();
    try (Scope ignored = span.makeCurrent()) {
      assertThat(client.get("/frontend").aggregate().join().contentUtf8()).isEqualTo("OK");
    } finally {
      span.end();
    }

    List<SpanData> finishedSpanItems = spanExporter.getFinishedSpanItems();
    // 3 spans, one from the client, and one from each of the servers.
    assertThat(finishedSpanItems).hasSize(3);
    String traceId = finishedSpanItems.get(0).getTraceId();

    assertThat(finishedSpanItems)
        .allSatisfy(spanData -> assertThat(spanData.getTraceId()).isEqualTo(traceId));
  }

  @ParameterizedTest
  @ArgumentsSource(WebClientArgumentSupplier.class)
  void noClientTracing(String testType, WebClient client) throws IOException {
    assertThat(client.get("/frontend").aggregate().join().contentUtf8()).isEqualTo("OK");

    List<SpanData> finishedSpanItems = spanExporter.getFinishedSpanItems();
    // 2 spans, one from each of the servers
    assertThat(finishedSpanItems).hasSize(2);
    String traceId = finishedSpanItems.get(0).getTraceId();

    assertThat(finishedSpanItems)
        .allSatisfy(spanData -> assertThat(spanData.getTraceId()).isEqualTo(traceId));
  }

  private static WebClient createPropagatingClient(URI uri, TextMapPropagator propagator) {
    return WebClient.builder(uri)
        .decorator(
            (delegate, ctx, req) -> {
              propagator.inject(
                  Context.current(), ctx, ClientRequestContext::addAdditionalRequestHeader);
              return delegate.execute(ctx, req);
            })
        .build();
  }

  private static OpenTelemetrySdk setupClient() {
    return OpenTelemetrySdk.builder()
        .setTracerProvider(
            SdkTracerProvider.builder()
                .addSpanProcessor(SimpleSpanProcessor.create(spanExporter))
                .build())
        .build();
  }

  private static Context extract(HttpRequest request, OpenTelemetry sdk) {
    return sdk.getPropagators()
        .getTextMapPropagator()
        .extract(
            Context.root(),
            request,
            new TextMapGetter<HttpRequest>() {
              @Override
              public Iterable<String> keys(HttpRequest carrier) {
                return () ->
                    carrier.headers().names().stream().map(AsciiString::toString).iterator();
              }

              @Nullable
              @Override
              public String get(@Nullable HttpRequest carrier, String key) {
                return carrier.headers().get(key);
              }
            });
  }

  public static class WebClientArgumentSupplier implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
      return Stream.of(
          Arguments.of("b3multi", b3MultiClient), Arguments.of("b3single", b3SingleClient));
    }
  }
}
