/*
 * Copyright 2020, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry;

import com.google.gson.Gson;
import io.grpc.Context;
import io.opentelemetry.context.propagation.DefaultContextPropagators;
import io.opentelemetry.context.propagation.TextMapPropagator.Getter;
import io.opentelemetry.context.propagation.TextMapPropagator.Setter;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.TracingContextUtils;
import io.opentelemetry.trace.propagation.HttpTraceContext;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

public class Application {
  private static final Logger logger = Logger.getLogger(Application.class.getName());

  private Application() {}

  /** Entry point. */
  public static void main(String[] args) {
    OpenTelemetry.setPropagators(
        DefaultContextPropagators.builder()
            .addTextMapPropagator(HttpTraceContext.getInstance())
            .build());

    Spark.port(5000);
    Spark.post(
        "verify-tracecontext",
        new Route() {
          @Override
          public Object handle(Request request, Response response) {
            final Gson gson = new Gson();

            final io.opentelemetry.Request[] requests =
                gson.fromJson(request.body(), io.opentelemetry.Request[].class);

            Context context =
                OpenTelemetry.getGlobalPropagators()
                    .getTextMapPropagator()
                    .extract(
                        Context.current(),
                        request.raw(),
                        new Getter<HttpServletRequest>() {
                          @Override
                          public String get(HttpServletRequest carrier, String key) {
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
                          }
                        });

            for (io.opentelemetry.Request req : requests) {
              Span span =
                  OpenTelemetry.getGlobalTracer("validation-server")
                      .spanBuilder("Entering Validation Server")
                      .setParent(context)
                      .startSpan();

              Context withSpanContext = TracingContextUtils.withSpan(span, context);

              // Make a new request using the builder
              okhttp3.Request.Builder reqBuilder = new okhttp3.Request.Builder();

              // Inject the current context into the new request.
              OpenTelemetry.getGlobalPropagators()
                  .getTextMapPropagator()
                  .inject(
                      withSpanContext,
                      reqBuilder,
                      new Setter<okhttp3.Request.Builder>() {
                        @Override
                        public void set(okhttp3.Request.Builder carrier, String key, String value) {
                          carrier.addHeader(key, value);
                        }
                      });

              // Add the post body and build the request
              String argumentsJson = gson.toJson(req.getArguments());
              RequestBody argumentsBody =
                  RequestBody.create(
                      MediaType.parse("application/json; charset=utf-8"), argumentsJson);
              okhttp3.Request newRequest = reqBuilder.url(req.getUrl()).post(argumentsBody).build();

              // Execute the request
              OkHttpClient client = new OkHttpClient();
              try (okhttp3.Response res = client.newCall(newRequest).execute()) {
                logger.info("response: " + res.code());
              } catch (Exception e) {
                logger.log(Level.SEVERE, "failed to send", e);
              }
              span.end();
            }

            return "Done";
          }
        });

    Spark.awaitInitialization();
  }
}
