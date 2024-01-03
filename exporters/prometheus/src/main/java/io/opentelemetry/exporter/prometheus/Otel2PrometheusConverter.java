/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.prometheus;

import static io.prometheus.metrics.model.snapshots.PrometheusNaming.sanitizeLabelName;
import static io.prometheus.metrics.model.snapshots.PrometheusNaming.sanitizeMetricName;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.internal.ThrottlingLogger;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.DoubleExemplarData;
import io.opentelemetry.sdk.metrics.data.DoublePointData;
import io.opentelemetry.sdk.metrics.data.ExemplarData;
import io.opentelemetry.sdk.metrics.data.ExponentialHistogramBuckets;
import io.opentelemetry.sdk.metrics.data.ExponentialHistogramData;
import io.opentelemetry.sdk.metrics.data.ExponentialHistogramPointData;
import io.opentelemetry.sdk.metrics.data.HistogramData;
import io.opentelemetry.sdk.metrics.data.HistogramPointData;
import io.opentelemetry.sdk.metrics.data.LongExemplarData;
import io.opentelemetry.sdk.metrics.data.LongPointData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.SumData;
import io.opentelemetry.sdk.metrics.data.SummaryPointData;
import io.opentelemetry.sdk.metrics.data.ValueAtQuantile;
import io.opentelemetry.sdk.resources.Resource;
import io.prometheus.metrics.model.snapshots.ClassicHistogramBuckets;
import io.prometheus.metrics.model.snapshots.CounterSnapshot;
import io.prometheus.metrics.model.snapshots.CounterSnapshot.CounterDataPointSnapshot;
import io.prometheus.metrics.model.snapshots.Exemplar;
import io.prometheus.metrics.model.snapshots.Exemplars;
import io.prometheus.metrics.model.snapshots.GaugeSnapshot;
import io.prometheus.metrics.model.snapshots.GaugeSnapshot.GaugeDataPointSnapshot;
import io.prometheus.metrics.model.snapshots.HistogramSnapshot;
import io.prometheus.metrics.model.snapshots.HistogramSnapshot.HistogramDataPointSnapshot;
import io.prometheus.metrics.model.snapshots.InfoSnapshot;
import io.prometheus.metrics.model.snapshots.InfoSnapshot.InfoDataPointSnapshot;
import io.prometheus.metrics.model.snapshots.Labels;
import io.prometheus.metrics.model.snapshots.MetricMetadata;
import io.prometheus.metrics.model.snapshots.MetricSnapshot;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import io.prometheus.metrics.model.snapshots.NativeHistogramBuckets;
import io.prometheus.metrics.model.snapshots.Quantile;
import io.prometheus.metrics.model.snapshots.Quantiles;
import io.prometheus.metrics.model.snapshots.SummarySnapshot;
import io.prometheus.metrics.model.snapshots.SummarySnapshot.SummaryDataPointSnapshot;
import io.prometheus.metrics.model.snapshots.Unit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

/** Convert OpenTelemetry {@link MetricData} to Prometheus {@link MetricSnapshots}. */
final class Otel2PrometheusConverter {

  private static final Logger LOGGER = Logger.getLogger(Otel2PrometheusConverter.class.getName());
  private static final ThrottlingLogger THROTTLING_LOGGER = new ThrottlingLogger(LOGGER);
  private final boolean otelScopeEnabled;
  private static final String OTEL_SCOPE_NAME = "otel_scope_name";
  private static final String OTEL_SCOPE_VERSION = "otel_scope_version";
  private static final long NANOS_PER_MILLISECOND = TimeUnit.MILLISECONDS.toNanos(1);

  /**
   * Constructor with feature flag parameter.
   *
   * @param otelScopeEnabled enable generation of the OpenTelemetry instrumentation scope info
   *     metric and labels.
   */
  Otel2PrometheusConverter(boolean otelScopeEnabled) {
    this.otelScopeEnabled = otelScopeEnabled;
  }

  MetricSnapshots convert(@Nullable Collection<MetricData> metricDataCollection) {
    if (metricDataCollection == null || metricDataCollection.isEmpty()) {
      return MetricSnapshots.of();
    }
    Map<String, MetricSnapshot> snapshotsByName = new HashMap<>(metricDataCollection.size());
    Resource resource = null;
    Set<InstrumentationScopeInfo> scopes = new LinkedHashSet<>();
    for (MetricData metricData : metricDataCollection) {
      MetricSnapshot snapshot = convert(metricData);
      if (snapshot == null) {
        continue;
      }
      putOrMerge(snapshotsByName, snapshot);
      if (resource == null) {
        resource = metricData.getResource();
      }
      if (otelScopeEnabled && !metricData.getInstrumentationScopeInfo().getAttributes().isEmpty()) {
        scopes.add(metricData.getInstrumentationScopeInfo());
      }
    }
    if (resource != null) {
      putOrMerge(snapshotsByName, makeTargetInfo(resource));
    }
    if (otelScopeEnabled && !scopes.isEmpty()) {
      putOrMerge(snapshotsByName, makeScopeInfo(scopes));
    }
    return new MetricSnapshots(snapshotsByName.values());
  }

  @Nullable
  private MetricSnapshot convert(MetricData metricData) {

    // Note that AggregationTemporality.DELTA should never happen
    // because PrometheusMetricReader#getAggregationTemporality returns CUMULATIVE.

    MetricMetadata metadata = convertMetadata(metricData);
    InstrumentationScopeInfo scope = metricData.getInstrumentationScopeInfo();
    switch (metricData.getType()) {
      case LONG_GAUGE:
        return convertLongGauge(metadata, scope, metricData.getLongGaugeData().getPoints());
      case DOUBLE_GAUGE:
        return convertDoubleGauge(metadata, scope, metricData.getDoubleGaugeData().getPoints());
      case LONG_SUM:
        SumData<LongPointData> longSumData = metricData.getLongSumData();
        if (longSumData.getAggregationTemporality() == AggregationTemporality.DELTA) {
          return null;
        } else if (longSumData.isMonotonic()) {
          return convertLongCounter(metadata, scope, longSumData.getPoints());
        } else {
          return convertLongGauge(metadata, scope, longSumData.getPoints());
        }
      case DOUBLE_SUM:
        SumData<DoublePointData> doubleSumData = metricData.getDoubleSumData();
        if (doubleSumData.getAggregationTemporality() == AggregationTemporality.DELTA) {
          return null;
        } else if (doubleSumData.isMonotonic()) {
          return convertDoubleCounter(metadata, scope, doubleSumData.getPoints());
        } else {
          return convertDoubleGauge(metadata, scope, doubleSumData.getPoints());
        }
      case HISTOGRAM:
        HistogramData histogramData = metricData.getHistogramData();
        if (histogramData.getAggregationTemporality() == AggregationTemporality.DELTA) {
          return null;
        } else {
          return convertHistogram(metadata, scope, histogramData.getPoints());
        }
      case EXPONENTIAL_HISTOGRAM:
        ExponentialHistogramData exponentialHistogramData =
            metricData.getExponentialHistogramData();
        if (exponentialHistogramData.getAggregationTemporality() == AggregationTemporality.DELTA) {
          return null;
        } else {
          return convertExponentialHistogram(metadata, scope, exponentialHistogramData.getPoints());
        }
      case SUMMARY:
        return convertSummary(metadata, scope, metricData.getSummaryData().getPoints());
    }
    return null;
  }

  private GaugeSnapshot convertLongGauge(
      MetricMetadata metadata,
      InstrumentationScopeInfo scope,
      Collection<LongPointData> dataPoints) {
    List<GaugeDataPointSnapshot> data = new ArrayList<>(dataPoints.size());
    for (LongPointData longData : dataPoints) {
      data.add(
          new GaugeDataPointSnapshot(
              (double) longData.getValue(),
              convertAttributes(scope, longData.getAttributes()),
              convertLongExemplar(longData.getExemplars())));
    }
    return new GaugeSnapshot(metadata, data);
  }

  private CounterSnapshot convertLongCounter(
      MetricMetadata metadata,
      InstrumentationScopeInfo scope,
      Collection<LongPointData> dataPoints) {
    List<CounterDataPointSnapshot> data =
        new ArrayList<CounterDataPointSnapshot>(dataPoints.size());
    for (LongPointData longData : dataPoints) {
      data.add(
          new CounterDataPointSnapshot(
              (double) longData.getValue(),
              convertAttributes(scope, longData.getAttributes()),
              convertLongExemplar(longData.getExemplars()),
              longData.getStartEpochNanos() / NANOS_PER_MILLISECOND));
    }
    return new CounterSnapshot(metadata, data);
  }

  private GaugeSnapshot convertDoubleGauge(
      MetricMetadata metadata,
      InstrumentationScopeInfo scope,
      Collection<DoublePointData> dataPoints) {
    List<GaugeDataPointSnapshot> data = new ArrayList<>(dataPoints.size());
    for (DoublePointData doubleData : dataPoints) {
      data.add(
          new GaugeDataPointSnapshot(
              doubleData.getValue(),
              convertAttributes(scope, doubleData.getAttributes()),
              convertDoubleExemplar(doubleData.getExemplars())));
    }
    return new GaugeSnapshot(metadata, data);
  }

  private CounterSnapshot convertDoubleCounter(
      MetricMetadata metadata,
      InstrumentationScopeInfo scope,
      Collection<DoublePointData> dataPoints) {
    List<CounterDataPointSnapshot> data = new ArrayList<>(dataPoints.size());
    for (DoublePointData doubleData : dataPoints) {
      data.add(
          new CounterDataPointSnapshot(
              doubleData.getValue(),
              convertAttributes(scope, doubleData.getAttributes()),
              convertDoubleExemplar(doubleData.getExemplars()),
              doubleData.getStartEpochNanos() / NANOS_PER_MILLISECOND));
    }
    return new CounterSnapshot(metadata, data);
  }

  private HistogramSnapshot convertHistogram(
      MetricMetadata metadata,
      InstrumentationScopeInfo scope,
      Collection<HistogramPointData> dataPoints) {
    List<HistogramDataPointSnapshot> data = new ArrayList<>(dataPoints.size());
    for (HistogramPointData histogramData : dataPoints) {
      List<Double> boundaries = new ArrayList<>(histogramData.getBoundaries().size() + 1);
      boundaries.addAll(histogramData.getBoundaries());
      boundaries.add(Double.POSITIVE_INFINITY);
      data.add(
          new HistogramDataPointSnapshot(
              ClassicHistogramBuckets.of(boundaries, histogramData.getCounts()),
              histogramData.getSum(),
              convertAttributes(scope, histogramData.getAttributes()),
              convertDoubleExemplars(histogramData.getExemplars()),
              histogramData.getStartEpochNanos() / NANOS_PER_MILLISECOND));
    }
    return new HistogramSnapshot(metadata, data);
  }

  @Nullable
  private HistogramSnapshot convertExponentialHistogram(
      MetricMetadata metadata,
      InstrumentationScopeInfo scope,
      Collection<ExponentialHistogramPointData> dataPoints) {
    List<HistogramDataPointSnapshot> data = new ArrayList<>(dataPoints.size());
    for (ExponentialHistogramPointData histogramData : dataPoints) {
      int scale = histogramData.getScale();
      if (scale < -4) {
        THROTTLING_LOGGER.log(
            Level.WARNING,
            "Dropping histogram "
                + metadata.getName()
                + " with attributes "
                + histogramData.getAttributes()
                + " because it has scale < -4 which is unsupported in Prometheus");
        return null;
      }
      // Scale > 8 are not supported in Prometheus. Histograms with scale > 8 are scaled down to 8.
      int scaleDown = scale > 8 ? scale - 8 : 0;
      data.add(
          new HistogramDataPointSnapshot(
              scale - scaleDown,
              histogramData.getZeroCount(),
              0L,
              convertExponentialHistogramBuckets(histogramData.getPositiveBuckets(), scaleDown),
              convertExponentialHistogramBuckets(histogramData.getNegativeBuckets(), scaleDown),
              histogramData.getSum(),
              convertAttributes(scope, histogramData.getAttributes()),
              convertDoubleExemplars(histogramData.getExemplars()),
              histogramData.getStartEpochNanos() / NANOS_PER_MILLISECOND));
    }
    return new HistogramSnapshot(metadata, data);
  }

  private static NativeHistogramBuckets convertExponentialHistogramBuckets(
      ExponentialHistogramBuckets buckets, int scaleDown) {
    if (buckets.getBucketCounts().isEmpty()) {
      return NativeHistogramBuckets.EMPTY;
    }
    List<Long> otelCounts = buckets.getBucketCounts();
    List<Integer> indexes = new ArrayList<>(otelCounts.size());
    List<Long> counts = new ArrayList<>(otelCounts.size());
    int previousIndex = (buckets.getOffset() >> scaleDown) + 1;
    long count = 0;
    for (int i = 0; i < otelCounts.size(); i++) {
      int index = ((buckets.getOffset() + i) >> scaleDown) + 1;
      if (index > previousIndex) {
        indexes.add(previousIndex);
        counts.add(count);
        previousIndex = index;
        count = 0;
      }
      count += otelCounts.get(i);
    }
    indexes.add(previousIndex);
    counts.add(count);
    return NativeHistogramBuckets.of(indexes, counts);
  }

  private SummarySnapshot convertSummary(
      MetricMetadata metadata,
      InstrumentationScopeInfo scope,
      Collection<SummaryPointData> dataPoints) {
    List<SummaryDataPointSnapshot> data = new ArrayList<>(dataPoints.size());
    for (SummaryPointData summaryData : dataPoints) {
      data.add(
          new SummaryDataPointSnapshot(
              summaryData.getCount(),
              summaryData.getSum(),
              convertQuantiles(summaryData.getValues()),
              convertAttributes(scope, summaryData.getAttributes()),
              Exemplars.EMPTY, // Exemplars for Summaries not implemented yet.
              summaryData.getStartEpochNanos() / NANOS_PER_MILLISECOND));
    }
    return new SummarySnapshot(metadata, data);
  }

  private static Quantiles convertQuantiles(List<ValueAtQuantile> values) {
    List<Quantile> result = new ArrayList<>(values.size());
    for (ValueAtQuantile value : values) {
      result.add(new Quantile(value.getQuantile(), value.getValue()));
    }
    return Quantiles.of(result);
  }

  @Nullable
  private Exemplar convertLongExemplar(List<LongExemplarData> exemplars) {
    if (exemplars.isEmpty()) {
      return null;
    } else {
      LongExemplarData exemplar = exemplars.get(0);
      return convertExemplar((double) exemplar.getValue(), exemplar);
    }
  }

  /** Converts the first exemplar in the list if available, else returns {#code null}. */
  @Nullable
  private Exemplar convertDoubleExemplar(List<DoubleExemplarData> exemplars) {
    if (exemplars.isEmpty()) {
      return null;
    } else {
      DoubleExemplarData exemplar = exemplars.get(0);
      return convertExemplar(exemplar.getValue(), exemplar);
    }
  }

  /** Converts the first exemplar in the list if available, else returns {#code null}. */
  private Exemplars convertDoubleExemplars(List<DoubleExemplarData> exemplars) {
    List<Exemplar> result = new ArrayList<>(exemplars.size());
    for (DoubleExemplarData exemplar : exemplars) {
      result.add(convertExemplar(exemplar.getValue(), exemplar));
    }
    return Exemplars.of(result);
  }

  private Exemplar convertExemplar(double value, ExemplarData exemplar) {
    SpanContext spanContext = exemplar.getSpanContext();
    if (spanContext.isValid()) {
      return new Exemplar(
          value,
          convertAttributes(
              null,
              exemplar.getFilteredAttributes(),
              "trace_id",
              spanContext.getTraceId(),
              "span_id",
              spanContext.getSpanId()),
          exemplar.getEpochNanos() / NANOS_PER_MILLISECOND);
    } else {
      return new Exemplar(
          value,
          convertAttributes(null, exemplar.getFilteredAttributes()),
          exemplar.getEpochNanos() / NANOS_PER_MILLISECOND);
    }
  }

  private InfoSnapshot makeTargetInfo(Resource resource) {
    return new InfoSnapshot(
        new MetricMetadata("target"),
        Collections.singletonList(
            new InfoDataPointSnapshot(convertAttributes(null, resource.getAttributes()))));
  }

  private InfoSnapshot makeScopeInfo(Set<InstrumentationScopeInfo> scopes) {
    List<InfoDataPointSnapshot> prometheusScopeInfos = new ArrayList<>(scopes.size());
    for (InstrumentationScopeInfo scope : scopes) {
      prometheusScopeInfos.add(
          new InfoDataPointSnapshot(convertAttributes(scope, scope.getAttributes())));
    }
    return new InfoSnapshot(new MetricMetadata("otel_scope"), prometheusScopeInfos);
  }

  /**
   * Convert OpenTelemetry attributes to Prometheus labels.
   *
   * @param scope will be converted to {@code otel_scope_*} labels if {@code otelScopeEnabled} is
   *     {@code true}.
   * @param attributes the attributes to be converted.
   * @param additionalAttributes optional list of key/value pairs, may be empty.
   */
  private Labels convertAttributes(
      @Nullable InstrumentationScopeInfo scope,
      Attributes attributes,
      String... additionalAttributes) {
    int numberOfScopeAttributes = 0;
    if (otelScopeEnabled && scope != null) {
      numberOfScopeAttributes = scope.getVersion() == null ? 1 : 2;
    }
    String[] names =
        new String[attributes.size() + numberOfScopeAttributes + additionalAttributes.length / 2];
    String[] values = new String[names.length];
    int[] pos = new int[] {0}; // using an array because we want to increment in a forEach() lambda.
    attributes.forEach(
        (key, value) -> {
          names[pos[0]] = sanitizeLabelName(key.getKey());
          values[pos[0]] = value.toString();
          pos[0]++;
        });
    for (int i = 0; i < additionalAttributes.length; i += 2) {
      names[pos[0]] = additionalAttributes[i];
      values[pos[0]] = additionalAttributes[i + 1];
      pos[0]++;
    }
    if (otelScopeEnabled && scope != null) {
      names[pos[0]] = OTEL_SCOPE_NAME;
      values[pos[0]] = scope.getName();
      pos[0]++;
      if (scope.getVersion() != null) {
        names[pos[0]] = OTEL_SCOPE_VERSION;
        values[pos[0]] = scope.getVersion();
        pos[0]++;
      }
    }
    return Labels.of(names, values);
  }

  private static MetricMetadata convertMetadata(MetricData metricData) {
    String name = sanitizeMetricName(metricData.getName());
    String help = metricData.getDescription();
    Unit unit = PrometheusUnitsHelper.convertUnit(metricData.getUnit());
    if (unit != null && !name.endsWith(unit.toString())) {
      name += "_" + unit;
    }
    return new MetricMetadata(name, help, unit);
  }

  private static void putOrMerge(
      Map<String, MetricSnapshot> snapshotsByName, MetricSnapshot snapshot) {
    String name = snapshot.getMetadata().getName();
    if (snapshotsByName.containsKey(name)) {
      MetricSnapshot merged = merge(snapshotsByName.get(name), snapshot);
      if (merged != null) {
        snapshotsByName.put(name, merged);
      }
    } else {
      snapshotsByName.put(name, snapshot);
    }
  }

  /**
   * OpenTelemetry may use the same metric name multiple times but in different instrumentation
   * scopes. In that case, we try to merge the metrics. They will have different {@code
   * otel_scope_name} attributes. However, merging is only possible if the metrics have the same
   * type. If the type differs, we log a message and drop one of them.
   */
  @Nullable
  private static MetricSnapshot merge(MetricSnapshot a, MetricSnapshot b) {
    MetricMetadata metadata = mergeMetadata(a.getMetadata(), b.getMetadata());
    if (metadata == null) {
      return null;
    }
    int numberOfDataPoints = a.getDataPoints().size() + b.getDataPoints().size();
    if (a instanceof GaugeSnapshot && b instanceof GaugeSnapshot) {
      List<GaugeDataPointSnapshot> dataPoints = new ArrayList<>(numberOfDataPoints);
      dataPoints.addAll(((GaugeSnapshot) a).getDataPoints());
      dataPoints.addAll(((GaugeSnapshot) b).getDataPoints());
      return new GaugeSnapshot(metadata, dataPoints);
    } else if (a instanceof CounterSnapshot && b instanceof CounterSnapshot) {
      List<CounterDataPointSnapshot> dataPoints = new ArrayList<>(numberOfDataPoints);
      dataPoints.addAll(((CounterSnapshot) a).getDataPoints());
      dataPoints.addAll(((CounterSnapshot) b).getDataPoints());
      return new CounterSnapshot(metadata, dataPoints);
    } else if (a instanceof HistogramSnapshot && b instanceof HistogramSnapshot) {
      List<HistogramDataPointSnapshot> dataPoints = new ArrayList<>(numberOfDataPoints);
      dataPoints.addAll(((HistogramSnapshot) a).getDataPoints());
      dataPoints.addAll(((HistogramSnapshot) b).getDataPoints());
      return new HistogramSnapshot(metadata, dataPoints);
    } else if (a instanceof SummarySnapshot && b instanceof SummarySnapshot) {
      List<SummaryDataPointSnapshot> dataPoints = new ArrayList<>(numberOfDataPoints);
      dataPoints.addAll(((SummarySnapshot) a).getDataPoints());
      dataPoints.addAll(((SummarySnapshot) b).getDataPoints());
      return new SummarySnapshot(metadata, dataPoints);
    } else if (a instanceof InfoSnapshot && b instanceof InfoSnapshot) {
      List<InfoDataPointSnapshot> dataPoints = new ArrayList<>(numberOfDataPoints);
      dataPoints.addAll(((InfoSnapshot) a).getDataPoints());
      dataPoints.addAll(((InfoSnapshot) b).getDataPoints());
      return new InfoSnapshot(metadata, dataPoints);
    } else {
      THROTTLING_LOGGER.log(
          Level.WARNING,
          "Conflicting metric name "
              + a.getMetadata().getPrometheusName()
              + ": Found one metric with type "
              + typeString(a)
              + " and one of type "
              + typeString(b)
              + ". Dropping the one with type "
              + typeString(b)
              + ".");
      return null;
    }
  }

  @Nullable
  private static MetricMetadata mergeMetadata(MetricMetadata a, MetricMetadata b) {
    String name = a.getPrometheusName();
    if (a.getName().equals(b.getName())) {
      name = a.getName();
    }
    String help = null;
    if (a.getHelp() != null && a.getHelp().equals(b.getHelp())) {
      help = a.getHelp();
    }
    Unit unit = a.getUnit();
    if (unit != null && !unit.equals(b.getUnit())) {
      THROTTLING_LOGGER.log(
          Level.WARNING,
          "Conflicting metrics: Multiple metrics with name "
              + name
              + " but different units found. Dropping the one with unit "
              + b.getUnit());
      return null;
    }
    return new MetricMetadata(name, help, unit);
  }

  private static String typeString(MetricSnapshot snapshot) {
    // Simple helper for a log message.
    return snapshot.getClass().getSimpleName().replace("Snapshot", "").toLowerCase(Locale.ENGLISH);
  }
}
