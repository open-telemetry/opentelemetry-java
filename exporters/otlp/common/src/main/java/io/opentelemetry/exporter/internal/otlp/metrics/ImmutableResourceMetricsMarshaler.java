package io.opentelemetry.exporter.internal.otlp.metrics;

import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.otlp.InstrumentationScopeMarshaler;
import io.opentelemetry.exporter.internal.otlp.ResourceMarshaler;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.resources.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ImmutableResourceMetricsMarshaler extends ResourceMetricsMarshaler {
  private final ResourceMarshaler resourceMarshaler;
  private final String schemaUrl;
  private final List<InstrumentationScopeMetricsMarshaler> instrumentationScopeMetricsMarshalers;
  private final int size;


  /** Returns Marshalers of ResourceMetrics created by grouping the provided metricData. */
  @SuppressWarnings("AvoidObjectArrays")
  public static List<ResourceMetricsMarshaler> create(Collection<MetricData> metricDataList) {
    Map<Resource, Map<InstrumentationScopeInfo, List<Marshaler>>> resourceAndScopeMap =
        groupByResourceAndScope(metricDataList);

    List<ResourceMetricsMarshaler> resourceMetricsMarshalers =
        new ArrayList<>(resourceAndScopeMap.size());

    for (Map.Entry<Resource, Map<InstrumentationScopeInfo, List<Marshaler>>> entry :
        resourceAndScopeMap.entrySet()) {
      List<InstrumentationScopeMetricsMarshaler> instrumentationScopeMetricsMarshalerArrayList =
          new ArrayList<>(entry.getValue().size());
      for (Map.Entry<InstrumentationScopeInfo, List<Marshaler>> entryIs :
          entry.getValue().entrySet()) {
        instrumentationScopeMetricsMarshalerArrayList.add(
            new ImmutableInstrumentationScopeMetricsMarshaler(
                InstrumentationScopeMarshaler.create(entryIs.getKey()),
                entryIs.getKey().getSchemaUrl(),
                entryIs.getValue()));
      }
      resourceMetricsMarshalers.add(
          new ImmutableResourceMetricsMarshaler(
              ResourceMarshaler.create(entry.getKey()),
              entry.getKey().getSchemaUrl(),
              instrumentationScopeMetricsMarshalerArrayList));
    }

    return resourceMetricsMarshalers;
  }

  protected static Map<Resource, Map<InstrumentationScopeInfo, List<Marshaler>>>
  groupByResourceAndScope(Collection<MetricData> metricDataList) {
    return MarshalerUtil.groupByResourceAndScope(
        metricDataList,
        // TODO(anuraaga): Replace with an internal SdkData type of interface that exposes these
        // two.
        MetricData::getResource,
        MetricData::getInstrumentationScopeInfo,
        ImmutableMetricMarshaler::create);
  }

  public ImmutableResourceMetricsMarshaler(
      ResourceMarshaler resourceMarshaler,
      String schemaUrl,
      List<InstrumentationScopeMetricsMarshaler> instrumentationScopeMetricsMarshalers) {
    this.resourceMarshaler = resourceMarshaler;
    this.schemaUrl = schemaUrl;
    this.instrumentationScopeMetricsMarshalers = instrumentationScopeMetricsMarshalers;
    this.size = calculateSize(resourceMarshaler, schemaUrl, instrumentationScopeMetricsMarshalers);
  }

  @Override
  public int getBinarySerializedSize() {
    return size;
  }

  @Override
  protected ResourceMarshaler getResourceMarshaler() {
    return resourceMarshaler;
  }

  @Override
  protected String getSchemaUrl() {
    return schemaUrl;
  }

  @Override
  protected List<InstrumentationScopeMetricsMarshaler> getInstrumentationScopeMetricsMarshalers() {
    return instrumentationScopeMetricsMarshalers;
  }
}
