/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry;

import static org.assertj.core.api.Assertions.assertThat;

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
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.testcontainers.shaded.okhttp3.Call;
import org.testcontainers.shaded.okhttp3.OkHttpClient;
import org.testcontainers.shaded.okhttp3.Request;
import org.testcontainers.shaded.okhttp3.Response;
import org.testcontainers.shaded.okhttp3.ResponseBody;
import spark.Service;

/** Integration tests for the B3 propagators, in various configurations. */
public class B3PropagationIntegrationTest {

  private static final int server1Port = new Random().nextInt(40000) + 1024;
  private static final int server2Port = new Random().nextInt(40000) + 1024;
  private static final InMemorySpanExporter spanExporter = InMemorySpanExporter.create();

  private Service server1;
  private Service server2;

  private void setup(TextMapPropagator propagator) {
    setupServer1(propagator);
    setupServer2(propagator);
  }

  @AfterEach
  void shutdown() {
    if (server2 != null) {
      server2.stop();
    }
    if (server1 != null) {
      server1.stop();
    }
    spanExporter.reset();
  }

  @ParameterizedTest
  @ArgumentsSource(PropagatorArgumentSupplier.class)
  void propagation(TextMapPropagator propagator) throws IOException {
    setup(propagator);
    OpenTelemetrySdk clientSdk = setupClient(propagator);

    OkHttpClient httpClient = createPropagatingClient(clientSdk);

    Span span = clientSdk.getTracer("testTracer").spanBuilder("clientSpan").startSpan();
    try (Scope ignored = span.makeCurrent()) {
      Call call =
          httpClient.newCall(
              new Request.Builder().get().url("http://localhost:" + server1Port + "/test").build());

      try (Response r = call.execute()) {
        ResponseBody body = r.body();
        assertThat(body.string()).isEqualTo("OK");
      }
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
  @ArgumentsSource(PropagatorArgumentSupplier.class)
  void noClientTracing(TextMapPropagator propagator) throws IOException {
    setup(propagator);
    OkHttpClient httpClient = new OkHttpClient();

    Call call =
        httpClient.newCall(
            new Request.Builder().get().url("http://localhost:" + server1Port + "/test").build());

    try (Response r = call.execute()) {
      ResponseBody body = r.body();
      assertThat(body.string()).isEqualTo("OK");
    }

    List<SpanData> finishedSpanItems = spanExporter.getFinishedSpanItems();
    // 2 spans, one from each of the servers
    assertThat(finishedSpanItems).hasSize(2);
    String traceId = finishedSpanItems.get(0).getTraceId();

    assertThat(finishedSpanItems)
        .allSatisfy(spanData -> assertThat(spanData.getTraceId()).isEqualTo(traceId));
  }

  private static OkHttpClient createPropagatingClient(OpenTelemetrySdk sdk) {
    return new OkHttpClient.Builder()
        .addNetworkInterceptor(
            chain -> {
              Request request = chain.request();

              Request.Builder requestBuilder = request.newBuilder();

              sdk.getPropagators()
                  .getTextMapPropagator()
                  .inject(Context.current(), requestBuilder, Request.Builder::header);

              request = requestBuilder.build();
              return chain.proceed(request);
            })
        .build();
  }

  private static OpenTelemetrySdk setupClient(TextMapPropagator propagator) {
    OpenTelemetrySdk clientSdk =
        OpenTelemetrySdk.builder()
            .setPropagators(ContextPropagators.create(propagator))
            .setTracerProvider(
                SdkTracerProvider.builder()
                    .addSpanProcessor(SimpleSpanProcessor.create(spanExporter))
                    .build())
            .build();
    return clientSdk;
  }

  private void setupServer1(TextMapPropagator propagator) {
    OpenTelemetrySdk serverSdk =
        OpenTelemetrySdk.builder()
            .setPropagators(ContextPropagators.create(propagator))
            .setTracerProvider(
                SdkTracerProvider.builder()
                    .addSpanProcessor(SimpleSpanProcessor.create(spanExporter))
                    .build())
            .build();

    OkHttpClient serverClient = createPropagatingClient(serverSdk);
    server1 = Service.ignite().port(server1Port);
    server1.get(
        "/test",
        (request, response) -> {
          Context incomingContext = extract(request, serverSdk);

          Span span =
              serverSdk
                  .getTracer("server1Tracer")
                  .spanBuilder("server1Span")
                  .setParent(incomingContext)
                  .startSpan();
          try (Scope ignored = span.makeCurrent()) {
            Call call =
                serverClient.newCall(
                    new Request.Builder()
                        .get()
                        .url("http://localhost:" + server2Port + "/test2")
                        .build());

            try (Response r = call.execute()) {
              assertThat(r.code()).isEqualTo(200);
            }

            return "OK";
          } finally {
            span.end();
          }
        });
    server1.awaitInitialization();
  }

  private void setupServer2(TextMapPropagator propagator) {
    OpenTelemetrySdk serverSdk =
        OpenTelemetrySdk.builder()
            .setPropagators(ContextPropagators.create(propagator))
            .setTracerProvider(
                SdkTracerProvider.builder()
                    .addSpanProcessor(SimpleSpanProcessor.create(spanExporter))
                    .build())
            .build();

    server2 = Service.ignite().port(server2Port);
    server2.get(
        "/test2",
        (request, response) -> {
          Context incomingContext = extract(request, serverSdk);

          Span span =
              serverSdk
                  .getTracer("server2Tracer")
                  .spanBuilder("server2Span")
                  .setParent(incomingContext)
                  .startSpan();
          try (Scope ignored = span.makeCurrent()) {
            return "OK";
          } finally {
            span.end();
          }
        });
    server2.awaitInitialization();
  }

  private static Context extract(spark.Request request, OpenTelemetrySdk sdk) {
    return sdk.getPropagators()
        .getTextMapPropagator()
        .extract(
            Context.root(),
            request,
            new TextMapGetter<spark.Request>() {
              @Override
              public Iterable<String> keys(spark.Request carrier) {
                return carrier.headers();
              }

              @Nullable
              @Override
              public String get(@Nullable spark.Request carrier, String key) {
                return carrier.headers(key);
              }
            });
  }

  public static class PropagatorArgumentSupplier implements ArgumentsProvider {

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
      return Stream.of(
          Arguments.of(B3Propagator.injectingMultiHeaders()),
          Arguments.of(B3Propagator.injectingSingleHeader()));
    }
  }
}
