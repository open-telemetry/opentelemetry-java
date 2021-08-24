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
  public void writeTo(CodedOutputStream output) throws IOException {
    MarshalerUtil.marshalRepeatedMessage(
        ExportMetricsServiceRequest.RESOURCE_METRICS_FIELD_NUMBER,
        resourceMetricsMarshalers,
        output);
  }

  private static int calculateSize(ResourceMetricsMarshaler[] resourceMetricsMarshalers) {
    int size = 0;
    size +=
        MarshalerUtil.sizeRepeatedMessage(
            ExportMetricsServiceRequest.RESOURCE_METRICS_FIELD_NUMBER, resourceMetricsMarshalers);
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
    public void writeTo(CodedOutputStream output) throws IOException {
      MarshalerUtil.marshalMessage(
          ResourceMetrics.RESOURCE_FIELD_NUMBER, resourceMarshaler, output);
      MarshalerUtil.marshalRepeatedMessage(
          ResourceMetrics.INSTRUMENTATION_LIBRARY_METRICS_FIELD_NUMBER,
          instrumentationLibraryMetricsMarshalers,
          output);
      MarshalerUtil.marshalBytes(ResourceMetrics.SCHEMA_URL_FIELD_NUMBER, schemaUrl, output);
    }

    private static int calculateSize(
        ResourceMarshaler resourceMarshaler,
        byte[] schemaUrl,
        InstrumentationLibraryMetricsMarshaler[] instrumentationLibraryMetricsMarshalers) {
      int size = 0;
      size += MarshalerUtil.sizeMessage(ResourceMetrics.RESOURCE_FIELD_NUMBER, resourceMarshaler);
      size += MarshalerUtil.sizeBytes(ResourceMetrics.SCHEMA_URL_FIELD_NUMBER, schemaUrl);
      size +=
          MarshalerUtil.sizeRepeatedMessage(
              ResourceMetrics.INSTRUMENTATION_LIBRARY_METRICS_FIELD_NUMBER,
              instrumentationLibraryMetricsMarshalers);
      return size;
    }
  }

  private static final class InstrumentationLibraryMetricsMarshaler extends MarshalerWithSize {
    private final InstrumentationLibraryMarshaler instrumentationLibrary;
    private final List<Marshaler> metricMarshalers;
    private final byte[] schemaUrl;

    private InstrumentationLibraryMetricsMarshaler(
        InstrumentationLibraryMarshaler instrumentationLibrary,
        byte[] schemaUrl,
        List<Marshaler> metricMarshalers) {
      super(calculateSize(instrumentationLibrary, schemaUrl, metricMarshalers));
      this.instrumentationLibrary = instrumentationLibrary;
      this.schemaUrl = schemaUrl;
      this.metricMarshalers = metricMarshalers;
    }

    @Override
    public void writeTo(CodedOutputStream output) throws IOException {
      MarshalerUtil.marshalMessage(
          InstrumentationLibraryMetrics.INSTRUMENTATION_LIBRARY_FIELD_NUMBER,
          instrumentationLibrary,
          output);
      MarshalerUtil.marshalRepeatedMessage(
          InstrumentationLibraryMetrics.METRICS_FIELD_NUMBER, metricMarshalers, output);
      MarshalerUtil.marshalBytes(
          InstrumentationLibraryMetrics.SCHEMA_URL_FIELD_NUMBER, schemaUrl, output);
    }

    private static int calculateSize(
        InstrumentationLibraryMarshaler instrumentationLibrary,
        byte[] schemaUrl,
        List<Marshaler> metricMarshalers) {
      int size = 0;
      size +=
          MarshalerUtil.sizeMessage(
              InstrumentationLibraryMetrics.INSTRUMENTATION_LIBRARY_FIELD_NUMBER,
              instrumentationLibrary);
      size +=
          MarshalerUtil.sizeBytes(InstrumentationLibraryMetrics.SCHEMA_URL_FIELD_NUMBER, schemaUrl);
      size +=
          MarshalerUtil.sizeRepeatedMessage(
              InstrumentationLibraryMetrics.METRICS_FIELD_NUMBER, metricMarshalers);
      return size;
    }
  }

  static final class MetricMarshaler extends MarshalerWithSize {
    private final byte[] name;
    private final byte[] description;
    private final byte[] unit;

    private final Marshaler dataMarshaler;
    private final int dataFieldNumber;

    static Marshaler create(MetricData metric) {
      // TODO(anuraaga): Cache these as they should be effectively singleton.
      byte[] name = MarshalerUtil.toBytes(metric.getName());
      byte[] description = MarshalerUtil.toBytes(metric.getDescription());
      byte[] unit = MarshalerUtil.toBytes(metric.getUnit());

      Marshaler dataMarshaler = null;
      int dataFieldNumber = -1;
      switch (metric.getType()) {
        case LONG_GAUGE:
          dataMarshaler = GaugeMarshaler.create(metric.getLongGaugeData());
          dataFieldNumber = Metric.GAUGE_FIELD_NUMBER;
          break;
        case DOUBLE_GAUGE:
          dataMarshaler = GaugeMarshaler.create(metric.getDoubleGaugeData());
          dataFieldNumber = Metric.GAUGE_FIELD_NUMBER;
          break;
        case LONG_SUM:
          dataMarshaler = SumMarshaler.create(metric.getLongSumData());
          dataFieldNumber = Metric.SUM_FIELD_NUMBER;
          break;
        case DOUBLE_SUM:
          dataMarshaler = SumMarshaler.create(metric.getDoubleSumData());
          dataFieldNumber = Metric.SUM_FIELD_NUMBER;
          break;
        case SUMMARY:
          dataMarshaler = SummaryMarshaler.create(metric.getDoubleSummaryData());
          dataFieldNumber = Metric.SUMMARY_FIELD_NUMBER;
          break;
        case HISTOGRAM:
          dataMarshaler = HistogramMarshaler.create(metric.getDoubleHistogramData());
          dataFieldNumber = Metric.HISTOGRAM_FIELD_NUMBER;
          break;
      }

      if (dataMarshaler == null) {
        // Someone not using BOM to align versions as we require. Just skip the metric.
        return NoopMarshaler.INSTANCE;
      }

      return new MetricMarshaler(name, description, unit, dataMarshaler, dataFieldNumber);
    }

    private MetricMarshaler(
        byte[] name,
        byte[] description,
        byte[] unit,
        Marshaler dataMarshaler,
        int dataFieldNumber) {
      super(calculateSize(name, description, unit, dataMarshaler, dataFieldNumber));
      this.name = name;
      this.description = description;
      this.unit = unit;
      this.dataMarshaler = dataMarshaler;
      this.dataFieldNumber = dataFieldNumber;
    }

    @Override
    public void writeTo(CodedOutputStream output) throws IOException {
      MarshalerUtil.marshalBytes(Metric.NAME_FIELD_NUMBER, name, output);
      MarshalerUtil.marshalBytes(Metric.DESCRIPTION_FIELD_NUMBER, description, output);
      MarshalerUtil.marshalBytes(Metric.UNIT_FIELD_NUMBER, unit, output);
      MarshalerUtil.marshalMessage(dataFieldNumber, dataMarshaler, output);
    }

    private static int calculateSize(
        byte[] name,
        byte[] description,
        byte[] unit,
        Marshaler dataMarshaler,
        int dataFieldNumber) {
      int size = 0;
      size += MarshalerUtil.sizeBytes(Metric.NAME_FIELD_NUMBER, name);
      size += MarshalerUtil.sizeBytes(Metric.DESCRIPTION_FIELD_NUMBER, description);
      size += MarshalerUtil.sizeBytes(Metric.UNIT_FIELD_NUMBER, unit);
      size += MarshalerUtil.sizeMessage(dataFieldNumber, dataMarshaler);
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
    public void writeTo(CodedOutputStream output) throws IOException {
      MarshalerUtil.marshalRepeatedMessage(Gauge.DATA_POINTS_FIELD_NUMBER, dataPoints, output);
    }

    private static int calculateSize(NumberDataPointMarshaler[] dataPoints) {
      int size = 0;
      size += MarshalerUtil.sizeRepeatedMessage(Gauge.DATA_POINTS_FIELD_NUMBER, dataPoints);
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
    public void writeTo(CodedOutputStream output) throws IOException {
      MarshalerUtil.marshalRepeatedMessage(Histogram.DATA_POINTS_FIELD_NUMBER, dataPoints, output);
      // TODO: Make this a MarshalerUtil helper.
      output.writeEnum(Histogram.AGGREGATION_TEMPORALITY_FIELD_NUMBER, aggregationTemporality);
    }

    private static int calculateSize(
        HistogramDataPointMarshaler[] dataPoints, int aggregationTemporality) {
      int size = 0;
      size += MarshalerUtil.sizeRepeatedMessage(Histogram.DATA_POINTS_FIELD_NUMBER, dataPoints);
      // TODO: Make this a MarshalerUtil helper.
      size +=
          CodedOutputStream.computeEnumSize(
              Histogram.AGGREGATION_TEMPORALITY_FIELD_NUMBER, aggregationTemporality);
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
    private final AttributeMarshaler[] attributes;

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
      AttributeMarshaler[] attributeMarshalers =
          AttributeMarshaler.createRepeated(point.getAttributes());
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
        AttributeMarshaler[] attributes) {
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
    public void writeTo(CodedOutputStream output) throws IOException {
      MarshalerUtil.marshalFixed64(
          HistogramDataPoint.START_TIME_UNIX_NANO_FIELD_NUMBER, startTimeUnixNano, output);
      MarshalerUtil.marshalFixed64(
          HistogramDataPoint.TIME_UNIX_NANO_FIELD_NUMBER, timeUnixNano, output);
      MarshalerUtil.marshalFixed64(HistogramDataPoint.COUNT_FIELD_NUMBER, count, output);
      MarshalerUtil.marshalDouble(HistogramDataPoint.SUM_FIELD_NUMBER, sum, output);
      MarshalerUtil.marshalRepeatedFixed64(
          HistogramDataPoint.BUCKET_COUNTS_FIELD_NUMBER, bucketCounts, output);
      MarshalerUtil.marshalRepeatedDouble(
          HistogramDataPoint.EXPLICIT_BOUNDS_FIELD_NUMBER, explicitBounds, output);
      MarshalerUtil.marshalRepeatedMessage(
          HistogramDataPoint.EXEMPLARS_FIELD_NUMBER, exemplars, output);
      MarshalerUtil.marshalRepeatedMessage(
          HistogramDataPoint.ATTRIBUTES_FIELD_NUMBER, attributes, output);
    }

    private static int calculateSize(
        long startTimeUnixNano,
        long timeUnixNano,
        long count,
        double sum,
        List<Long> bucketCounts,
        List<Double> explicitBounds,
        ExemplarMarshaler[] exemplars,
        AttributeMarshaler[] attributes) {
      int size = 0;
      size +=
          MarshalerUtil.sizeFixed64(
              HistogramDataPoint.START_TIME_UNIX_NANO_FIELD_NUMBER, startTimeUnixNano);
      size +=
          MarshalerUtil.sizeFixed64(HistogramDataPoint.TIME_UNIX_NANO_FIELD_NUMBER, timeUnixNano);
      size += MarshalerUtil.sizeFixed64(HistogramDataPoint.COUNT_FIELD_NUMBER, count);
      size += MarshalerUtil.sizeDouble(HistogramDataPoint.SUM_FIELD_NUMBER, sum);
      size +=
          MarshalerUtil.sizeRepeatedFixed64(
              HistogramDataPoint.BUCKET_COUNTS_FIELD_NUMBER, bucketCounts);
      size +=
          MarshalerUtil.sizeRepeatedDouble(
              HistogramDataPoint.EXPLICIT_BOUNDS_FIELD_NUMBER, explicitBounds);
      size +=
          MarshalerUtil.sizeRepeatedMessage(HistogramDataPoint.EXEMPLARS_FIELD_NUMBER, exemplars);
      size +=
          MarshalerUtil.sizeRepeatedMessage(HistogramDataPoint.ATTRIBUTES_FIELD_NUMBER, attributes);
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
    public void writeTo(CodedOutputStream output) throws IOException {
      MarshalerUtil.marshalRepeatedMessage(Sum.DATA_POINTS_FIELD_NUMBER, dataPoints, output);
      // TODO: Make this a MarshalerUtil helper.
      output.writeEnum(Sum.AGGREGATION_TEMPORALITY_FIELD_NUMBER, aggregationTemporality);
      MarshalerUtil.marshalBool(Sum.IS_MONOTONIC_FIELD_NUMBER, isMonotonic, output);
    }

    private static int calculateSize(
        NumberDataPointMarshaler[] dataPoints, int aggregationTemporality, boolean isMonotonic) {
      int size = 0;
      size += MarshalerUtil.sizeRepeatedMessage(Sum.DATA_POINTS_FIELD_NUMBER, dataPoints);
      // TODO: Make this a MarshalerUtil helper.
      size +=
          CodedOutputStream.computeEnumSize(
              Sum.AGGREGATION_TEMPORALITY_FIELD_NUMBER, aggregationTemporality);
      size += MarshalerUtil.sizeBool(Sum.IS_MONOTONIC_FIELD_NUMBER, isMonotonic);
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
    public void writeTo(CodedOutputStream output) throws IOException {
      MarshalerUtil.marshalRepeatedMessage(Summary.DATA_POINTS_FIELD_NUMBER, dataPoints, output);
    }

    private static int calculateSize(SummaryDataPointMarshaler[] dataPoints) {
      int size = 0;
      size += MarshalerUtil.sizeRepeatedMessage(Summary.DATA_POINTS_FIELD_NUMBER, dataPoints);
      return size;
    }
  }

  static class SummaryDataPointMarshaler extends MarshalerWithSize {
    private final long startTimeUnixNano;
    private final long timeUnixNano;
    private final long count;
    private final double sum;
    private final ValueAtQuantileMarshaler[] quantileValues;
    private final AttributeMarshaler[] attributes;

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
      AttributeMarshaler[] attributeMarshalers =
          AttributeMarshaler.createRepeated(point.getAttributes());

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
        AttributeMarshaler[] attributes) {
      super(calculateSize(startTimeUnixNano, timeUnixNano, count, sum, quantileValues, attributes));
      this.startTimeUnixNano = startTimeUnixNano;
      this.timeUnixNano = timeUnixNano;
      this.count = count;
      this.sum = sum;
      this.quantileValues = quantileValues;
      this.attributes = attributes;
    }

    @Override
    public void writeTo(CodedOutputStream output) throws IOException {
      MarshalerUtil.marshalFixed64(
          SummaryDataPoint.START_TIME_UNIX_NANO_FIELD_NUMBER, startTimeUnixNano, output);
      MarshalerUtil.marshalFixed64(
          SummaryDataPoint.TIME_UNIX_NANO_FIELD_NUMBER, timeUnixNano, output);
      MarshalerUtil.marshalFixed64(SummaryDataPoint.COUNT_FIELD_NUMBER, count, output);
      MarshalerUtil.marshalDouble(SummaryDataPoint.SUM_FIELD_NUMBER, sum, output);
      MarshalerUtil.marshalRepeatedMessage(
          SummaryDataPoint.QUANTILE_VALUES_FIELD_NUMBER, quantileValues, output);
      MarshalerUtil.marshalRepeatedMessage(
          SummaryDataPoint.ATTRIBUTES_FIELD_NUMBER, attributes, output);
    }

    private static int calculateSize(
        long startTimeUnixNano,
        long timeUnixNano,
        long count,
        double sum,
        ValueAtQuantileMarshaler[] quantileValues,
        AttributeMarshaler[] attributes) {
      int size = 0;
      size +=
          MarshalerUtil.sizeFixed64(
              SummaryDataPoint.START_TIME_UNIX_NANO_FIELD_NUMBER, startTimeUnixNano);
      size += MarshalerUtil.sizeFixed64(SummaryDataPoint.TIME_UNIX_NANO_FIELD_NUMBER, timeUnixNano);
      size += MarshalerUtil.sizeFixed64(SummaryDataPoint.COUNT_FIELD_NUMBER, count);
      size += MarshalerUtil.sizeDouble(SummaryDataPoint.SUM_FIELD_NUMBER, sum);
      size +=
          MarshalerUtil.sizeRepeatedMessage(
              SummaryDataPoint.QUANTILE_VALUES_FIELD_NUMBER, quantileValues);
      size +=
          MarshalerUtil.sizeRepeatedMessage(SummaryDataPoint.ATTRIBUTES_FIELD_NUMBER, attributes);
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
    public void writeTo(CodedOutputStream output) throws IOException {
      MarshalerUtil.marshalDouble(
          SummaryDataPoint.ValueAtQuantile.QUANTILE_FIELD_NUMBER, quantile, output);
      MarshalerUtil.marshalDouble(
          SummaryDataPoint.ValueAtQuantile.VALUE_FIELD_NUMBER, value, output);
    }

    private static int calculateSize(double quantile, double value) {
      int size = 0;
      size +=
          MarshalerUtil.sizeDouble(
              SummaryDataPoint.ValueAtQuantile.QUANTILE_FIELD_NUMBER, quantile);
      size += MarshalerUtil.sizeDouble(SummaryDataPoint.ValueAtQuantile.VALUE_FIELD_NUMBER, value);
      return size;
    }
  }

  static final class NumberDataPointMarshaler extends MarshalerWithSize {
    private final long startTimeUnixNano;
    private final long timeUnixNano;

    // Always fixed64, for a double it's the bits themselves.
    private final long value;
    private final int valueFieldNumber;

    private final ExemplarMarshaler[] exemplars;
    private final AttributeMarshaler[] attributes;

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
      AttributeMarshaler[] attributeMarshalers =
          AttributeMarshaler.createRepeated(point.getAttributes());

      final long value;
      final int valueFieldNumber;
      if (point instanceof LongPointData) {
        value = ((LongPointData) point).getValue();
        valueFieldNumber = NumberDataPoint.AS_INT_FIELD_NUMBER;
      } else {
        assert point instanceof DoublePointData;
        value = Double.doubleToRawLongBits(((DoublePointData) point).getValue());
        valueFieldNumber = NumberDataPoint.AS_DOUBLE_FIELD_NUMBER;
      }

      return new NumberDataPointMarshaler(
          point.getStartEpochNanos(),
          point.getEpochNanos(),
          value,
          valueFieldNumber,
          exemplarMarshalers,
          attributeMarshalers);
    }

    private NumberDataPointMarshaler(
        long startTimeUnixNano,
        long timeUnixNano,
        long value,
        int valueFieldNumber,
        ExemplarMarshaler[] exemplars,
        AttributeMarshaler[] attributes) {
      super(
          calculateSize(
              startTimeUnixNano, timeUnixNano, value, valueFieldNumber, exemplars, attributes));
      this.startTimeUnixNano = startTimeUnixNano;
      this.timeUnixNano = timeUnixNano;
      this.value = value;
      this.valueFieldNumber = valueFieldNumber;
      this.exemplars = exemplars;
      this.attributes = attributes;
    }

    @Override
    public void writeTo(CodedOutputStream output) throws IOException {
      MarshalerUtil.marshalFixed64(
          NumberDataPoint.START_TIME_UNIX_NANO_FIELD_NUMBER, startTimeUnixNano, output);
      MarshalerUtil.marshalFixed64(
          NumberDataPoint.TIME_UNIX_NANO_FIELD_NUMBER, timeUnixNano, output);
      MarshalerUtil.marshalFixed64(valueFieldNumber, value, output);
      MarshalerUtil.marshalRepeatedMessage(
          NumberDataPoint.EXEMPLARS_FIELD_NUMBER, exemplars, output);
      MarshalerUtil.marshalRepeatedMessage(
          NumberDataPoint.ATTRIBUTES_FIELD_NUMBER, attributes, output);
    }

    private static int calculateSize(
        long startTimeUnixNano,
        long timeUnixNano,
        long value,
        int valueFieldNumber,
        ExemplarMarshaler[] exemplars,
        AttributeMarshaler[] attributes) {
      int size = 0;
      size +=
          MarshalerUtil.sizeFixed64(
              NumberDataPoint.START_TIME_UNIX_NANO_FIELD_NUMBER, startTimeUnixNano);
      size += MarshalerUtil.sizeFixed64(NumberDataPoint.TIME_UNIX_NANO_FIELD_NUMBER, timeUnixNano);
      size += MarshalerUtil.sizeFixed64(valueFieldNumber, value);
      size += MarshalerUtil.sizeRepeatedMessage(NumberDataPoint.EXEMPLARS_FIELD_NUMBER, exemplars);
      size +=
          MarshalerUtil.sizeRepeatedMessage(NumberDataPoint.ATTRIBUTES_FIELD_NUMBER, attributes);
      return size;
    }
  }

  private static class ExemplarMarshaler extends MarshalerWithSize {

    private final long timeUnixNano;

    // Always fixed64, for a double it's the bits themselves.
    private final long value;
    private final int valueFieldNumber;

    private final byte[] spanId;
    private final byte[] traceId;

    private final AttributeMarshaler[] filteredAttributeMarshalers;

    static ExemplarMarshaler[] createRepeated(List<Exemplar> exemplars) {
      int numExemplars = exemplars.size();
      ExemplarMarshaler[] marshalers = new ExemplarMarshaler[numExemplars];
      for (int i = 0; i < numExemplars; i++) {
        marshalers[i] = ExemplarMarshaler.create(exemplars.get(i));
      }
      return marshalers;
    }

    private static ExemplarMarshaler create(Exemplar exemplar) {
      AttributeMarshaler[] attributeMarshalers =
          AttributeMarshaler.createRepeated(exemplar.getFilteredAttributes());

      final long value;
      final int valueFieldNumber;
      if (exemplar instanceof LongExemplar) {
        value = ((LongExemplar) exemplar).getValue();
        valueFieldNumber = io.opentelemetry.proto.metrics.v1.internal.Exemplar.AS_INT_FIELD_NUMBER;
      } else {
        assert exemplar instanceof DoubleExemplar;
        value = Double.doubleToRawLongBits(((DoubleExemplar) exemplar).getValue());
        valueFieldNumber =
            io.opentelemetry.proto.metrics.v1.internal.Exemplar.AS_DOUBLE_FIELD_NUMBER;
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
          exemplar.getEpochNanos(), value, valueFieldNumber, spanId, traceId, attributeMarshalers);
    }

    private ExemplarMarshaler(
        long timeUnixNano,
        long value,
        int valueFieldNumber,
        byte[] spanId,
        byte[] traceId,
        AttributeMarshaler[] filteredAttributeMarshalers) {
      super(
          calculateSize(
              timeUnixNano, value, valueFieldNumber, spanId, traceId, filteredAttributeMarshalers));
      this.timeUnixNano = timeUnixNano;
      this.value = value;
      this.valueFieldNumber = valueFieldNumber;
      this.spanId = spanId;
      this.traceId = traceId;
      this.filteredAttributeMarshalers = filteredAttributeMarshalers;
    }

    @Override
    public void writeTo(CodedOutputStream output) throws IOException {
      MarshalerUtil.marshalFixed64(
          io.opentelemetry.proto.metrics.v1.internal.Exemplar.TIME_UNIX_NANO_FIELD_NUMBER,
          timeUnixNano,
          output);
      MarshalerUtil.marshalFixed64(valueFieldNumber, value, output);
      MarshalerUtil.marshalBytes(
          io.opentelemetry.proto.metrics.v1.internal.Exemplar.SPAN_ID_FIELD_NUMBER, spanId, output);
      MarshalerUtil.marshalBytes(
          io.opentelemetry.proto.metrics.v1.internal.Exemplar.TRACE_ID_FIELD_NUMBER,
          traceId,
          output);
      MarshalerUtil.marshalRepeatedMessage(
          io.opentelemetry.proto.metrics.v1.internal.Exemplar.FILTERED_ATTRIBUTES_FIELD_NUMBER,
          filteredAttributeMarshalers,
          output);
    }

    private static int calculateSize(
        long timeUnixNano,
        long value,
        int valueFieldNumber,
        byte[] spanId,
        byte[] traceId,
        AttributeMarshaler[] filteredAttributeMarshalers) {
      int size = 0;
      size +=
          MarshalerUtil.sizeFixed64(
              io.opentelemetry.proto.metrics.v1.internal.Exemplar.TIME_UNIX_NANO_FIELD_NUMBER,
              timeUnixNano);
      size += MarshalerUtil.sizeFixed64(valueFieldNumber, value);
      size +=
          MarshalerUtil.sizeBytes(
              io.opentelemetry.proto.metrics.v1.internal.Exemplar.SPAN_ID_FIELD_NUMBER, spanId);
      size +=
          MarshalerUtil.sizeBytes(
              io.opentelemetry.proto.metrics.v1.internal.Exemplar.TRACE_ID_FIELD_NUMBER, traceId);
      size +=
          MarshalerUtil.sizeRepeatedMessage(
              io.opentelemetry.proto.metrics.v1.internal.Exemplar.FILTERED_ATTRIBUTES_FIELD_NUMBER,
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
