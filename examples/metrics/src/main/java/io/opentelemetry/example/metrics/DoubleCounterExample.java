package io.opentelemetry.example.metrics;

import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.common.Labels;
import io.opentelemetry.context.Scope;
import io.opentelemetry.metrics.DoubleCounter;
import io.opentelemetry.metrics.Meter;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Span.Kind;
import io.opentelemetry.trace.Status;
import io.opentelemetry.trace.Tracer;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.filechooser.FileSystemView;

/**
 * Example of using {@link DoubleCounter} to count disk space used by files with specific
 * extensions.
 */
public class DoubleCounterExample {

  private static final Tracer tracer =
      OpenTelemetry.getTracer("io.opentelemetry.example.metrics", "0.5");
  private static final Meter sampleMeter =
      OpenTelemetry.getMeterProvider().get("io.opentelemetry.example.metrics", "0.5");
  private static final File directoryToCountIn =
      FileSystemView.getFileSystemView().getHomeDirectory();
  private static final DoubleCounter diskSpaceCounter =
      sampleMeter
          .doubleCounterBuilder("calculated_used_space")
          .setDescription("Counts disk space used by file extension.")
          .setUnit("MB")
          .build();

  public static void main(String[] args) {
    Span span = tracer.spanBuilder("calculate space").setSpanKind(Kind.INTERNAL).startSpan();
    DoubleCounterExample example = new DoubleCounterExample();
    try (Scope scope = tracer.withSpan(span)) {
      List<String> extensionsToFind = new ArrayList<>();
      extensionsToFind.add("dll");
      extensionsToFind.add("png");
      extensionsToFind.add("exe");
      example.calculateSpaceUsedByFilesWithExtension(extensionsToFind, directoryToCountIn);
    } catch (Exception e) {
      Status status = Status.UNKNOWN.withDescription("Error while calculating used space");
      span.setStatus(status);
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
