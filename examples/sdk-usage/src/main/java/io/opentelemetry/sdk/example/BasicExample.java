package io.opentelemetry.sdk.example;

import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.TracerSdk;
import io.opentelemetry.sdk.trace.TracerSdkFactory;

/**
 * This example shows how to instantiate an OpenTelemetry tracer.
 */
class BasicExample {

	public static void main(String[] args) throws InterruptedException {
		// Get the tracer factory. Multiple tracer can be instantiated.
		TracerSdkFactory tracerProvider = OpenTelemetrySdk.getTracerFactory();

		// Tracer MUST have a name and it has an optional version string.
		TracerSdk tracerWithoutVersion = tracerProvider.get("BasicExample");
		TracerSdk tracerWithVersion = tracerProvider.get("BasicExample", "semver:0.2.0");

		// The version is part of the unique tracer name
		TracerSdk tracerWithNewVersion = tracerProvider.get("BasicExample", "semver:0.3.0");
		System.out.print("Are tracers with same name and different version equal? ");
		System.out.println(tracerWithNewVersion.equals(tracerWithVersion));

		// Not having a version is equal to pass the null value
		TracerSdk tracerWithNullVersion = tracerProvider.get("BasicExample", null);
		System.out.print("Is null the same of empty version? ");
		System.out.println(tracerWithNullVersion.equals(tracerWithoutVersion));

		// Tracers are a singleton implementation defined uniquely by name and version.
		final TracerSdk[] tracers = new TracerSdk[2];
		Thread t1 = new Thread(() -> tracers[0] = tracerProvider.get("Thread"));
		t1.start();
		Thread t2 = new Thread(() -> tracers[1] = tracerProvider.get("Thread"));
		t2.start();
		t1.join();
		t2.join();

		System.out.print("Is the tracer shared between the threads? ");
		System.out.println(tracers[0] == tracers[1]);

	}

}