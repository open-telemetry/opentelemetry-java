/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.example;

import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.common.ReadableAttributes;
import io.opentelemetry.exporters.logging.LoggingSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.Sampler;
import io.opentelemetry.sdk.trace.Samplers;
import io.opentelemetry.sdk.trace.TracerSdkManagement;
import io.opentelemetry.sdk.trace.config.TraceConfig;
import io.opentelemetry.sdk.trace.data.SpanData.Link;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Span.Kind;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.Tracer;
import java.util.List;

class ConfigureTraceExample {

  // Configure a tracer for these examples
  static TracerSdkManagement tracerProvider = OpenTelemetrySdk.getTracerManagement();
  static Tracer tracer = OpenTelemetry.getTracer("ConfigureTraceExample");

  static {
    tracerProvider.addSpanProcessor(
        SimpleSpanProcessor.newBuilder(new LoggingSpanExporter()).build());
  }

  public static void main(String[] args) {

    // TraceConfig handles the global tracing configuration
    printTraceConfig();

    // OpenTelemetry has a maximum of 32 Attributes by default for Spans, Links, and Events.
    Span multiAttrSpan = tracer.spanBuilder("Example Span Attributes").startSpan();
    multiAttrSpan.setAttribute("Attribute 1", "first attribute value");
    multiAttrSpan.setAttribute("Attribute 2", "second attribute value");
    multiAttrSpan.end();

    // The configuration can be changed in the trace provider.
    // For example, we can change the maximum number of Attributes per span to 1.
    TraceConfig newConf =
        tracerProvider.getActiveTraceConfig().toBuilder().setMaxNumberOfAttributes(1).build();
    tracerProvider.updateActiveTraceConfig(newConf);
    printTraceConfig();

    // If more attributes than allowed by the configuration are set, they are dropped.
    Span singleAttrSpan = tracer.spanBuilder("Example Span Attributes").startSpan();
    singleAttrSpan.setAttribute("Attribute 1", "first attribute value");
    singleAttrSpan.setAttribute("Attribute 2", "second attribute value");
    singleAttrSpan.end();

    // OpenTelemetry offers three different default samplers:
    //  - alwaysOn: it samples all traces
    //  - alwaysOff: it rejects all traces
    //  - probability: it samples traces based on the probability passed in input
    TraceConfig alwaysOff =
        tracerProvider.getActiveTraceConfig().toBuilder().setSampler(Samplers.alwaysOff()).build();
    TraceConfig alwaysOn =
        tracerProvider.getActiveTraceConfig().toBuilder().setSampler(Samplers.alwaysOn()).build();
    TraceConfig probability =
        tracerProvider.getActiveTraceConfig().toBuilder()
            .setSampler(Samplers.traceIdRatioBased(0.5))
            .build();

    // We update the configuration to use the alwaysOff sampler.
    tracerProvider.updateActiveTraceConfig(alwaysOff);
    printTraceConfig();
    tracer.spanBuilder("Not forwarded to any processors").startSpan().end();
    tracer.spanBuilder("Not forwarded to any processors").startSpan().end();

    // We update the configuration to use the alwaysOn sampler.
    tracerProvider.updateActiveTraceConfig(alwaysOn);
    printTraceConfig();
    tracer.spanBuilder("Forwarded to all processors").startSpan().end();
    tracer.spanBuilder("Forwarded to all processors").startSpan().end();

    // We update the configuration to use the probability sampler which was configured to sample
    // only 50% of the spans.
    tracerProvider.updateActiveTraceConfig(probability);
    printTraceConfig();
    for (int i = 0; i < 10; i++) {
      tracer
          .spanBuilder(String.format("Span %d might be forwarded to all processors", i))
          .startSpan()
          .end();
    }

    // We can also implement our own sampler. We need to implement the
    // io.opentelemetry.sdk.trace.Sampler interface.
    class MySampler implements Sampler {

      @Override
      public SamplingResult shouldSample(
          SpanContext parentContext,
          String traceId,
          String name,
          Kind spanKind,
          ReadableAttributes attributes,
          List<Link> parentLinks) {
        return Samplers.emptySamplingResult(
            name.contains("SAMPLE") ? Decision.RECORD_AND_SAMPLE : Decision.DROP);
      }

      @Override
      public String getDescription() {
        return "My Sampler Implementation!";
      }
    }

    // Add MySampler to the Trace Configuration
    TraceConfig mySampler =
        tracerProvider.getActiveTraceConfig().toBuilder().setSampler(new MySampler()).build();
    tracerProvider.updateActiveTraceConfig(mySampler);
    printTraceConfig();

    tracer.spanBuilder("#1 - SamPleD").startSpan().end();
    tracer
        .spanBuilder("#2 - SAMPLE this trace will be the first to be printed in the console output")
        .startSpan()
        .end();
    tracer.spanBuilder("#3 - Smth").startSpan().end();
    tracer
        .spanBuilder("#4 - SAMPLED this trace will be the second one shown in the console output")
        .startSpan()
        .end();
    tracer.spanBuilder("#5").startSpan().end();

    // Example's over! We can release the resources of OpenTelemetry calling the shutdown method.
    OpenTelemetrySdk.getTracerManagement().shutdown();
  }

  private static void printTraceConfig() {
    TraceConfig config = tracerProvider.getActiveTraceConfig();
    System.err.println("==================================");
    System.err.print("Max number of attributes: ");
    System.err.println(config.getMaxNumberOfAttributes());
    System.err.print("Max number of attributes per event: ");
    System.err.println(config.getMaxNumberOfAttributesPerEvent());
    System.err.print("Max number of attributes per link: ");
    System.err.println(config.getMaxNumberOfAttributesPerLink());
    System.err.print("Max number of events: ");
    System.err.println(config.getMaxNumberOfEvents());
    System.err.print("Max number of links: ");
    System.err.println(config.getMaxNumberOfLinks());
    System.err.print("Sampler: ");
    System.err.println(config.getSampler().getDescription());
  }
}
