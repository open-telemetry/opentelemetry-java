package io.opentelemetry.example.metrics;

import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.context.Scope;
import io.opentelemetry.metrics.DoubleCounter;
import io.opentelemetry.metrics.LongCounter;
import io.opentelemetry.metrics.Meter;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Span.Kind;
import io.opentelemetry.trace.Status;
import io.opentelemetry.trace.Tracer;
import java.io.File;
import javax.swing.filechooser.FileSystemView;

/**
 * Example of using {@link DoubleCounter} and {@link DoubleCounter.BoundDoubleCounter} to count disk space used by files with a specific extension.
 */
public class DoubleCounterExample {

  private static final Tracer tracer =
      OpenTelemetry.getTracer("io.opentelemetry.example.metrics");
  private static final Meter sampleMeter = OpenTelemetry.getMeterProvider()
      .get("io.opentelemetry.example.metrics", "0.5");
  private static final File directoryToCountIn = FileSystemView.getFileSystemView().getHomeDirectory();
  private static final DoubleCounter methodCallCounter = sampleMeter
      .doubleCounterBuilder("calculated_used_space")
      .setDescription("Counts disk space used by file extension.")
      .setUnit("MB")
      .build();

  public static void main(String[] args) {
    Span span = tracer.spanBuilder("calculate space")
        .setSpanKind(Kind.INTERNAL)
        .startSpan();
    DoubleCounterExample example = new DoubleCounterExample();
    try (Scope scope = tracer.withSpan(span)) {
      example.calculateSpaceUsedByFilesWithExtension("dll", directoryToCountIn);
    } catch (Exception e) {
      Status status = Status.UNKNOWN.withDescription("Error while calculating used space");
      span.setStatus(status);
    } finally {
      span.end();
    }
  }

  public void calculateSpaceUsedByFilesWithExtension(String extension, File directory) {
    File[] files = directory.listFiles();
    if (files != null) {
      for (File file : files) {
        if (file.getName().endsWith("." + extension)) {
          //we can add values to counter without addition label key-values pairs
          methodCallCounter.add((double) file.length() / 1024);
        }
      }
    }
  }
}
