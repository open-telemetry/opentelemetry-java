/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.linecorp.armeria.client.WebClient;
import com.linecorp.armeria.common.AggregatedHttpResponse;
import com.linecorp.armeria.common.HttpMethod;
import com.linecorp.armeria.common.MediaType;
import com.linecorp.armeria.common.RequestHeaders;
import com.linecorp.armeria.common.RequestHeadersBuilder;
import com.linecorp.armeria.server.Server;
import com.linecorp.armeria.server.annotation.Blocking;
import com.linecorp.armeria.server.annotation.Post;
import com.linecorp.armeria.server.healthcheck.HealthCheckService;
import com.linecorp.armeria.server.logging.LoggingService;
import io.netty.util.AsciiString;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapSetter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

public final class Application {
  private static final Logger logger = Logger.getLogger(Application.class.getName());

  private static final ObjectMapper objectMapper = new ObjectMapper();
  private static final OpenTelemetry openTelemetry;

  static {
    openTelemetry =
        OpenTelemetrySdk.builder()
            .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
            .build();
  }

  private enum ArmeriaGetter implements TextMapGetter<RequestHeaders> {
    INSTANCE;

    @Override
    public Iterable<String> keys(RequestHeaders carrier) {
      return carrier.names().stream().map(AsciiString::toString).collect(Collectors.toList());
    }

    @Nullable
    @Override
    public String get(@Nullable RequestHeaders carrier, String key) {
      if (carrier == null) {
        return null;
      }
      return carrier.get(key);
    }
  }

  private enum ArmeriaSetter implements TextMapSetter<RequestHeadersBuilder> {
    INSTANCE;

    @Override
    public void set(@Nullable RequestHeadersBuilder carrier, String key, String value) {
      if (carrier == null) {
        return;
      }
      carrier.set(key, value);
    }
  }

  private static class Service {
    private final WebClient client = WebClient.of();

    @Post("/verify-tracecontext")
    @Blocking
    public String serve(RequestHeaders headers, List<Request> requests) {
      Context context =
          openTelemetry
              .getPropagators()
              .getTextMapPropagator()
              .extract(Context.current(), headers, ArmeriaGetter.INSTANCE);

      for (io.opentelemetry.Request req : requests) {
        Span span =
            openTelemetry
                .getTracer("validation-server")
                .spanBuilder("Entering Validation Server")
                .setParent(context)
                .startSpan();

        Context withSpanContext = context.with(span);

        RequestHeadersBuilder outHeaders =
            RequestHeaders.builder(HttpMethod.POST, req.getUrl()).contentType(MediaType.JSON_UTF_8);
        openTelemetry
            .getPropagators()
            .getTextMapPropagator()
            .inject(withSpanContext, outHeaders, ArmeriaSetter.INSTANCE);

        try {
          AggregatedHttpResponse response =
              client
                  .execute(outHeaders.build(), objectMapper.writeValueAsBytes(req.getArguments()))
                  .aggregate()
                  .join();
          logger.info("response: " + response.status());
        } catch (Throwable t) {
          logger.log(Level.SEVERE, "failed to send", t);
        }
        span.end();
      }
      return "Done";
    }
  }

  private Application() {}

  /** Entry point. */
  public static void main(String[] args) {
    Server server =
        Server.builder()
            .http(5000)
            .annotatedService(new Service())
            .service("/health", HealthCheckService.of())
            .decorator(LoggingService.newDecorator())
            .build();
    server.start().join();
    Runtime.getRuntime().addShutdownHook(new Thread(() -> server.stop().join()));
  }
}
