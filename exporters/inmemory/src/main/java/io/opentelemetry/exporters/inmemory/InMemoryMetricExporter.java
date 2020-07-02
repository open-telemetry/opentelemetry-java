package io.opentelemetry.exporters.inmemory;

import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * A {@link MetricExporter} implementation that can be used to test OpenTelemetry integration.
 */
public class InMemoryMetricExporter implements MetricExporter {

  // using LinkedBlockingQueue to avoid manual locks for thread-safe operations
  private final Queue<MetricData> finishedMetricItems = new LinkedBlockingQueue<MetricData>();
  private boolean isStopped = false;

  private InMemoryMetricExporter() {}

  /**
   * Returns a new instance of the {@code InMemoryMetricExporter}.
   *
   * @return a new instance of the {@code InMemoryMetricExporter}.
   */
  public static InMemoryMetricExporter create() {
    return new InMemoryMetricExporter();
  }

  /**
   * Returns a {@code List} of the finished {@code Metric}s, represented by {@code MetricData}.
   *
   * @return a {@code List} of the finished {@code Metric}s.
   */
  public List<MetricData> getFinishedMetricItems() {
    return Collections.unmodifiableList(new ArrayList<>(finishedMetricItems));
  }

  /**
   * Clears the internal {@code List} of finished {@code Metric}s.
   *
   * <p>Does not reset the state of this exporter if already shutdown.
   */
  public void reset() {
    finishedMetricItems.clear();
  }

  /**
   * Exports the collection of {@code Metric}s into the inmemory queue.
   * If this is called after {@code shutdown}, this will return {@code ResultCode.FAILURE}.
   */
  @Override
  public ResultCode export(Collection<MetricData> metrics) {
    if (isStopped) {
      return ResultCode.FAILURE;
    }
    finishedMetricItems.addAll(metrics);
    return ResultCode.SUCCESS;
  }

  /**
   * The InMemory exporter does not batch metrics, so this method will immediately return with
   * success.
   *
   * @return always Success
   */
  @Override
  public ResultCode flush() {
    return ResultCode.SUCCESS;
  }

  /**
   * Clears the internal {@code List} of finished {@code Metric}s.
   * Any subsequent call to export() function on this MetricExporter, will return {@code ResultCode.FAILURE}
   */
  @Override
  public void shutdown() {
    isStopped = true;
    finishedMetricItems.clear();
  }
}
