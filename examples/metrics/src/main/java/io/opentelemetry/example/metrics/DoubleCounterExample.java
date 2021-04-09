package io.opentelemetry.example.metrics;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.metrics.DoubleCounter;
import io.opentelemetry.api.metrics.GlobalMeterProvider;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.common.Labels;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.filechooser.FileSystemView;

/**
 * Example of using {@link DoubleCounter} to count disk space used by files with specific
 * extensions.
 */
public final class DoubleCounterExample {
  private static final OpenTelemetry openTelemetry = GlobalOpenTelemetry.get();
  private static final Tracer tracer =
      openTelemetry.getTracer("io.opentelemetry.example.metrics", "0.13.1");
  private static final Meter sampleMeter =
      GlobalMeterProvider.get().get("io.opentelemetry.example.metrics", "0.13.1");
  private static final File directoryToCountIn =
      FileSystemView.getFileSystemView().getHomeDirectory();
  private static final DoubleCounter diskSpaceCounter =
      sampleMeter
          .doubleCounterBuilder("calculated_used_space")
          .setDescription("Counts disk space used by file extension.")
          .setUnit("MB")
          .build();

  public static void main(String[] args) {
    Span span = tracer.spanBuilder("calculate space").setSpanKind(SpanKind.INTERNAL).startSpan();
    DoubleCounterExample example = new DoubleCounterExample();
    try (Scope scope = span.makeCurrent()) {
      List<String> extensionsToFind = new ArrayList<>();
      extensionsToFind.add("dll");
      extensionsToFind.add("png");
      extensionsToFind.add("exe");
      example.calculateSpaceUsedByFilesWithExtension(extensionsToFind, directoryToCountIn);
    } catch (Exception e) {
      span.setStatus(StatusCode.ERROR, "Error while calculating used space");
    } finally {
      span.end();
    }
  }

  public void calculateSpaceUsedByFilesWithExtension(List<String> extensions, File directory) {
    File[] files = directory.listFiles();
    if (files != null) {
      for (File file : files) {
        for (String extension : extensions) {
          if (file.getName().endsWith("." + extension)) {
            // we can add values to the counter for specific labels
            // the label key is "file_extension", its value is the name of the extension
            diskSpaceCounter.add(
                (double) file.length() / 1_000_000, Labels.of("file_extension", extension));
          }
        }
      }
    }
  }
}
