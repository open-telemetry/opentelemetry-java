/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.zpages;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import io.opentelemetry.sdk.trace.config.TraceConfig;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TraceConfigzZPageHandlerTest {
  private static final Map<String, String> emptyQueryMap = ImmutableMap.of();

  private TracezTraceConfigSupplier configSupplier;

  @BeforeEach
  void setup() {
    configSupplier = new TracezTraceConfigSupplier();
  }

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
        new TraceConfigzZPageHandler(configSupplier);
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
        new TraceConfigzZPageHandler(configSupplier);
    traceConfigzZPageHandler.emitHtml(emptyQueryMap, output);

    assertThat(output.toString()).contains("Sampler");
    assertThat(output.toString())
        .contains(">" + configSupplier.get().getSampler().getDescription() + "<");
    assertThat(output.toString()).contains("MaxNumberOfAttributes");
    assertThat(output.toString())
        .contains(">" + Integer.toString(configSupplier.get().getMaxNumberOfAttributes()) + "<");
    assertThat(output.toString()).contains("MaxNumberOfEvents");
    assertThat(output.toString())
        .contains(">" + Integer.toString(configSupplier.get().getMaxNumberOfEvents()) + "<");
    assertThat(output.toString()).contains("MaxNumberOfLinks");
    assertThat(output.toString())
        .contains(">" + Integer.toString(configSupplier.get().getMaxNumberOfLinks()) + "<");
    assertThat(output.toString()).contains("MaxNumberOfAttributesPerEvent");
    assertThat(output.toString())
        .contains(
            ">" + Integer.toString(configSupplier.get().getMaxNumberOfAttributesPerEvent()) + "<");
    assertThat(output.toString()).contains("MaxNumberOfAttributesPerLink");
    assertThat(output.toString())
        .contains(
            ">" + Integer.toString(configSupplier.get().getMaxNumberOfAttributesPerLink()) + "<");
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
        new TraceConfigzZPageHandler(configSupplier);
    traceConfigzZPageHandler.processRequest("POST", queryMap, output);
    traceConfigzZPageHandler.emitHtml(queryMap, output);

    assertThat(configSupplier.get().getSampler().getDescription())
        .isEqualTo(
            Sampler.traceIdRatioBased(Double.parseDouble(newSamplingProbability)).getDescription());
    assertThat(configSupplier.get().getMaxNumberOfAttributes())
        .isEqualTo(Integer.parseInt(newMaxNumOfAttributes));
    assertThat(configSupplier.get().getMaxNumberOfEvents())
        .isEqualTo(Integer.parseInt(newMaxNumOfEvents));
    assertThat(configSupplier.get().getMaxNumberOfLinks())
        .isEqualTo(Integer.parseInt(newMaxNumOfLinks));
    assertThat(configSupplier.get().getMaxNumberOfAttributesPerEvent())
        .isEqualTo(Integer.parseInt(newMaxNumOfAttributesPerEvent));
    assertThat(configSupplier.get().getMaxNumberOfAttributesPerLink())
        .isEqualTo(Integer.parseInt(newMaxNumOfAttributesPerLink));
  }

  @Test
  void appliesChangesCorrectly_restoreDefault() {
    OutputStream output = new ByteArrayOutputStream();

    Map<String, String> queryMap = ImmutableMap.of("action", "default");

    TraceConfigzZPageHandler traceConfigzZPageHandler =
        new TraceConfigzZPageHandler(configSupplier);
    traceConfigzZPageHandler.processRequest("POST", queryMap, output);
    traceConfigzZPageHandler.emitHtml(queryMap, output);

    assertThat(configSupplier.get().getSampler().getDescription())
        .isEqualTo(TraceConfig.getDefault().getSampler().getDescription());
    assertThat(configSupplier.get().getMaxNumberOfAttributes())
        .isEqualTo(TraceConfig.getDefault().getMaxNumberOfAttributes());
    assertThat(configSupplier.get().getMaxNumberOfEvents())
        .isEqualTo(TraceConfig.getDefault().getMaxNumberOfEvents());
    assertThat(configSupplier.get().getMaxNumberOfLinks())
        .isEqualTo(TraceConfig.getDefault().getMaxNumberOfLinks());
    assertThat(configSupplier.get().getMaxNumberOfAttributesPerEvent())
        .isEqualTo(TraceConfig.getDefault().getMaxNumberOfAttributesPerEvent());
    assertThat(configSupplier.get().getMaxNumberOfAttributesPerLink())
        .isEqualTo(TraceConfig.getDefault().getMaxNumberOfAttributesPerLink());
  }

  @Test
  void appliesChangesCorrectly_doNotCrashOnNullParameters() {
    OutputStream output = new ByteArrayOutputStream();

    Map<String, String> queryMap = ImmutableMap.of("action", "change");

    TraceConfigzZPageHandler traceConfigzZPageHandler =
        new TraceConfigzZPageHandler(configSupplier);
    traceConfigzZPageHandler.processRequest("POST", queryMap, output);
    traceConfigzZPageHandler.emitHtml(queryMap, output);

    assertThat(configSupplier.get().getSampler().getDescription())
        .isEqualTo(TraceConfig.getDefault().getSampler().getDescription());
    assertThat(configSupplier.get().getMaxNumberOfAttributes())
        .isEqualTo(TraceConfig.getDefault().getMaxNumberOfAttributes());
    assertThat(configSupplier.get().getMaxNumberOfEvents())
        .isEqualTo(TraceConfig.getDefault().getMaxNumberOfEvents());
    assertThat(configSupplier.get().getMaxNumberOfLinks())
        .isEqualTo(TraceConfig.getDefault().getMaxNumberOfLinks());
    assertThat(configSupplier.get().getMaxNumberOfAttributesPerEvent())
        .isEqualTo(TraceConfig.getDefault().getMaxNumberOfAttributesPerEvent());
    assertThat(configSupplier.get().getMaxNumberOfAttributesPerLink())
        .isEqualTo(TraceConfig.getDefault().getMaxNumberOfAttributesPerLink());
  }

  @Test
  void applyChanges_emitErrorOnInvalidInput() {
    // Invalid samplingProbability (not type of double)
    OutputStream output = new ByteArrayOutputStream();
    TraceConfigzZPageHandler traceConfigzZPageHandler =
        new TraceConfigzZPageHandler(configSupplier);
    Map<String, String> queryMap =
        ImmutableMap.of("action", "change", "samplingprobability", "invalid");

    traceConfigzZPageHandler.processRequest("POST", queryMap, output);

    assertThat(output.toString()).contains("Error while applying trace config changes: ");
    assertThat(output.toString()).contains("SamplingProbability must be of the type double");

    // Invalid samplingProbability (< 0)
    output = new ByteArrayOutputStream();
    traceConfigzZPageHandler = new TraceConfigzZPageHandler(configSupplier);
    queryMap = ImmutableMap.of("action", "change", "samplingprobability", "-1");

    traceConfigzZPageHandler.processRequest("POST", queryMap, output);

    assertThat(output.toString()).contains("Error while applying trace config changes: ");
    assertThat(output.toString()).contains("ratio must be in range [0.0, 1.0]");

    // Invalid samplingProbability (> 1)
    output = new ByteArrayOutputStream();
    traceConfigzZPageHandler = new TraceConfigzZPageHandler(configSupplier);
    queryMap = ImmutableMap.of("action", "change", "samplingprobability", "1.1");

    traceConfigzZPageHandler.processRequest("POST", queryMap, output);

    assertThat(output.toString()).contains("Error while applying trace config changes: ");
    assertThat(output.toString()).contains("ratio must be in range [0.0, 1.0]");

    // Invalid maxNumOfAttributes
    output = new ByteArrayOutputStream();
    traceConfigzZPageHandler = new TraceConfigzZPageHandler(configSupplier);
    queryMap = ImmutableMap.of("action", "change", "maxnumofattributes", "invalid");

    traceConfigzZPageHandler.processRequest("POST", queryMap, output);

    assertThat(output.toString()).contains("Error while applying trace config changes: ");
    assertThat(output.toString()).contains("MaxNumOfAttributes must be of the type integer");

    // Invalid maxNumOfEvents
    output = new ByteArrayOutputStream();
    traceConfigzZPageHandler = new TraceConfigzZPageHandler(configSupplier);
    queryMap = ImmutableMap.of("action", "change", "maxnumofevents", "invalid");

    traceConfigzZPageHandler.processRequest("POST", queryMap, output);

    assertThat(output.toString()).contains("Error while applying trace config changes: ");
    assertThat(output.toString()).contains("MaxNumOfEvents must be of the type integer");

    // Invalid maxNumLinks
    output = new ByteArrayOutputStream();
    traceConfigzZPageHandler = new TraceConfigzZPageHandler(configSupplier);
    queryMap = ImmutableMap.of("action", "change", "maxnumoflinks", "invalid");

    traceConfigzZPageHandler.processRequest("POST", queryMap, output);

    assertThat(output.toString()).contains("Error while applying trace config changes: ");
    assertThat(output.toString()).contains("MaxNumOfLinks must be of the type integer");

    // Invalid maxNumOfAttributesPerEvent
    output = new ByteArrayOutputStream();
    traceConfigzZPageHandler = new TraceConfigzZPageHandler(configSupplier);
    queryMap = ImmutableMap.of("action", "change", "maxnumofattributesperevent", "invalid");

    traceConfigzZPageHandler.processRequest("POST", queryMap, output);

    assertThat(output.toString()).contains("Error while applying trace config changes: ");
    assertThat(output.toString())
        .contains("MaxNumOfAttributesPerEvent must be of the type integer");

    // Invalid maxNumOfAttributesPerLink
    output = new ByteArrayOutputStream();
    traceConfigzZPageHandler = new TraceConfigzZPageHandler(configSupplier);
    queryMap = ImmutableMap.of("action", "change", "maxnumofattributesperlink", "invalid");

    traceConfigzZPageHandler.processRequest("POST", queryMap, output);

    assertThat(output.toString()).contains("Error while applying trace config changes: ");
    assertThat(output.toString()).contains("MaxNumOfAttributesPerLink must be of the type integer");
  }

  @Test
  void applyChanges_shouldNotUpdateOnGetRequest() {
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

    // Apply new config
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
        new TraceConfigzZPageHandler(configSupplier);

    // GET request, Should not apply changes
    traceConfigzZPageHandler.emitHtml(queryMap, output);

    assertThat(configSupplier.get().getSampler().getDescription())
        .isEqualTo(TraceConfig.getDefault().getSampler().getDescription());
    assertThat(configSupplier.get().getMaxNumberOfAttributes())
        .isEqualTo(TraceConfig.getDefault().getMaxNumberOfAttributes());
    assertThat(configSupplier.get().getMaxNumberOfEvents())
        .isEqualTo(TraceConfig.getDefault().getMaxNumberOfEvents());
    assertThat(configSupplier.get().getMaxNumberOfLinks())
        .isEqualTo(TraceConfig.getDefault().getMaxNumberOfLinks());
    assertThat(configSupplier.get().getMaxNumberOfAttributesPerEvent())
        .isEqualTo(TraceConfig.getDefault().getMaxNumberOfAttributesPerEvent());
    assertThat(configSupplier.get().getMaxNumberOfAttributesPerLink())
        .isEqualTo(TraceConfig.getDefault().getMaxNumberOfAttributesPerLink());

    // POST request, Should apply changes
    traceConfigzZPageHandler.processRequest("POST", queryMap, output);
    traceConfigzZPageHandler.emitHtml(queryMap, output);

    assertThat(configSupplier.get().getSampler().getDescription())
        .isEqualTo(
            Sampler.traceIdRatioBased(Double.parseDouble(newSamplingProbability)).getDescription());
    assertThat(configSupplier.get().getMaxNumberOfAttributes())
        .isEqualTo(Integer.parseInt(newMaxNumOfAttributes));
    assertThat(configSupplier.get().getMaxNumberOfEvents())
        .isEqualTo(Integer.parseInt(newMaxNumOfEvents));
    assertThat(configSupplier.get().getMaxNumberOfLinks())
        .isEqualTo(Integer.parseInt(newMaxNumOfLinks));
    assertThat(configSupplier.get().getMaxNumberOfAttributesPerEvent())
        .isEqualTo(Integer.parseInt(newMaxNumOfAttributesPerEvent));
    assertThat(configSupplier.get().getMaxNumberOfAttributesPerLink())
        .isEqualTo(Integer.parseInt(newMaxNumOfAttributesPerLink));
  }
}
