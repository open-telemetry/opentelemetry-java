/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry;

import com.google.gson.Gson;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.propagation.HttpTraceContext;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.DefaultContextPropagators;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;
import spark.Spark;

public class Application {

  private static final Logger logger = Logger.getLogger(Application.class.getName());
  private static final OpenTelemetry openTelemetry;

  static {
    openTelemetry =
        OpenTelemetry.get().toBuilder()
            .setPropagators(
                DefaultContextPropagators.builder()
                    .addTextMapPropagator(HttpTraceContext.getInstance())
                    .build())
            .build();
    // set the updated instance as the global instance, just to make sure.
    OpenTelemetry.set(openTelemetry);
  }

  private Application() {}

  /** Entry point. */
  public static void main(String[] args) {
    Spark.port(5000);
    Spark.post(
        "verify-tracecontext",
        (request, response) -> {
          final Gson gson = new Gson();

          final io.opentelemetry.Request[] requests =
              gson.fromJson(request.body(), io.opentelemetry.Request[].class);

          Context context =
              openTelemetry
                  .getPropagators()
                  .getTextMapPropagator()
                  .extract(
                      Context.current(),
                      request.raw(),
                      (carrier, key) -> {
                        Enumeration<String> headers = carrier.getHeaders(key);
                        if (headers == null || !headers.hasMoreElements()) {
                          return null;
                        }
                        List<String> values = new ArrayList<>();
                        while (headers.hasMoreElements()) {
                          String nextElement = headers.nextElement();
                          if (!nextElement.trim().isEmpty()) {
                            values.add(nextElement);
                          }
                        }
                        if (values.isEmpty()) {
                          return null;
                        }
                        if (values.size() == 1) {
                          return values.get(0);
                        }
                        StringBuilder builder = new StringBuilder(values.get(0));
                        for (int i = 1; i < values.size(); i++) {
                          builder.append(",").append(values.get(i));
                        }

                        return builder.toString();
                      });

          for (io.opentelemetry.Request req : requests) {
            Span span =
                openTelemetry
                    .getTracer("validation-server")
                    .spanBuilder("Entering Validation Server")
                    .setParent(context)
                    .startSpan();

            Context withSpanContext = context.with(span);

            // Make a new request using the builder
            okhttp3.Request.Builder reqBuilder = new okhttp3.Request.Builder();

            // Inject the current context into the new request.
            openTelemetry
                .getPropagators()
                .getTextMapPropagator()
                .inject(withSpanContext, reqBuilder, okhttp3.Request.Builder::addHeader);

            // Add the post body and build the request
            String argumentsJson = gson.toJson(req.getArguments());
            RequestBody argumentsBody =
                RequestBody.create(
                    MediaType.parse("application/json; charset=utf-8"), argumentsJson);
            okhttp3.Request newRequest = reqBuilder.url(req.getUrl()).post(argumentsBody).build();

            // Execute the request
            OkHttpClient client = new OkHttpClient();
            try (Response res = client.newCall(newRequest).execute()) {
              logger.info("response: " + res.code());
            } catch (Exception e) {
              logger.log(Level.SEVERE, "failed to send", e);
            }
            span.end();
          }

          return "Done";
        });

    Spark.awaitInitialization();
  }
}
