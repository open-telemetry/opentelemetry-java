package io.opentelemetry.example.metrics;

import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.context.Scope;
import io.opentelemetry.metrics.LongCounter;
import io.opentelemetry.metrics.LongCounter.BoundLongCounter;
import io.opentelemetry.metrics.Meter;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Span.Kind;
import io.opentelemetry.trace.Tracer;
import java.io.File;
import javax.swing.filechooser.FileSystemView;

/**
 * Example to search all directories for specific file.
 * Here we will search directory recursively for specific file, using meter to count searched directories.
 */
public class LongCounterExample {

  private static final Tracer tracer =
      OpenTelemetry.getTracer("io.opentelemetry.example.metrics");
  private static final Meter sampleMeter = OpenTelemetry.getMeterProvider()
      .get("io.opentelemetry.example.metrics", "0.5");
  private static final LongCounter methodCallCounter = sampleMeter
      .longCounterBuilder("directories_search_count")
      .setDescription("should count directories searched")
      .setUnit("unit")
      .build();
  //we can use BoundCounters to not specify labels each time
  private static final BoundLongCounter directoryCounter = methodCallCounter
      .bind("directory", "searched");

  public static void main(String[] args) {
    Span span = tracer.spanBuilder("workflow")
        .setSpanKind(Kind.INTERNAL)
        .startSpan();
    LongCounterExample example = new LongCounterExample();
    try (Scope scope = tracer.withSpan(span)) {
      directoryCounter.add(1);// count root directory
      example.findFile("file_to_find.txt", FileSystemView.getFileSystemView().getHomeDirectory());
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
          directoryCounter.add(1);
          findFile(name, file);
        } else if (name.equalsIgnoreCase(file.getName())) {
          System.out.println(file.getParentFile());
        }
      }
    }
  }
}
