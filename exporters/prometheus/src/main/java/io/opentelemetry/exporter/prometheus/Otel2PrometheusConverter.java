/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.prometheus;

import static io.prometheus.metrics.model.snapshots.PrometheusNaming.sanitizeLabelName;
import static io.prometheus.metrics.model.snapshots.PrometheusNaming.sanitizeMetricName;
import static java.util.Objects.requireNonNull;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.AttributeType;
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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

/** Convert OpenTelemetry {@link MetricData} to Prometheus {@link MetricSnapshots}. */
final class Otel2PrometheusConverter {

  private static final Logger LOGGER = Logger.getLogger(Otel2PrometheusConverter.class.getName());
  private static final ThrottlingLogger THROTTLING_LOGGER = new ThrottlingLogger(LOGGER);
  private static final String OTEL_SCOPE_NAME = "otel_scope_name";
  private static final String OTEL_SCOPE_VERSION = "otel_scope_version";
  private static final String OTEL_SCOPE_SCHEMA_URL = "otel_scope_schema_url";
  private static final String OTEL_SCOPE_ATTRIBUTE_PREFIX = "otel_scope_";
  private static final long NANOS_PER_MILLISECOND = TimeUnit.MILLISECONDS.toNanos(1);
  static final int MAX_CACHE_SIZE = 10;

  @Nullable private final Predicate<String> allowedResourceAttributesFilter;

  /**
   * Used only if addResourceAttributesAsLabels is true. Once the cache reaches {@link
   * #MAX_CACHE_SIZE}, it is cleared to protect against unbounded conversion over time.
   */
  private final Map<Attributes, List<AttributeKey<?>>> resourceAttributesToAllowedKeysCache;

  /**
   * Constructor with feature flag parameter.
   *
   * @param allowedResourceAttributesFilter if not {@code null}, resource attributes with keys
   *     matching this predicate will be added as labels on each exported metric
   */
  Otel2PrometheusConverter(@Nullable Predicate<String> allowedResourceAttributesFilter) {
    this.allowedResourceAttributesFilter = allowedResourceAttributesFilter;
    this.resourceAttributesToAllowedKeysCache =
        allowedResourceAttributesFilter != null
            ? new ConcurrentHashMap<>()
            : Collections.emptyMap();
  }

  MetricSnapshots convert(@Nullable Collection<MetricData> metricDataCollection) {
    if (metricDataCollection == null || metricDataCollection.isEmpty()) {
      return MetricSnapshots.of();
    }
    Map<String, MetricSnapshot> snapshotsByName = new HashMap<>(metricDataCollection.size());
    Resource resource = null;
    for (MetricData metricData : metricDataCollection) {
      MetricSnapshot snapshot = convert(metricData);
      if (snapshot == null) {
        continue;
      }
      putOrMerge(snapshotsByName, snapshot);
      if (resource == null) {
        resource = metricData.getResource();
      }
    }
    if (resource != null) {
      putOrMerge(snapshotsByName, makeTargetInfo(resource));
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
        return convertLongGauge(
            metadata, scope, metricData.getLongGaugeData().getPoints(), metricData.getResource());
      case DOUBLE_GAUGE:
        return convertDoubleGauge(
            metadata, scope, metricData.getDoubleGaugeData().getPoints(), metricData.getResource());
      case LONG_SUM:
        SumData<LongPointData> longSumData = metricData.getLongSumData();
        if (longSumData.getAggregationTemporality() == AggregationTemporality.DELTA) {
          return null;
        } else if (longSumData.isMonotonic()) {
          return convertLongCounter(
              metadata, scope, longSumData.getPoints(), metricData.getResource());
        } else {
          return convertLongGauge(
              metadata, scope, longSumData.getPoints(), metricData.getResource());
        }
      case DOUBLE_SUM:
        SumData<DoublePointData> doubleSumData = metricData.getDoubleSumData();
        if (doubleSumData.getAggregationTemporality() == AggregationTemporality.DELTA) {
          return null;
        } else if (doubleSumData.isMonotonic()) {
          return convertDoubleCounter(
              metadata, scope, doubleSumData.getPoints(), metricData.getResource());
        } else {
          return convertDoubleGauge(
              metadata, scope, doubleSumData.getPoints(), metricData.getResource());
        }
      case HISTOGRAM:
        HistogramData histogramData = metricData.getHistogramData();
        if (histogramData.getAggregationTemporality() == AggregationTemporality.DELTA) {
          return null;
        } else {
          return convertHistogram(
              metadata, scope, histogramData.getPoints(), metricData.getResource());
        }
      case EXPONENTIAL_HISTOGRAM:
        ExponentialHistogramData exponentialHistogramData =
            metricData.getExponentialHistogramData();
        if (exponentialHistogramData.getAggregationTemporality() == AggregationTemporality.DELTA) {
          return null;
        } else {
          return convertExponentialHistogram(
              metadata, scope, exponentialHistogramData.getPoints(), metricData.getResource());
        }
      case SUMMARY:
        return convertSummary(
            metadata, scope, metricData.getSummaryData().getPoints(), metricData.getResource());
    }
    return null;
  }

  private GaugeSnapshot convertLongGauge(
      MetricMetadata metadata,
      InstrumentationScopeInfo scope,
      Collection<LongPointData> dataPoints,
      Resource resource) {
    List<GaugeDataPointSnapshot> data = new ArrayList<>(dataPoints.size());
    for (LongPointData longData : dataPoints) {
      data.add(
          new GaugeDataPointSnapshot(
              (double) longData.getValue(),
              convertAttributes(resource, scope, longData.getAttributes()),
              convertLongExemplar(longData.getExemplars())));
    }
    return new GaugeSnapshot(metadata, data);
  }

  private CounterSnapshot convertLongCounter(
      MetricMetadata metadata,
      InstrumentationScopeInfo scope,
      Collection<LongPointData> dataPoints,
      Resource resource) {
    List<CounterDataPointSnapshot> data = new ArrayList<>(dataPoints.size());
    for (LongPointData longData : dataPoints) {
      data.add(
          new CounterDataPointSnapshot(
              (double) longData.getValue(),
              convertAttributes(resource, scope, longData.getAttributes()),
              convertLongExemplar(longData.getExemplars()),
              longData.getStartEpochNanos() / NANOS_PER_MILLISECOND));
    }
    return new CounterSnapshot(metadata, data);
  }

  private GaugeSnapshot convertDoubleGauge(
      MetricMetadata metadata,
      InstrumentationScopeInfo scope,
      Collection<DoublePointData> dataPoints,
      Resource resource) {
    List<GaugeDataPointSnapshot> data = new ArrayList<>(dataPoints.size());
    for (DoublePointData doubleData : dataPoints) {
      data.add(
          new GaugeDataPointSnapshot(
              doubleData.getValue(),
              convertAttributes(resource, scope, doubleData.getAttributes()),
              convertDoubleExemplar(doubleData.getExemplars())));
    }
    return new GaugeSnapshot(metadata, data);
  }

  private CounterSnapshot convertDoubleCounter(
      MetricMetadata metadata,
      InstrumentationScopeInfo scope,
      Collection<DoublePointData> dataPoints,
      Resource resource) {
    List<CounterDataPointSnapshot> data = new ArrayList<>(dataPoints.size());
    for (DoublePointData doubleData : dataPoints) {
      data.add(
          new CounterDataPointSnapshot(
              doubleData.getValue(),
              convertAttributes(resource, scope, doubleData.getAttributes()),
              convertDoubleExemplar(doubleData.getExemplars()),
              doubleData.getStartEpochNanos() / NANOS_PER_MILLISECOND));
    }
    return new CounterSnapshot(metadata, data);
  }

  private HistogramSnapshot convertHistogram(
      MetricMetadata metadata,
      InstrumentationScopeInfo scope,
      Collection<HistogramPointData> dataPoints,
      Resource resource) {
    List<HistogramDataPointSnapshot> data = new ArrayList<>(dataPoints.size());
    for (HistogramPointData histogramData : dataPoints) {
      List<Double> boundaries = new ArrayList<>(histogramData.getBoundaries().size() + 1);
      boundaries.addAll(histogramData.getBoundaries());
      boundaries.add(Double.POSITIVE_INFINITY);
      data.add(
          new HistogramDataPointSnapshot(
              ClassicHistogramBuckets.of(boundaries, histogramData.getCounts()),
              histogramData.getSum(),
              convertAttributes(resource, scope, histogramData.getAttributes()),
              convertDoubleExemplars(histogramData.getExemplars()),
              histogramData.getStartEpochNanos() / NANOS_PER_MILLISECOND));
    }
    return new HistogramSnapshot(metadata, data);
  }

  @Nullable
  private HistogramSnapshot convertExponentialHistogram(
      MetricMetadata metadata,
      InstrumentationScopeInfo scope,
      Collection<ExponentialHistogramPointData> dataPoints,
      Resource resource) {
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
              convertAttributes(resource, scope, histogramData.getAttributes()),
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
      Collection<SummaryPointData> dataPoints,
      Resource resource) {
    List<SummaryDataPointSnapshot> data = new ArrayList<>(dataPoints.size());
    for (SummaryPointData summaryData : dataPoints) {
      data.add(
          new SummaryDataPointSnapshot(
              summaryData.getCount(),
              summaryData.getSum(),
              convertQuantiles(summaryData.getValues()),
              convertAttributes(resource, scope, summaryData.getAttributes()),
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
              null, // resource attributes are only copied for point's attributes
              null, // scope attributes are only needed for point's attributes
              exemplar.getFilteredAttributes(),
              "trace_id",
              spanContext.getTraceId(),
              "span_id",
              spanContext.getSpanId()),
          exemplar.getEpochNanos() / NANOS_PER_MILLISECOND);
    } else {
      return new Exemplar(
          value,
          convertAttributes(
              null, // resource attributes are only copied for point's attributes
              null, // scope attributes are only needed for point's attributes
              exemplar.getFilteredAttributes()),
          exemplar.getEpochNanos() / NANOS_PER_MILLISECOND);
    }
  }

  private InfoSnapshot makeTargetInfo(Resource resource) {
    return new InfoSnapshot(
        new MetricMetadata("target"),
        Collections.singletonList(
            new InfoDataPointSnapshot(
                convertAttributes(
                    null, // resource attributes are only copied for point's attributes
                    null, // scope attributes are only needed for point's attributes
                    resource.getAttributes()))));
  }

  /**
   * Convert OpenTelemetry attributes to Prometheus labels.
   *
   * @param resource optional resource (attributes) to be converted.
   * @param scope will be converted to {@code otel_scope_*} labels.
   * @param attributes the attributes to be converted.
   * @param additionalAttributes optional list of key/value pairs, may be empty.
   */
  @SuppressWarnings({"rawtypes", "unchecked"})
  private Labels convertAttributes(
      @Nullable Resource resource,
      @Nullable InstrumentationScopeInfo scope,
      Attributes attributes,
      String... additionalAttributes) {

    List<AttributeKey<?>> allowedAttributeKeys =
        allowedResourceAttributesFilter != null
            ? filterAllowedResourceAttributeKeys(resource)
            : Collections.emptyList();

    Map<String, String> labelNameToValue = new HashMap<>();
    attributes.forEach(
        (key, value) ->
            labelNameToValue.put(
                sanitizeLabelName(key.getKey()), toLabelValue(key.getType(), value)));

    for (int i = 0; i < additionalAttributes.length; i += 2) {
      labelNameToValue.putIfAbsent(
          requireNonNull(additionalAttributes[i]), additionalAttributes[i + 1]);
    }

    if (scope != null) {
      labelNameToValue.putIfAbsent(OTEL_SCOPE_NAME, scope.getName());
      if (scope.getVersion() != null) {
        labelNameToValue.putIfAbsent(OTEL_SCOPE_VERSION, scope.getVersion());
      }
      String schemaUrl = scope.getSchemaUrl();
      if (schemaUrl != null) {
        labelNameToValue.putIfAbsent(OTEL_SCOPE_SCHEMA_URL, schemaUrl);
      }
      scope
          .getAttributes()
          .forEach(
              (key, value) ->
                  labelNameToValue.putIfAbsent(
                      OTEL_SCOPE_ATTRIBUTE_PREFIX + key.getKey(), value.toString()));
    }

    if (resource != null) {
      Attributes resourceAttributes = resource.getAttributes();
      for (AttributeKey attributeKey : allowedAttributeKeys) {
        Object attributeValue = resourceAttributes.get(attributeKey);
        if (attributeValue != null) {
          labelNameToValue.putIfAbsent(
              sanitizeLabelName(attributeKey.getKey()), attributeValue.toString());
        }
      }
    }

    String[] names = new String[labelNameToValue.size()];
    String[] values = new String[labelNameToValue.size()];
    int[] pos = new int[] {0};
    labelNameToValue.forEach(
        (name, value) -> {
          names[pos[0]] = name;
          values[pos[0]] = value;
          pos[0] += 1;
        });

    return Labels.of(names, values);
  }

  private List<AttributeKey<?>> filterAllowedResourceAttributeKeys(@Nullable Resource resource) {
    requireNonNull(
        allowedResourceAttributesFilter,
        "This method should only be called when allowedResourceAttributesFilter is not null.");
    if (resource == null) {
      return Collections.emptyList();
    }

    List<AttributeKey<?>> allowedAttributeKeys =
        resourceAttributesToAllowedKeysCache.computeIfAbsent(
            resource.getAttributes(),
            resourceAttributes ->
                resourceAttributes.asMap().keySet().stream()
                    .filter(o -> allowedResourceAttributesFilter.test(o.getKey()))
                    .collect(Collectors.toList()));

    if (resourceAttributesToAllowedKeysCache.size() > MAX_CACHE_SIZE) {
      resourceAttributesToAllowedKeysCache.clear();
    }
    return allowedAttributeKeys;
  }

  private static MetricMetadata convertMetadata(MetricData metricData) {
    String name = sanitizeMetricName(metricData.getName());
    String help = metricData.getDescription();
    Unit unit = PrometheusUnitsHelper.convertUnit(metricData.getUnit());
    if (unit != null && !name.endsWith(unit.toString())) {
      name = name + "_" + unit;
    }
    // Repeated __ are not allowed according to spec, although this is allowed in prometheus
    while (name.contains("__")) {
      name = name.replace("__", "_");
    }

    return new MetricMetadata(name, help, unit);
  }

  private static void putOrMerge(
      Map<String, MetricSnapshot> snapshotsByName, MetricSnapshot snapshot) {
    String name = snapshot.getMetadata().getPrometheusName();
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
              + b.getUnit()
              + ".");
      return null;
    }
    return new MetricMetadata(name, help, unit);
  }

  private static String typeString(MetricSnapshot snapshot) {
    // Simple helper for a log message.
    return snapshot.getClass().getSimpleName().replace("Snapshot", "").toLowerCase(Locale.ENGLISH);
  }

  private static String toLabelValue(AttributeType type, Object attributeValue) {
    switch (type) {
      case STRING:
      case BOOLEAN:
      case LONG:
      case DOUBLE:
        return attributeValue.toString();
      case BOOLEAN_ARRAY:
      case LONG_ARRAY:
      case DOUBLE_ARRAY:
      case STRING_ARRAY:
        if (attributeValue instanceof List) {
          return toJsonStr((List<?>) attributeValue);
        } else {
          throw new IllegalStateException(
              String.format(
                  "Unexpected label value of %s for %s",
                  attributeValue.getClass().getName(), type.name()));
        }
    }
    throw new IllegalStateException("Unrecognized AttributeType: " + type);
  }

  public static String toJsonStr(List<?> attributeValue) {
    StringJoiner joiner = new StringJoiner(",", "[", "]");
    for (int i = 0; i < attributeValue.size(); i++) {
      Object value = attributeValue.get(i);
      joiner.add(value instanceof String ? toJsonValidStr((String) value) : String.valueOf(value));
    }
    return joiner.toString();
  }

  public static String toJsonValidStr(String str) {
    StringBuilder sb = new StringBuilder();
    sb.append('"');
    for (int i = 0; i < str.length(); i++) {
      char c = str.charAt(i);

      switch (c) {
        case '"':
          sb.append("\\\"");
          break;
        case '\\':
          sb.append("\\\\");
          break;
        case '\b':
          sb.append("\\b");
          break;
        case '\f':
          sb.append("\\f");
          break;
        case '\n':
          sb.append("\\n");
          break;
        case '\r':
          sb.append("\\r");
          break;
        case '\t':
          sb.append("\\t");
          break;
        default:
          if (c <= 0x1F) {
            sb.append(String.format(Locale.ROOT, "\\u%04X", (int) c));
          } else {
            sb.append(c);
          }
      }
    }
    sb.append('"');
    return sb.toString();
  }
}
