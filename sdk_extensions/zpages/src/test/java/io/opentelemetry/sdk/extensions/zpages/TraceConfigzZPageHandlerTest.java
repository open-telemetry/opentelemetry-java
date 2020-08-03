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

package io.opentelemetry.sdk.extensions.zpages;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.Samplers;
import io.opentelemetry.sdk.trace.TracerSdkProvider;
import io.opentelemetry.sdk.trace.config.TraceConfig;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link TraceConfigzZPageHandler}. */
public final class TraceConfigzZPageHandlerTest {
  private static final TracerSdkProvider tracerProvider = OpenTelemetrySdk.getTracerProvider();
  private static final Map<String, String> emptyQueryMap = ImmutableMap.of();

  @Test
  void changeTable_emitRowsCorrectly() {
    OutputStream output = new ByteArrayOutputStream();
    String querySamplingProbability = "samplingprobability";
    String queryMaxNumOfAttributes = "maxnumofattributes";
    String queryMaxNumOfEvents = "maxnumofevents";
    String queryMaxNumOfLinks = "maxnumoflinks";
    String queryMaxNumOfAttributesPerEvent = "maxnumofattributesperevent";
    String queryMaxNumOfAttributesPerLink = "maxnumofattributesperlink";

    TraceConfigzZPageHandler traceConfigzZPageHandler =
        new TraceConfigzZPageHandler(tracerProvider);
    traceConfigzZPageHandler.emitHtml(emptyQueryMap, output);

    assertThat(output.toString()).contains("SamplingProbability to");
    assertThat(output.toString()).contains("name=" + querySamplingProbability);
    assertThat(output.toString())
        .contains("(" + TraceConfig.getDefault().getSampler().getDescription() + ")");
    assertThat(output.toString()).contains("MaxNumberOfAttributes to");
    assertThat(output.toString()).contains("name=" + queryMaxNumOfAttributes);
    assertThat(output.toString())
        .contains(
            "(" + Integer.toString(TraceConfig.getDefault().getMaxNumberOfAttributes()) + ")");
    assertThat(output.toString()).contains("MaxNumberOfEvents to");
    assertThat(output.toString()).contains("name=" + queryMaxNumOfEvents);
    assertThat(output.toString())
        .contains("(" + Integer.toString(TraceConfig.getDefault().getMaxNumberOfEvents()) + ")");
    assertThat(output.toString()).contains("MaxNumberOfLinks to");
    assertThat(output.toString()).contains("name=" + queryMaxNumOfLinks);
    assertThat(output.toString())
        .contains("(" + Integer.toString(TraceConfig.getDefault().getMaxNumberOfLinks()) + ")");
    assertThat(output.toString()).contains("MaxNumberOfAttributesPerEvent to");
    assertThat(output.toString()).contains("name=" + queryMaxNumOfAttributesPerEvent);
    assertThat(output.toString())
        .contains(
            "("
                + Integer.toString(TraceConfig.getDefault().getMaxNumberOfAttributesPerEvent())
                + ")");
    assertThat(output.toString()).contains("MaxNumberOfAttributesPerLink to");
    assertThat(output.toString()).contains("name=" + queryMaxNumOfAttributesPerLink);
    assertThat(output.toString())
        .contains(
            "("
                + Integer.toString(TraceConfig.getDefault().getMaxNumberOfAttributesPerLink())
                + ")");
  }

  @Test
  void activeTable_emitRowsCorrectly() {
    OutputStream output = new ByteArrayOutputStream();

    TraceConfigzZPageHandler traceConfigzZPageHandler =
        new TraceConfigzZPageHandler(tracerProvider);
    traceConfigzZPageHandler.emitHtml(emptyQueryMap, output);

    assertThat(output.toString()).contains("Sampler");
    assertThat(output.toString())
        .contains(">" + tracerProvider.getActiveTraceConfig().getSampler().getDescription() + "<");
    assertThat(output.toString()).contains("MaxNumberOfAttributes");
    assertThat(output.toString())
        .contains(
            ">"
                + Integer.toString(tracerProvider.getActiveTraceConfig().getMaxNumberOfAttributes())
                + "<");
    assertThat(output.toString()).contains("MaxNumberOfEvents");
    assertThat(output.toString())
        .contains(
            ">"
                + Integer.toString(tracerProvider.getActiveTraceConfig().getMaxNumberOfEvents())
                + "<");
    assertThat(output.toString()).contains("MaxNumberOfLinks");
    assertThat(output.toString())
        .contains(
            ">"
                + Integer.toString(tracerProvider.getActiveTraceConfig().getMaxNumberOfLinks())
                + "<");
    assertThat(output.toString()).contains("MaxNumberOfAttributesPerEvent");
    assertThat(output.toString())
        .contains(
            ">"
                + Integer.toString(
                    tracerProvider.getActiveTraceConfig().getMaxNumberOfAttributesPerEvent())
                + "<");
    assertThat(output.toString()).contains("MaxNumberOfAttributesPerLink");
    assertThat(output.toString())
        .contains(
            ">"
                + Integer.toString(
                    tracerProvider.getActiveTraceConfig().getMaxNumberOfAttributesPerLink())
                + "<");
  }

  @Test
  void appliesChangesCorrectly_formSubmit() {
    OutputStream output = new ByteArrayOutputStream();
    String querySamplingProbability = "samplingprobability";
    String queryMaxNumOfAttributes = "maxnumofattributes";
    String queryMaxNumOfEvents = "maxnumofevents";
    String queryMaxNumOfLinks = "maxnumoflinks";
    String queryMaxNumOfAttributesPerEvent = "maxnumofattributesperevent";
    String queryMaxNumOfAttributesPerLink = "maxnumofattributesperlink";
    String newSamplingProbability = "0.001";
    String newMaxNumOfAttributes = "16";
    String newMaxNumOfEvents = "16";
    String newMaxNumOfLinks = "16";
    String newMaxNumOfAttributesPerEvent = "16";
    String newMaxNumOfAttributesPerLink = "16";

    Map<String, String> queryMap =
        new ImmutableMap.Builder<String, String>()
            .put("action", "change")
            .put(querySamplingProbability, newSamplingProbability)
            .put(queryMaxNumOfAttributes, newMaxNumOfAttributes)
            .put(queryMaxNumOfEvents, newMaxNumOfEvents)
            .put(queryMaxNumOfLinks, newMaxNumOfLinks)
            .put(queryMaxNumOfAttributesPerEvent, newMaxNumOfAttributesPerEvent)
            .put(queryMaxNumOfAttributesPerLink, newMaxNumOfAttributesPerLink)
            .build();

    TraceConfigzZPageHandler traceConfigzZPageHandler =
        new TraceConfigzZPageHandler(tracerProvider);
    traceConfigzZPageHandler.emitHtml(queryMap, output);

    assertThat(tracerProvider.getActiveTraceConfig().getSampler().getDescription())
        .isEqualTo(
            Samplers.probability(Double.parseDouble(newSamplingProbability)).getDescription());
    assertThat(tracerProvider.getActiveTraceConfig().getMaxNumberOfAttributes())
        .isEqualTo(Integer.parseInt(newMaxNumOfAttributes));
    assertThat(tracerProvider.getActiveTraceConfig().getMaxNumberOfEvents())
        .isEqualTo(Integer.parseInt(newMaxNumOfEvents));
    assertThat(tracerProvider.getActiveTraceConfig().getMaxNumberOfLinks())
        .isEqualTo(Integer.parseInt(newMaxNumOfLinks));
    assertThat(tracerProvider.getActiveTraceConfig().getMaxNumberOfAttributesPerEvent())
        .isEqualTo(Integer.parseInt(newMaxNumOfAttributesPerEvent));
    assertThat(tracerProvider.getActiveTraceConfig().getMaxNumberOfAttributesPerLink())
        .isEqualTo(Integer.parseInt(newMaxNumOfAttributesPerLink));
  }

  @Test
  void appliesChangesCorrectly_restoreDefault() {
    OutputStream output = new ByteArrayOutputStream();

    Map<String, String> queryMap = ImmutableMap.of("action", "default");

    TraceConfigzZPageHandler traceConfigzZPageHandler =
        new TraceConfigzZPageHandler(tracerProvider);
    traceConfigzZPageHandler.emitHtml(queryMap, output);

    assertThat(tracerProvider.getActiveTraceConfig().getSampler().getDescription())
        .isEqualTo(TraceConfig.getDefault().getSampler().getDescription());
    assertThat(tracerProvider.getActiveTraceConfig().getMaxNumberOfAttributes())
        .isEqualTo(TraceConfig.getDefault().getMaxNumberOfAttributes());
    assertThat(tracerProvider.getActiveTraceConfig().getMaxNumberOfEvents())
        .isEqualTo(TraceConfig.getDefault().getMaxNumberOfEvents());
    assertThat(tracerProvider.getActiveTraceConfig().getMaxNumberOfLinks())
        .isEqualTo(TraceConfig.getDefault().getMaxNumberOfLinks());
    assertThat(tracerProvider.getActiveTraceConfig().getMaxNumberOfAttributesPerEvent())
        .isEqualTo(TraceConfig.getDefault().getMaxNumberOfAttributesPerEvent());
    assertThat(tracerProvider.getActiveTraceConfig().getMaxNumberOfAttributesPerLink())
        .isEqualTo(TraceConfig.getDefault().getMaxNumberOfAttributesPerLink());
  }

  @Test
  void appliesChangesCorrectly_doNotCrashOnNullParameters() {
    OutputStream output = new ByteArrayOutputStream();

    Map<String, String> queryMap = ImmutableMap.of("action", "change");

    TraceConfigzZPageHandler traceConfigzZPageHandler =
        new TraceConfigzZPageHandler(tracerProvider);
    traceConfigzZPageHandler.emitHtml(queryMap, output);

    assertThat(tracerProvider.getActiveTraceConfig().getSampler().getDescription())
        .isEqualTo(TraceConfig.getDefault().getSampler().getDescription());
    assertThat(tracerProvider.getActiveTraceConfig().getMaxNumberOfAttributes())
        .isEqualTo(TraceConfig.getDefault().getMaxNumberOfAttributes());
    assertThat(tracerProvider.getActiveTraceConfig().getMaxNumberOfEvents())
        .isEqualTo(TraceConfig.getDefault().getMaxNumberOfEvents());
    assertThat(tracerProvider.getActiveTraceConfig().getMaxNumberOfLinks())
        .isEqualTo(TraceConfig.getDefault().getMaxNumberOfLinks());
    assertThat(tracerProvider.getActiveTraceConfig().getMaxNumberOfAttributesPerEvent())
        .isEqualTo(TraceConfig.getDefault().getMaxNumberOfAttributesPerEvent());
    assertThat(tracerProvider.getActiveTraceConfig().getMaxNumberOfAttributesPerLink())
        .isEqualTo(TraceConfig.getDefault().getMaxNumberOfAttributesPerLink());
  }

  @Test
  void applyChanges_emitErrorOnInvalidInput() {
    // Invalid samplingProbability (not type of double)
    OutputStream output = new ByteArrayOutputStream();
    TraceConfigzZPageHandler traceConfigzZPageHandler =
        new TraceConfigzZPageHandler(tracerProvider);
    Map<String, String> queryMap =
        ImmutableMap.of("action", "change", "samplingprobability", "invalid");

    traceConfigzZPageHandler.emitHtml(queryMap, output);

    assertThat(output.toString()).contains("Error while generating HTML: ");
    assertThat(output.toString()).contains("SamplingProbability must be of the type double");

    // Invalid samplingProbability (< 0)
    output = new ByteArrayOutputStream();
    traceConfigzZPageHandler = new TraceConfigzZPageHandler(tracerProvider);
    queryMap = ImmutableMap.of("action", "change", "samplingprobability", "-1");

    traceConfigzZPageHandler.emitHtml(queryMap, output);

    assertThat(output.toString()).contains("Error while generating HTML: ");
    assertThat(output.toString()).contains("probability must be in range [0.0, 1.0]");

    // Invalid samplingProbability (> 1)
    output = new ByteArrayOutputStream();
    traceConfigzZPageHandler = new TraceConfigzZPageHandler(tracerProvider);
    queryMap = ImmutableMap.of("action", "change", "samplingprobability", "1.1");

    traceConfigzZPageHandler.emitHtml(queryMap, output);

    assertThat(output.toString()).contains("Error while generating HTML: ");
    assertThat(output.toString()).contains("probability must be in range [0.0, 1.0]");

    // Invalid maxNumOfAttributes
    output = new ByteArrayOutputStream();
    traceConfigzZPageHandler = new TraceConfigzZPageHandler(tracerProvider);
    queryMap = ImmutableMap.of("action", "change", "maxnumofattributes", "invalid");

    traceConfigzZPageHandler.emitHtml(queryMap, output);

    assertThat(output.toString()).contains("Error while generating HTML: ");
    assertThat(output.toString()).contains("MaxNumOfAttributes must be of the type integer");

    // Invalid maxNumOfEvents
    output = new ByteArrayOutputStream();
    traceConfigzZPageHandler = new TraceConfigzZPageHandler(tracerProvider);
    queryMap = ImmutableMap.of("action", "change", "maxnumofevents", "invalid");

    traceConfigzZPageHandler.emitHtml(queryMap, output);

    assertThat(output.toString()).contains("Error while generating HTML: ");
    assertThat(output.toString()).contains("MaxNumOfEvents must be of the type integer");

    // Invalid maxNumLinks
    output = new ByteArrayOutputStream();
    traceConfigzZPageHandler = new TraceConfigzZPageHandler(tracerProvider);
    queryMap = ImmutableMap.of("action", "change", "maxnumoflinks", "invalid");

    traceConfigzZPageHandler.emitHtml(queryMap, output);

    assertThat(output.toString()).contains("Error while generating HTML: ");
    assertThat(output.toString()).contains("MaxNumOfLinks must be of the type integer");

    // Invalid maxNumOfAttributesPerEvent
    output = new ByteArrayOutputStream();
    traceConfigzZPageHandler = new TraceConfigzZPageHandler(tracerProvider);
    queryMap = ImmutableMap.of("action", "change", "maxnumofattributesperevent", "invalid");

    traceConfigzZPageHandler.emitHtml(queryMap, output);

    assertThat(output.toString()).contains("Error while generating HTML: ");
    assertThat(output.toString())
        .contains("MaxNumOfAttributesPerEvent must be of the type integer");

    // Invalid maxNumOfAttributesPerLink
    output = new ByteArrayOutputStream();
    traceConfigzZPageHandler = new TraceConfigzZPageHandler(tracerProvider);
    queryMap = ImmutableMap.of("action", "change", "maxnumofattributesperlink", "invalid");

    traceConfigzZPageHandler.emitHtml(queryMap, output);

    assertThat(output.toString()).contains("Error while generating HTML: ");
    assertThat(output.toString()).contains("MaxNumOfAttributesPerLink must be of the type integer");
  }
}
