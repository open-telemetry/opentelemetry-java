# OpenTelemetry metrics export framework

The metrics world split between "pushed based" and "pull based" libraries and backends, and because
of this, the OpenTelemetry export framework needs to address all the possible combinations.

To achieve the support for "pushed based" and "pull based" libraries the OpenTelemetry defines two
interfaces that helps with this:
* MetricProducer - is the interface that a "pull based" library should implement in order to make
data available to OpenTelemetry exporters.
* MetricExporter - is an interface that every OpenTelemetry exporter should implement in order to
allow "push based" libraries to push metrics to the backend.

Here are some examples on how different libraries will interact with pull/push backends.

**Push backend:**

```java
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.metrics.export.MetricProducer;

/**
 * Class to build and register MeterProvider globally. MeterProvider is
 * initialized with PeriodicMetricReader which wraps the MetricsExporter
 * and automatically reads and exports the metrics every export interval.
 */
public final class PushExporter {
  // The number of milliseconds between metric exports.
  private static final long METRIC_EXPORT_INTERVAL_MS = 800L;

  private final PushMetricExporter metricExporter;
  // This will be used to create instruments
  private final SdkMeterProvider meterProvider;

  public PushExporter() {
    metricExporter = new PushMetricExporter();
    // Create an instance of PeriodicMetricReaderFactory and configure it
    // to export via the Metrics exporter
    MetricReaderFactory periodicReaderFactory =
        PeriodicMetricReader.create(
            metricExporter, Duration.ofMillis(METRIC_EXPORT_INTERVAL_MS));
    meterProvider =
        SdkMeterProvider.builder()
            .registerMetricReader(periodicReaderFactory)
            .buildAndRegisterGlobal();
  }

  // Can be accessed by any "push based" library to export metrics.
  public MetricExporter getMetricExporter() {
    return metricExporter;
  }

  // GlobalMeterProvider can also be used to achieve the same.
  public SdkMeterProvider getSdkMeterProvider() {
    return meterProvider;
  }

}
```

**Pull backend:**

```java
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.metrics.export.MetricProducer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Simple implementation of the MetricExporter that stores data in memory and makes them available
 * via MetricProducer interface.
 */
public final class PullMetricExporter implements MetricExporter, MetricProducer {
  private final List<MetricData> metricsBuffer = new ArrayList<>();

  @Override
  synchronized ResultCode export(Collection<MetricData> metrics) {
    metricsBuffer.addAll(metrics);
    return ResultCode.SUCCESS;
  }

  synchronized Collection<MetricData> getAllMetrics() {
    List<MetricData> ret = metricsBuffer;
    metricsBuffer = new ArrayList<>();
    return ret;
  }
}

public final class PullExporter {
  private final PullMetricExporter metricExporter;
  private final Collection<MetricProducer> producers;

  public PushExporter(Collection<MetricProducer> producers) {
    metricExporter = new PullMetricExporter();
    producers = Collections.unmodifiableCollection(new ArrayList<>(producers));
  }

  // Can be accessed by any "push based" library to export metrics.
  public MetricExporter getMetricExporter() {
    return metricExporter;
  }

  private void onPullRequest() {
    // Iterate over all producers and the PullMetricExporter and export all metrics.
    for (MetricProducer metricProducer : producers) {
      Collection<MetricData> metrics = metricProducer.getAllMetrics();
      // Do something with metrics
    }
  }
}
```
