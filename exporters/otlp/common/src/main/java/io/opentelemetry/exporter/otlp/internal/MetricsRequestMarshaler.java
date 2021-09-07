/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal;

import io.opentelemetry.api.internal.OtelEncodingUtils;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.proto.collector.metrics.v1.internal.ExportMetricsServiceRequest;
import io.opentelemetry.proto.metrics.v1.internal.AggregationTemporality;
import io.opentelemetry.proto.metrics.v1.internal.Gauge;
import io.opentelemetry.proto.metrics.v1.internal.Histogram;
import io.opentelemetry.proto.metrics.v1.internal.HistogramDataPoint;
import io.opentelemetry.proto.metrics.v1.internal.InstrumentationLibraryMetrics;
import io.opentelemetry.proto.metrics.v1.internal.Metric;
import io.opentelemetry.proto.metrics.v1.internal.NumberDataPoint;
import io.opentelemetry.proto.metrics.v1.internal.ResourceMetrics;
import io.opentelemetry.proto.metrics.v1.internal.Sum;
import io.opentelemetry.proto.metrics.v1.internal.Summary;
import io.opentelemetry.proto.metrics.v1.internal.SummaryDataPoint;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.data.DoubleExemplar;
import io.opentelemetry.sdk.metrics.data.DoubleHistogramData;
import io.opentelemetry.sdk.metrics.data.DoubleHistogramPointData;
import io.opentelemetry.sdk.metrics.data.DoublePointData;
import io.opentelemetry.sdk.metrics.data.DoubleSummaryData;
import io.opentelemetry.sdk.metrics.data.DoubleSummaryPointData;
import io.opentelemetry.sdk.metrics.data.Exemplar;
import io.opentelemetry.sdk.metrics.data.GaugeData;
import io.opentelemetry.sdk.metrics.data.LongExemplar;
import io.opentelemetry.sdk.metrics.data.LongPointData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.PointData;
import io.opentelemetry.sdk.metrics.data.SumData;
import io.opentelemetry.sdk.metrics.data.ValueAtPercentile;
import io.opentelemetry.sdk.resources.Resource;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * {@link Marshaler} to convert SDK {@link MetricData} to OTLP ExportMetricsServiceRequest.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class MetricsRequestMarshaler extends MarshalerWithSize implements Marshaler {

  private final ResourceMetricsMarshaler[] resourceMetricsMarshalers;

  /**
   * Returns a {@link MetricsRequestMarshaler} that can be used to convert the provided {@link
   * MetricData} into a serialized OTLP ExportMetricsServiceRequest.
   */
  public static MetricsRequestMarshaler create(Collection<MetricData> metricDataList) {
    Map<Resource, Map<InstrumentationLibraryInfo, List<Marshaler>>> resourceAndLibraryMap =
        groupByResourceAndLibrary(metricDataList);

    ResourceMetricsMarshaler[] resourceMetricsMarshalers =
        new ResourceMetricsMarshaler[resourceAndLibraryMap.size()];
    int posResource = 0;
    for (Map.Entry<Resource, Map<InstrumentationLibraryInfo, List<Marshaler>>> entry :
        resourceAndLibraryMap.entrySet()) {
      final InstrumentationLibraryMetricsMarshaler[] instrumentationLibrarySpansMarshalers =
          new InstrumentationLibraryMetricsMarshaler[entry.getValue().size()];
      int posInstrumentation = 0;
      for (Map.Entry<InstrumentationLibraryInfo, List<Marshaler>> entryIs :
          entry.getValue().entrySet()) {
        instrumentationLibrarySpansMarshalers[posInstrumentation++] =
            new InstrumentationLibraryMetricsMarshaler(
                InstrumentationLibraryMarshaler.create(entryIs.getKey()),
                MarshalerUtil.toBytes(entryIs.getKey().getSchemaUrl()),
                entryIs.getValue());
      }
      resourceMetricsMarshalers[posResource++] =
          new ResourceMetricsMarshaler(
              ResourceMarshaler.create(entry.getKey()),
              MarshalerUtil.toBytes(entry.getKey().getSchemaUrl()),
              instrumentationLibrarySpansMarshalers);
    }

    return new MetricsRequestMarshaler(resourceMetricsMarshalers);
  }

  private MetricsRequestMarshaler(ResourceMetricsMarshaler[] resourceMetricsMarshalers) {
    super(calculateSize(resourceMetricsMarshalers));
    this.resourceMetricsMarshalers = resourceMetricsMarshalers;
  }

  @Override
  public void writeTo(Serializer output) throws IOException {
    output.serializeRepeatedMessage(
        ExportMetricsServiceRequest.RESOURCE_METRICS, resourceMetricsMarshalers);
  }

  private static int calculateSize(ResourceMetricsMarshaler[] resourceMetricsMarshalers) {
    int size = 0;
    size +=
        MarshalerUtil.sizeRepeatedMessage(
            ExportMetricsServiceRequest.RESOURCE_METRICS, resourceMetricsMarshalers);
    return size;
  }

  private static Map<Resource, Map<InstrumentationLibraryInfo, List<Marshaler>>>
      groupByResourceAndLibrary(Collection<MetricData> metricDataList) {
    return MarshalerUtil.groupByResourceAndLibrary(
        metricDataList,
        // TODO(anuraaga): Replace with an internal SdkData type of interface that exposes these
        // two.
        MetricData::getResource,
        MetricData::getInstrumentationLibraryInfo,
        MetricMarshaler::create);
  }

  private static final class ResourceMetricsMarshaler extends MarshalerWithSize {
    private final ResourceMarshaler resourceMarshaler;
    private final byte[] schemaUrl;
    private final InstrumentationLibraryMetricsMarshaler[] instrumentationLibraryMetricsMarshalers;

    private ResourceMetricsMarshaler(
        ResourceMarshaler resourceMarshaler,
        byte[] schemaUrl,
        InstrumentationLibraryMetricsMarshaler[] instrumentationLibraryMetricsMarshalers) {
      super(calculateSize(resourceMarshaler, schemaUrl, instrumentationLibraryMetricsMarshalers));
      this.resourceMarshaler = resourceMarshaler;
      this.schemaUrl = schemaUrl;
      this.instrumentationLibraryMetricsMarshalers = instrumentationLibraryMetricsMarshalers;
    }

    @Override
    public void writeTo(Serializer output) throws IOException {
      output.serializeMessage(ResourceMetrics.RESOURCE, resourceMarshaler);
      output.serializeRepeatedMessage(
          ResourceMetrics.INSTRUMENTATION_LIBRARY_METRICS, instrumentationLibraryMetricsMarshalers);
      output.serializeString(ResourceMetrics.SCHEMA_URL, schemaUrl);
    }

    private static int calculateSize(
        ResourceMarshaler resourceMarshaler,
        byte[] schemaUrl,
        InstrumentationLibraryMetricsMarshaler[] instrumentationLibraryMetricsMarshalers) {
      int size = 0;
      size += MarshalerUtil.sizeMessage(ResourceMetrics.RESOURCE, resourceMarshaler);
      size += MarshalerUtil.sizeBytes(ResourceMetrics.SCHEMA_URL, schemaUrl);
      size +=
          MarshalerUtil.sizeRepeatedMessage(
              ResourceMetrics.INSTRUMENTATION_LIBRARY_METRICS,
              instrumentationLibraryMetricsMarshalers);
      return size;
    }
  }

  private static final class InstrumentationLibraryMetricsMarshaler extends MarshalerWithSize {
    private final InstrumentationLibraryMarshaler instrumentationLibrary;
    private final List<Marshaler> metricMarshalers;
    private final byte[] schemaUrlUtf8;

    private InstrumentationLibraryMetricsMarshaler(
        InstrumentationLibraryMarshaler instrumentationLibrary,
        byte[] schemaUrlUtf8,
        List<Marshaler> metricMarshalers) {
      super(calculateSize(instrumentationLibrary, schemaUrlUtf8, metricMarshalers));
      this.instrumentationLibrary = instrumentationLibrary;
      this.schemaUrlUtf8 = schemaUrlUtf8;
      this.metricMarshalers = metricMarshalers;
    }

    @Override
    public void writeTo(Serializer output) throws IOException {
      output.serializeMessage(
          InstrumentationLibraryMetrics.INSTRUMENTATION_LIBRARY, instrumentationLibrary);
      output.serializeRepeatedMessage(InstrumentationLibraryMetrics.METRICS, metricMarshalers);
      output.serializeString(InstrumentationLibraryMetrics.SCHEMA_URL, schemaUrlUtf8);
    }

    private static int calculateSize(
        InstrumentationLibraryMarshaler instrumentationLibrary,
        byte[] schemaUrlUtf8,
        List<Marshaler> metricMarshalers) {
      int size = 0;
      size +=
          MarshalerUtil.sizeMessage(
              InstrumentationLibraryMetrics.INSTRUMENTATION_LIBRARY, instrumentationLibrary);
      size += MarshalerUtil.sizeBytes(InstrumentationLibraryMetrics.SCHEMA_URL, schemaUrlUtf8);
      size +=
          MarshalerUtil.sizeRepeatedMessage(
              InstrumentationLibraryMetrics.METRICS, metricMarshalers);
      return size;
    }
  }

  static final class MetricMarshaler extends MarshalerWithSize {
    private final byte[] nameUtf8;
    private final byte[] descriptionUtf8;
    private final byte[] unitUtf8;

    private final Marshaler dataMarshaler;
    private final ProtoFieldInfo dataField;

    static Marshaler create(MetricData metric) {
      // TODO(anuraaga): Cache these as they should be effectively singleton.
      byte[] name = MarshalerUtil.toBytes(metric.getName());
      byte[] description = MarshalerUtil.toBytes(metric.getDescription());
      byte[] unit = MarshalerUtil.toBytes(metric.getUnit());

      Marshaler dataMarshaler = null;
      ProtoFieldInfo dataFIeld = null;
      switch (metric.getType()) {
        case LONG_GAUGE:
          dataMarshaler = GaugeMarshaler.create(metric.getLongGaugeData());
          dataFIeld = Metric.GAUGE;
          break;
        case DOUBLE_GAUGE:
          dataMarshaler = GaugeMarshaler.create(metric.getDoubleGaugeData());
          dataFIeld = Metric.GAUGE;
          break;
        case LONG_SUM:
          dataMarshaler = SumMarshaler.create(metric.getLongSumData());
          dataFIeld = Metric.SUM;
          break;
        case DOUBLE_SUM:
          dataMarshaler = SumMarshaler.create(metric.getDoubleSumData());
          dataFIeld = Metric.SUM;
          break;
        case SUMMARY:
          dataMarshaler = SummaryMarshaler.create(metric.getDoubleSummaryData());
          dataFIeld = Metric.SUMMARY;
          break;
        case HISTOGRAM:
          dataMarshaler = HistogramMarshaler.create(metric.getDoubleHistogramData());
          dataFIeld = Metric.HISTOGRAM;
          break;
      }

      if (dataMarshaler == null) {
        // Someone not using BOM to align versions as we require. Just skip the metric.
        return NoopMarshaler.INSTANCE;
      }

      return new MetricMarshaler(name, description, unit, dataMarshaler, dataFIeld);
    }

    private MetricMarshaler(
        byte[] nameUtf8,
        byte[] descriptionUtf8,
        byte[] unitUtf8,
        Marshaler dataMarshaler,
        ProtoFieldInfo dataField) {
      super(calculateSize(nameUtf8, descriptionUtf8, unitUtf8, dataMarshaler, dataField));
      this.nameUtf8 = nameUtf8;
      this.descriptionUtf8 = descriptionUtf8;
      this.unitUtf8 = unitUtf8;
      this.dataMarshaler = dataMarshaler;
      this.dataField = dataField;
    }

    @Override
    public void writeTo(Serializer output) throws IOException {
      output.serializeString(Metric.NAME, nameUtf8);
      output.serializeString(Metric.DESCRIPTION, descriptionUtf8);
      output.serializeString(Metric.UNIT, unitUtf8);
      output.serializeMessage(dataField, dataMarshaler);
    }

    private static int calculateSize(
        byte[] nameUtf8,
        byte[] descriptionUtf8,
        byte[] unitUtf8,
        Marshaler dataMarshaler,
        ProtoFieldInfo dataField) {
      int size = 0;
      size += MarshalerUtil.sizeBytes(Metric.NAME, nameUtf8);
      size += MarshalerUtil.sizeBytes(Metric.DESCRIPTION, descriptionUtf8);
      size += MarshalerUtil.sizeBytes(Metric.UNIT, unitUtf8);
      size += MarshalerUtil.sizeMessage(dataField, dataMarshaler);
      return size;
    }
  }

  private static class GaugeMarshaler extends MarshalerWithSize {
    private final NumberDataPointMarshaler[] dataPoints;

    static GaugeMarshaler create(GaugeData<? extends PointData> gauge) {
      NumberDataPointMarshaler[] dataPointMarshalers =
          NumberDataPointMarshaler.createRepeated(gauge.getPoints());

      return new GaugeMarshaler(dataPointMarshalers);
    }

    private GaugeMarshaler(NumberDataPointMarshaler[] dataPoints) {
      super(calculateSize(dataPoints));
      this.dataPoints = dataPoints;
    }

    @Override
    public void writeTo(Serializer output) throws IOException {
      output.serializeRepeatedMessage(Gauge.DATA_POINTS, dataPoints);
    }

    private static int calculateSize(NumberDataPointMarshaler[] dataPoints) {
      int size = 0;
      size += MarshalerUtil.sizeRepeatedMessage(Gauge.DATA_POINTS, dataPoints);
      return size;
    }
  }

  private static class HistogramMarshaler extends MarshalerWithSize {
    private final HistogramDataPointMarshaler[] dataPoints;
    private final int aggregationTemporality;

    static HistogramMarshaler create(DoubleHistogramData histogram) {
      HistogramDataPointMarshaler[] dataPointMarshalers =
          HistogramDataPointMarshaler.createRepeated(histogram.getPoints());
      return new HistogramMarshaler(
          dataPointMarshalers, mapToTemporality(histogram.getAggregationTemporality()));
    }

    private HistogramMarshaler(
        HistogramDataPointMarshaler[] dataPoints, int aggregationTemporality) {
      super(calculateSize(dataPoints, aggregationTemporality));
      this.dataPoints = dataPoints;
      this.aggregationTemporality = aggregationTemporality;
    }

    @Override
    public void writeTo(Serializer output) throws IOException {
      output.serializeRepeatedMessage(Histogram.DATA_POINTS, dataPoints);
      output.serializeEnum(Histogram.AGGREGATION_TEMPORALITY, aggregationTemporality);
    }

    private static int calculateSize(
        HistogramDataPointMarshaler[] dataPoints, int aggregationTemporality) {
      int size = 0;
      size += MarshalerUtil.sizeRepeatedMessage(Histogram.DATA_POINTS, dataPoints);
      size += MarshalerUtil.sizeEnum(Histogram.AGGREGATION_TEMPORALITY, aggregationTemporality);
      return size;
    }
  }

  static class HistogramDataPointMarshaler extends MarshalerWithSize {
    private final long startTimeUnixNano;
    private final long timeUnixNano;
    private final long count;
    private final double sum;
    private final List<Long> bucketCounts;
    private final List<Double> explicitBounds;
    private final ExemplarMarshaler[] exemplars;
    private final KeyValueMarshaler[] attributes;

    static HistogramDataPointMarshaler[] createRepeated(
        Collection<DoubleHistogramPointData> points) {
      HistogramDataPointMarshaler[] marshalers = new HistogramDataPointMarshaler[points.size()];
      int index = 0;
      for (DoubleHistogramPointData point : points) {
        marshalers[index++] = HistogramDataPointMarshaler.create(point);
      }
      return marshalers;
    }

    static HistogramDataPointMarshaler create(DoubleHistogramPointData point) {
      KeyValueMarshaler[] attributeMarshalers =
          KeyValueMarshaler.createRepeated(point.getAttributes());
      ExemplarMarshaler[] exemplarMarshalers =
          ExemplarMarshaler.createRepeated(point.getExemplars());

      return new HistogramDataPointMarshaler(
          point.getStartEpochNanos(),
          point.getEpochNanos(),
          point.getCount(),
          point.getSum(),
          point.getCounts(),
          point.getBoundaries(),
          exemplarMarshalers,
          attributeMarshalers);
    }

    private HistogramDataPointMarshaler(
        long startTimeUnixNano,
        long timeUnixNano,
        long count,
        double sum,
        List<Long> bucketCounts,
        List<Double> explicitBounds,
        ExemplarMarshaler[] exemplars,
        KeyValueMarshaler[] attributes) {
      super(
          calculateSize(
              startTimeUnixNano,
              timeUnixNano,
              count,
              sum,
              bucketCounts,
              explicitBounds,
              exemplars,
              attributes));
      this.startTimeUnixNano = startTimeUnixNano;
      this.timeUnixNano = timeUnixNano;
      this.count = count;
      this.sum = sum;
      this.bucketCounts = bucketCounts;
      this.explicitBounds = explicitBounds;
      this.exemplars = exemplars;
      this.attributes = attributes;
    }

    @Override
    public void writeTo(Serializer output) throws IOException {
      output.serializeFixed64(HistogramDataPoint.START_TIME_UNIX_NANO, startTimeUnixNano);
      output.serializeFixed64(HistogramDataPoint.TIME_UNIX_NANO, timeUnixNano);
      output.serializeFixed64(HistogramDataPoint.COUNT, count);
      output.serializeDouble(HistogramDataPoint.SUM, sum);
      output.serializeRepeatedFixed64(HistogramDataPoint.BUCKET_COUNTS, bucketCounts);
      output.serializeRepeatedDouble(HistogramDataPoint.EXPLICIT_BOUNDS, explicitBounds);
      output.serializeRepeatedMessage(HistogramDataPoint.EXEMPLARS, exemplars);
      output.serializeRepeatedMessage(HistogramDataPoint.ATTRIBUTES, attributes);
    }

    private static int calculateSize(
        long startTimeUnixNano,
        long timeUnixNano,
        long count,
        double sum,
        List<Long> bucketCounts,
        List<Double> explicitBounds,
        ExemplarMarshaler[] exemplars,
        KeyValueMarshaler[] attributes) {
      int size = 0;
      size += MarshalerUtil.sizeFixed64(HistogramDataPoint.START_TIME_UNIX_NANO, startTimeUnixNano);
      size += MarshalerUtil.sizeFixed64(HistogramDataPoint.TIME_UNIX_NANO, timeUnixNano);
      size += MarshalerUtil.sizeFixed64(HistogramDataPoint.COUNT, count);
      size += MarshalerUtil.sizeDouble(HistogramDataPoint.SUM, sum);
      size += MarshalerUtil.sizeRepeatedFixed64(HistogramDataPoint.BUCKET_COUNTS, bucketCounts);
      size += MarshalerUtil.sizeRepeatedDouble(HistogramDataPoint.EXPLICIT_BOUNDS, explicitBounds);
      size += MarshalerUtil.sizeRepeatedMessage(HistogramDataPoint.EXEMPLARS, exemplars);
      size += MarshalerUtil.sizeRepeatedMessage(HistogramDataPoint.ATTRIBUTES, attributes);
      return size;
    }
  }

  private static class SumMarshaler extends MarshalerWithSize {
    private final NumberDataPointMarshaler[] dataPoints;
    private final int aggregationTemporality;
    private final boolean isMonotonic;

    static SumMarshaler create(SumData<? extends PointData> sum) {
      NumberDataPointMarshaler[] dataPointMarshalers =
          NumberDataPointMarshaler.createRepeated(sum.getPoints());

      return new SumMarshaler(
          dataPointMarshalers,
          mapToTemporality(sum.getAggregationTemporality()),
          sum.isMonotonic());
    }

    private SumMarshaler(
        NumberDataPointMarshaler[] dataPoints, int aggregationTemporality, boolean isMonotonic) {
      super(calculateSize(dataPoints, aggregationTemporality, isMonotonic));
      this.dataPoints = dataPoints;
      this.aggregationTemporality = aggregationTemporality;
      this.isMonotonic = isMonotonic;
    }

    @Override
    public void writeTo(Serializer output) throws IOException {
      output.serializeRepeatedMessage(Sum.DATA_POINTS, dataPoints);
      output.serializeEnum(Sum.AGGREGATION_TEMPORALITY, aggregationTemporality);
      output.serializeBool(Sum.IS_MONOTONIC, isMonotonic);
    }

    private static int calculateSize(
        NumberDataPointMarshaler[] dataPoints, int aggregationTemporality, boolean isMonotonic) {
      int size = 0;
      size += MarshalerUtil.sizeRepeatedMessage(Sum.DATA_POINTS, dataPoints);
      size += MarshalerUtil.sizeEnum(Sum.AGGREGATION_TEMPORALITY, aggregationTemporality);
      size += MarshalerUtil.sizeBool(Sum.IS_MONOTONIC, isMonotonic);
      return size;
    }
  }

  private static class SummaryMarshaler extends MarshalerWithSize {
    private final SummaryDataPointMarshaler[] dataPoints;

    static SummaryMarshaler create(DoubleSummaryData summary) {
      SummaryDataPointMarshaler[] dataPointMarshalers =
          SummaryDataPointMarshaler.createRepeated(summary.getPoints());
      return new SummaryMarshaler(dataPointMarshalers);
    }

    private SummaryMarshaler(SummaryDataPointMarshaler[] dataPoints) {
      super(calculateSize(dataPoints));
      this.dataPoints = dataPoints;
    }

    @Override
    public void writeTo(Serializer output) throws IOException {
      output.serializeRepeatedMessage(Summary.DATA_POINTS, dataPoints);
    }

    private static int calculateSize(SummaryDataPointMarshaler[] dataPoints) {
      int size = 0;
      size += MarshalerUtil.sizeRepeatedMessage(Summary.DATA_POINTS, dataPoints);
      return size;
    }
  }

  static class SummaryDataPointMarshaler extends MarshalerWithSize {
    private final long startTimeUnixNano;
    private final long timeUnixNano;
    private final long count;
    private final double sum;
    private final ValueAtQuantileMarshaler[] quantileValues;
    private final KeyValueMarshaler[] attributes;

    static SummaryDataPointMarshaler[] createRepeated(Collection<DoubleSummaryPointData> points) {
      SummaryDataPointMarshaler[] marshalers = new SummaryDataPointMarshaler[points.size()];
      int index = 0;
      for (DoubleSummaryPointData point : points) {
        marshalers[index++] = SummaryDataPointMarshaler.create(point);
      }
      return marshalers;
    }

    static SummaryDataPointMarshaler create(DoubleSummaryPointData point) {
      ValueAtQuantileMarshaler[] quantileMarshalers =
          ValueAtQuantileMarshaler.createRepeated(point.getPercentileValues());
      KeyValueMarshaler[] attributeMarshalers =
          KeyValueMarshaler.createRepeated(point.getAttributes());

      return new SummaryDataPointMarshaler(
          point.getStartEpochNanos(),
          point.getEpochNanos(),
          point.getCount(),
          point.getSum(),
          quantileMarshalers,
          attributeMarshalers);
    }

    private SummaryDataPointMarshaler(
        long startTimeUnixNano,
        long timeUnixNano,
        long count,
        double sum,
        ValueAtQuantileMarshaler[] quantileValues,
        KeyValueMarshaler[] attributes) {
      super(calculateSize(startTimeUnixNano, timeUnixNano, count, sum, quantileValues, attributes));
      this.startTimeUnixNano = startTimeUnixNano;
      this.timeUnixNano = timeUnixNano;
      this.count = count;
      this.sum = sum;
      this.quantileValues = quantileValues;
      this.attributes = attributes;
    }

    @Override
    public void writeTo(Serializer output) throws IOException {
      output.serializeFixed64(SummaryDataPoint.START_TIME_UNIX_NANO, startTimeUnixNano);
      output.serializeFixed64(SummaryDataPoint.TIME_UNIX_NANO, timeUnixNano);
      output.serializeFixed64(SummaryDataPoint.COUNT, count);
      output.serializeDouble(SummaryDataPoint.SUM, sum);
      output.serializeRepeatedMessage(SummaryDataPoint.QUANTILE_VALUES, quantileValues);
      output.serializeRepeatedMessage(SummaryDataPoint.ATTRIBUTES, attributes);
    }

    private static int calculateSize(
        long startTimeUnixNano,
        long timeUnixNano,
        long count,
        double sum,
        ValueAtQuantileMarshaler[] quantileValues,
        KeyValueMarshaler[] attributes) {
      int size = 0;
      size += MarshalerUtil.sizeFixed64(SummaryDataPoint.START_TIME_UNIX_NANO, startTimeUnixNano);
      size += MarshalerUtil.sizeFixed64(SummaryDataPoint.TIME_UNIX_NANO, timeUnixNano);
      size += MarshalerUtil.sizeFixed64(SummaryDataPoint.COUNT, count);
      size += MarshalerUtil.sizeDouble(SummaryDataPoint.SUM, sum);
      size += MarshalerUtil.sizeRepeatedMessage(SummaryDataPoint.QUANTILE_VALUES, quantileValues);
      size += MarshalerUtil.sizeRepeatedMessage(SummaryDataPoint.ATTRIBUTES, attributes);
      return size;
    }
  }

  private static class ValueAtQuantileMarshaler extends MarshalerWithSize {
    private final double quantile;
    private final double value;

    static ValueAtQuantileMarshaler[] createRepeated(List<ValueAtPercentile> values) {
      int numValues = values.size();
      ValueAtQuantileMarshaler[] marshalers = new ValueAtQuantileMarshaler[numValues];
      for (int i = 0; i < numValues; i++) {
        marshalers[i] = ValueAtQuantileMarshaler.create(values.get(i));
      }
      return marshalers;
    }

    private static ValueAtQuantileMarshaler create(ValueAtPercentile value) {
      return new ValueAtQuantileMarshaler(value.getPercentile() / 100.0, value.getValue());
    }

    private ValueAtQuantileMarshaler(double quantile, double value) {
      super(calculateSize(quantile, value));
      this.quantile = quantile;
      this.value = value;
    }

    @Override
    public void writeTo(Serializer output) throws IOException {
      output.serializeDouble(SummaryDataPoint.ValueAtQuantile.QUANTILE, quantile);
      output.serializeDouble(SummaryDataPoint.ValueAtQuantile.VALUE, value);
    }

    private static int calculateSize(double quantile, double value) {
      int size = 0;
      size += MarshalerUtil.sizeDouble(SummaryDataPoint.ValueAtQuantile.QUANTILE, quantile);
      size += MarshalerUtil.sizeDouble(SummaryDataPoint.ValueAtQuantile.VALUE, value);
      return size;
    }
  }

  static final class NumberDataPointMarshaler extends MarshalerWithSize {
    private final long startTimeUnixNano;
    private final long timeUnixNano;

    // Always fixed64, for a double it's the bits themselves.
    private final long value;
    private final ProtoFieldInfo valueField;

    private final ExemplarMarshaler[] exemplars;
    private final KeyValueMarshaler[] attributes;

    static NumberDataPointMarshaler[] createRepeated(Collection<? extends PointData> points) {
      int numPoints = points.size();
      NumberDataPointMarshaler[] marshalers = new NumberDataPointMarshaler[numPoints];
      int index = 0;
      for (PointData point : points) {
        marshalers[index++] = NumberDataPointMarshaler.create(point);
      }
      return marshalers;
    }

    static NumberDataPointMarshaler create(PointData point) {
      ExemplarMarshaler[] exemplarMarshalers =
          ExemplarMarshaler.createRepeated(point.getExemplars());
      KeyValueMarshaler[] attributeMarshalers =
          KeyValueMarshaler.createRepeated(point.getAttributes());

      final long value;
      final ProtoFieldInfo valueField;
      if (point instanceof LongPointData) {
        value = ((LongPointData) point).getValue();
        valueField = NumberDataPoint.AS_INT;
      } else {
        assert point instanceof DoublePointData;
        value = Double.doubleToRawLongBits(((DoublePointData) point).getValue());
        valueField = NumberDataPoint.AS_DOUBLE;
      }

      return new NumberDataPointMarshaler(
          point.getStartEpochNanos(),
          point.getEpochNanos(),
          value,
          valueField,
          exemplarMarshalers,
          attributeMarshalers);
    }

    private NumberDataPointMarshaler(
        long startTimeUnixNano,
        long timeUnixNano,
        long value,
        ProtoFieldInfo valueField,
        ExemplarMarshaler[] exemplars,
        KeyValueMarshaler[] attributes) {
      super(
          calculateSize(startTimeUnixNano, timeUnixNano, value, valueField, exemplars, attributes));
      this.startTimeUnixNano = startTimeUnixNano;
      this.timeUnixNano = timeUnixNano;
      this.value = value;
      this.valueField = valueField;
      this.exemplars = exemplars;
      this.attributes = attributes;
    }

    @Override
    public void writeTo(Serializer output) throws IOException {
      output.serializeFixed64(NumberDataPoint.START_TIME_UNIX_NANO, startTimeUnixNano);
      output.serializeFixed64(NumberDataPoint.TIME_UNIX_NANO, timeUnixNano);
      output.serializeFixed64(valueField, value);
      output.serializeRepeatedMessage(NumberDataPoint.EXEMPLARS, exemplars);
      output.serializeRepeatedMessage(NumberDataPoint.ATTRIBUTES, attributes);
    }

    private static int calculateSize(
        long startTimeUnixNano,
        long timeUnixNano,
        long value,
        ProtoFieldInfo valueField,
        ExemplarMarshaler[] exemplars,
        KeyValueMarshaler[] attributes) {
      int size = 0;
      size += MarshalerUtil.sizeFixed64(NumberDataPoint.START_TIME_UNIX_NANO, startTimeUnixNano);
      size += MarshalerUtil.sizeFixed64(NumberDataPoint.TIME_UNIX_NANO, timeUnixNano);
      size += MarshalerUtil.sizeFixed64(valueField, value);
      size += MarshalerUtil.sizeRepeatedMessage(NumberDataPoint.EXEMPLARS, exemplars);
      size += MarshalerUtil.sizeRepeatedMessage(NumberDataPoint.ATTRIBUTES, attributes);
      return size;
    }
  }

  private static class ExemplarMarshaler extends MarshalerWithSize {

    private final long timeUnixNano;

    // Always fixed64, for a double it's the bits themselves.
    private final long value;
    private final ProtoFieldInfo valueField;

    private final byte[] spanId;
    private final byte[] traceId;

    private final KeyValueMarshaler[] filteredAttributeMarshalers;

    static ExemplarMarshaler[] createRepeated(List<Exemplar> exemplars) {
      int numExemplars = exemplars.size();
      ExemplarMarshaler[] marshalers = new ExemplarMarshaler[numExemplars];
      for (int i = 0; i < numExemplars; i++) {
        marshalers[i] = ExemplarMarshaler.create(exemplars.get(i));
      }
      return marshalers;
    }

    private static ExemplarMarshaler create(Exemplar exemplar) {
      KeyValueMarshaler[] attributeMarshalers =
          KeyValueMarshaler.createRepeated(exemplar.getFilteredAttributes());

      final long value;
      final ProtoFieldInfo valueField;
      if (exemplar instanceof LongExemplar) {
        value = ((LongExemplar) exemplar).getValue();
        valueField = io.opentelemetry.proto.metrics.v1.internal.Exemplar.AS_INT;
      } else {
        assert exemplar instanceof DoubleExemplar;
        value = Double.doubleToRawLongBits(((DoubleExemplar) exemplar).getValue());
        valueField = io.opentelemetry.proto.metrics.v1.internal.Exemplar.AS_DOUBLE;
      }

      byte[] spanId = MarshalerUtil.EMPTY_BYTES;
      if (exemplar.getSpanId() != null) {
        spanId = OtelEncodingUtils.bytesFromBase16(exemplar.getSpanId(), SpanId.getLength());
      }
      byte[] traceId = MarshalerUtil.EMPTY_BYTES;
      if (exemplar.getTraceId() != null) {
        traceId = OtelEncodingUtils.bytesFromBase16(exemplar.getTraceId(), TraceId.getLength());
      }

      return new ExemplarMarshaler(
          exemplar.getEpochNanos(), value, valueField, spanId, traceId, attributeMarshalers);
    }

    private ExemplarMarshaler(
        long timeUnixNano,
        long value,
        ProtoFieldInfo valueField,
        byte[] spanId,
        byte[] traceId,
        KeyValueMarshaler[] filteredAttributeMarshalers) {
      super(
          calculateSize(
              timeUnixNano, value, valueField, spanId, traceId, filteredAttributeMarshalers));
      this.timeUnixNano = timeUnixNano;
      this.value = value;
      this.valueField = valueField;
      this.spanId = spanId;
      this.traceId = traceId;
      this.filteredAttributeMarshalers = filteredAttributeMarshalers;
    }

    @Override
    public void writeTo(Serializer output) throws IOException {
      output.serializeFixed64(
          io.opentelemetry.proto.metrics.v1.internal.Exemplar.TIME_UNIX_NANO, timeUnixNano);
      output.serializeFixed64(valueField, value);
      output.serializeBytes(io.opentelemetry.proto.metrics.v1.internal.Exemplar.SPAN_ID, spanId);
      output.serializeBytes(io.opentelemetry.proto.metrics.v1.internal.Exemplar.TRACE_ID, traceId);
      output.serializeRepeatedMessage(
          io.opentelemetry.proto.metrics.v1.internal.Exemplar.FILTERED_ATTRIBUTES,
          filteredAttributeMarshalers);
    }

    private static int calculateSize(
        long timeUnixNano,
        long value,
        ProtoFieldInfo valueField,
        byte[] spanId,
        byte[] traceId,
        KeyValueMarshaler[] filteredAttributeMarshalers) {
      int size = 0;
      size +=
          MarshalerUtil.sizeFixed64(
              io.opentelemetry.proto.metrics.v1.internal.Exemplar.TIME_UNIX_NANO, timeUnixNano);
      size += MarshalerUtil.sizeFixed64(valueField, value);
      size +=
          MarshalerUtil.sizeBytes(
              io.opentelemetry.proto.metrics.v1.internal.Exemplar.SPAN_ID, spanId);
      size +=
          MarshalerUtil.sizeBytes(
              io.opentelemetry.proto.metrics.v1.internal.Exemplar.TRACE_ID, traceId);
      size +=
          MarshalerUtil.sizeRepeatedMessage(
              io.opentelemetry.proto.metrics.v1.internal.Exemplar.FILTERED_ATTRIBUTES,
              filteredAttributeMarshalers);
      return size;
    }
  }

  private static int mapToTemporality(
      io.opentelemetry.sdk.metrics.data.AggregationTemporality temporality) {
    switch (temporality) {
      case CUMULATIVE:
        return AggregationTemporality.AGGREGATION_TEMPORALITY_CUMULATIVE_VALUE;
      case DELTA:
        return AggregationTemporality.AGGREGATION_TEMPORALITY_DELTA_VALUE;
    }
    return AggregationTemporality.AGGREGATION_TEMPORALITY_UNSPECIFIED_VALUE;
  }
}
