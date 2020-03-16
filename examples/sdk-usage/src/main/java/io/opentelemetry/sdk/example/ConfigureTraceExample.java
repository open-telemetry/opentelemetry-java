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

package io.opentelemetry.sdk.example;

import io.opentelemetry.common.AttributeValue;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.Sampler;
import io.opentelemetry.sdk.trace.Samplers;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.TracerSdk;
import io.opentelemetry.sdk.trace.TracerSdkProvider;
import io.opentelemetry.sdk.trace.config.TraceConfig;
import io.opentelemetry.trace.Link;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.TraceId;
import java.util.Collections;
import java.util.List;
import java.util.Map;

class ConfigureTraceExample {

  // Class that showcases how to implement your own SpanProcessor
  private static class MyProcessor implements SpanProcessor {

    @Override
    public void onStart(ReadableSpan span) {
      // We just do something when a span ends.
    }

    @Override
    public boolean isStartRequired() {
      // We don't need onStart() events in this example.
      return false;
    }

    @Override
    public void onEnd(ReadableSpan span) {
      Map<String, AttributeValue> attrs = span.toSpanData().getAttributes();
      System.out.printf("Span %s has %d attributes: \n", span.getName(), attrs.size());
      for (String key : attrs.keySet()) {
        System.out.printf("\t %s : %s\n", key, attrs.get(key).getStringValue());
      }
      System.out.println("-----------------------");
    }

    @Override
    public boolean isEndRequired() {
      // onEnd() events are required by this example.
      return true;
    }

    @Override
    public void shutdown() {}

    @Override
    public void forceFlush() {}
  }

  public static void main(String[] args) {
    // Configure a tracer for these examples
    TracerSdkProvider tracerProvider = OpenTelemetrySdk.getTracerProvider();
    TracerSdk tracer = tracerProvider.get("example");
    tracerProvider.addSpanProcessor(new MyProcessor());

    // TraceConfig handles the global tracing configuration
    TraceConfig config = TraceConfig.getDefault();
    printTraceConfig();

    // We can have a maximum of 32 Attributes by default. Let's add some to a span and verify they
    // are stored
    // correctly.
    Span multiAttrSpan = tracer.spanBuilder("Example Span Attributes").startSpan();
    multiAttrSpan.setAttribute("Attribute 1", "first attribute value");
    multiAttrSpan.setAttribute("Attribute 2", "second attribute value");
    multiAttrSpan.end();

    // The configuration can be changed in the trace provider
    // For example, we can change the maximum number of Attributes per span to 1
    TraceConfig newConf = config.toBuilder().setMaxNumberOfAttributes(1).build();
    tracerProvider.updateActiveTraceConfig(newConf);
    printTraceConfig();

    // Let check that now only one attribute is stored in the Span
    Span singleAttrSpan = tracer.spanBuilder("Example Span Attributes").startSpan();
    singleAttrSpan.setAttribute("Attribute 1", "first attribute value");
    singleAttrSpan.setAttribute("Attribute 2", "second attribute value");
    singleAttrSpan.end();

    // For example, we can configure that all spans are dropped using the "always off" sampler.
    TraceConfig alwaysOff =
        TraceConfig.getDefault().toBuilder().setSampler(Samplers.alwaysOff()).build();
    tracerProvider.updateActiveTraceConfig(alwaysOff);
    printTraceConfig();
    tracer.spanBuilder("Not forwarded to any processors").startSpan().end();
    tracer.spanBuilder("Not forwarded to any processors").startSpan().end();
    tracer.spanBuilder("Not forwarded to any processors").startSpan().end();
    tracer.spanBuilder("Not forwarded to any processors").startSpan().end();
    tracer.spanBuilder("Not forwarded to any processors").startSpan().end();

    // We can also implement our own sampler. We need to implement the
    // io.opentelemetry.sdk.trace.Sampler interface.
    class MySampler implements Sampler {

      @Override
      public Decision shouldSample(
          SpanContext parentContext,
          TraceId traceId,
          SpanId spanId,
          String name,
          Span.Kind spanKind,
          Map<String, AttributeValue> attributes,
          List<Link> parentLinks) {
        // We sample only if the name contains "SAMPLE"
        return new Decision() {

          @Override
          public boolean isSampled() {
            return name.contains("SAMPLE");
          }

          @Override
          public Map<String, AttributeValue> attributes() {
            // This method MUST return an immutable list of Attributes
            // that will be added to the generated Span.
            return Collections.unmodifiableMap(Collections.emptyMap());
          }
        };
      }

      @Override
      public String getDescription() {
        return "My Sampler Implementation!";
      }
    }

    // Add MySampler to the Trace Configuration
    TraceConfig mySampler =
        TraceConfig.getDefault().toBuilder().setSampler(new MySampler()).build();
    tracerProvider.updateActiveTraceConfig(mySampler);
    printTraceConfig();
    tracer.spanBuilder("#1 - SamPleD").startSpan().end();
    tracer.spanBuilder("#2 - SAMPLED").startSpan().end();
    tracer.spanBuilder("#3 - Smth").startSpan().end();
    tracer
        .spanBuilder("#4 - SAMPLED this trace will be shown in the console output")
        .startSpan()
        .end();
    tracer.spanBuilder("#5").startSpan().end();
  }

  private static void printTraceConfig() {
    TraceConfig config = OpenTelemetrySdk.getTracerProvider().getActiveTraceConfig();
    System.out.print("Max number of attributes: ");
    System.out.println(config.getMaxNumberOfAttributes());
    System.out.print("Max number of attributes per event: ");
    System.out.println(config.getMaxNumberOfAttributesPerEvent());
    System.out.print("Max number of attributes per link: ");
    System.out.println(config.getMaxNumberOfAttributesPerLink());
    System.out.print("Max number of events: ");
    System.out.println(config.getMaxNumberOfEvents());
    System.out.print("Max number of links: ");
    System.out.println(config.getMaxNumberOfLinks());
    System.out.print("Sampler: ");
    System.out.println(config.getSampler().getDescription());
    System.out.println("==================================");
  }
}
