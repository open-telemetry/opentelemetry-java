package io.opentelemetry.sdk.example;

import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.Sampler;
import io.opentelemetry.sdk.trace.Samplers;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.TracerSdk;
import io.opentelemetry.sdk.trace.TracerSdkFactory;
import io.opentelemetry.sdk.trace.config.TraceConfig;
import io.opentelemetry.trace.AttributeValue;
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
			//We just do something when a span ends.
		}

		@Override
		public void onEnd(ReadableSpan span) {
			Map<String, AttributeValue> attrs = span.toSpanData().getAttributes();
			System.out.printf("Span %s has %d attributes: \n", span.getName(), attrs.size());
			for(String key : attrs.keySet()){
				System.out.printf("\t %s : %s\n", key, attrs.get(key).getStringValue());
			}
			System.out.println("-----------------------");
		}

		@Override
		public void shutdown() {

		}
	}

	public static void main(String[] args) {
		// Configure a tracer for these examples
		TracerSdkFactory tracerProvider = OpenTelemetrySdk.getTracerFactory();
		TracerSdk tracer = tracerProvider.get("example");
		tracerProvider.addSpanProcessor(new MyProcessor());

		//TraceConfig handles the global tracing configuration
		TraceConfig config = TraceConfig.getDefault();
		printTraceConfig();

		// We can have 32 Attributes by default. Let's add some to a span and verify they are stored correctly.
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

		// We can also change how the Spans are sampled. For example, we can turn off completely the spans.
		TraceConfig alwaysOff = TraceConfig.getDefault().toBuilder().setSampler(
				Samplers.alwaysOff()
		).build();
		tracerProvider.updateActiveTraceConfig(alwaysOff);
		printTraceConfig();
		tracer.spanBuilder("Not forwarded to any processors").startSpan().end();
		tracer.spanBuilder("Not forwarded to any processors").startSpan().end();
		tracer.spanBuilder("Not forwarded to any processors").startSpan().end();
		tracer.spanBuilder("Not forwarded to any processors").startSpan().end();
		tracer.spanBuilder("Not forwarded to any processors").startSpan().end();

		// We can also implement our own sampler. We need to implement the io.opentelemetry.sdk.trace.Sampler interface.
		class MySampler implements Sampler {

			@Override
			public Decision shouldSample(SpanContext parentContext, TraceId traceId, SpanId spanId, String name, List<Link> parentLinks) {
				// We sample only if the name contains "SAMPLE"
				return new Decision() {

					@Override
					public boolean isSampled() {
						return name.contains("SAMPLE");
					}

					@Override
					public Map<String, AttributeValue> attributes() {
						return Collections.emptyMap();
					}
				};
			}

			@Override
			public String getDescription() {
				return "My Sampler Implementation!";
			}
		}

		// Add MySampler to the Trace Configuration
		TraceConfig mySampler = TraceConfig.getDefault().toBuilder().setSampler(
				new MySampler()
		).build();
		tracerProvider.updateActiveTraceConfig(mySampler);
		printTraceConfig();
		tracer.spanBuilder("#1 - SamPleD").startSpan().end();
		tracer.spanBuilder("#2 - SAMPLED").startSpan().end();
		tracer.spanBuilder("#3 - Smth").startSpan().end();
		tracer.spanBuilder("#4 - SAMPLED this trace will be showed in the console output").startSpan().end();
		tracer.spanBuilder("#5").startSpan().end();
	}

	private static void printTraceConfig() {
		TraceConfig config = OpenTelemetrySdk.getTracerFactory().getActiveTraceConfig();
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