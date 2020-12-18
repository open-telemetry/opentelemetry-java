package io.opentelemetry.example.metrics;

import io.opentelemetry.api.DefaultOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Labels;
import io.opentelemetry.api.metrics.GlobalMetricsProvider;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.LongCounter.BoundLongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Span.Kind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import java.io.File;
import javax.swing.filechooser.FileSystemView;

/**
 * Example of using {@link LongCounter} and {@link LongCounter.BoundLongCounter} to count searched
 * directories.
 */
public class LongCounterExample {
  private static final OpenTelemetry openTelemetry = DefaultOpenTelemetry.builder().build();
  private static final Tracer tracer =
      openTelemetry.getTracer("io.opentelemetry.example.metrics", "0.13.1");

  private static final Meter sampleMeter =
      GlobalMetricsProvider.getMeter("io.opentelemetry.example.metrics", "0.13.1");
  private static final LongCounter directoryCounter =
      sampleMeter
          .longCounterBuilder("directories_search_count")
          .setDescription("Counts directories accessed while searching for files.")
          .setUnit("unit")
          .build();
  private static final File homeDirectory = FileSystemView.getFileSystemView().getHomeDirectory();
  // we can use BoundCounters to not specify labels each time
  private static final BoundLongCounter homeDirectoryCounter =
      directoryCounter.bind(Labels.of("root directory", homeDirectory.getName()));

  public static void main(String[] args) {
    Span span = tracer.spanBuilder("workflow").setSpanKind(Kind.INTERNAL).startSpan();
    LongCounterExample example = new LongCounterExample();
    try (Scope scope = span.makeCurrent()) {
      homeDirectoryCounter.add(1); // count root directory
      example.findFile("file_to_find.txt", homeDirectory);
    } catch (Exception e) {
      span.setStatus(StatusCode.ERROR, "Error while finding file");
    } finally {
      span.end();
    }
  }

  public void findFile(String name, File directory) {
    File[] files = directory.listFiles();
    System.out.println("Currently looking at " + directory.getAbsolutePath());
    if (files != null) {
      for (File file : files) {
        if (file.isDirectory()) {
          // we don't have to specify the value for the "root directory" label again
          // since this is a BoundLongCounter with pre-set labels
          homeDirectoryCounter.add(1);
          findFile(name, file);
        } else if (name.equalsIgnoreCase(file.getName())) {
          System.out.println(file.getParentFile());
        }
      }
    }
  }
}
