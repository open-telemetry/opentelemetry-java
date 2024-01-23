package io.opentelemetry.exporter.internal.otlp.metrics;

import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.otlp.InstrumentationScopeMarshaler;
import io.opentelemetry.exporter.internal.otlp.MarshallerObjectPools;
import io.opentelemetry.exporter.internal.otlp.ResourceMarshaler;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.internal.DynamicList;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class MutableResourceMetricsMarshaler extends ResourceMetricsMarshaler {
  private ResourceMarshaler resourceMarshaler;
  private String schemaUrl;
  private DynamicList<InstrumentationScopeMetricsMarshaler> instrumentationScopeMetricsMarshalers
      = DynamicList.empty();
  private int size;

  /** Returns Marshalers of ResourceMetrics created by grouping the provided metricData. */
  public static void createIntoDynamicList(Collection<MetricData> metricDataList,
      DynamicList<ResourceMetricsMarshaler> resourceMetricsMarshalers,
      MarshallerObjectPools marshallerObjectPools) {

    Map<Resource, Map<InstrumentationScopeInfo, List<Marshaler>>> resourceAndScopeMap =
        groupByResourceAndScope(metricDataList, marshallerObjectPools);

    resourceMetricsMarshalers.resizeAndClear(resourceAndScopeMap.size());

    for (Map.Entry<Resource, Map<InstrumentationScopeInfo, List<Marshaler>>> entry :
        resourceAndScopeMap.entrySet()) {
      MutableResourceMetricsMarshaler mutableResourceMetricsMarshaler = marshallerObjectPools
          .getMutableResourceMetricsMarshallerPool()
          .borrowObject();

      DynamicList<InstrumentationScopeMetricsMarshaler> instrumentationScopeMetricsMarshalersList =
          mutableResourceMetricsMarshaler.instrumentationScopeMetricsMarshalers;
      instrumentationScopeMetricsMarshalersList.resizeAndClear(entry.getValue().size());

      for (Map.Entry<InstrumentationScopeInfo, List<Marshaler>> entryIs :
          entry.getValue().entrySet()) {
        MutableInstrumentationScopeMetricsMarshaler mutableInstrumentationScopeMetricsMarshaler =
            marshallerObjectPools
                .getMutableInstrumentationScopeMetricsMarshalerPool()
                .borrowObject();

        mutableInstrumentationScopeMetricsMarshaler.set(
            // Has internal cache and probably low cardinality hence no need to make it mutable
            InstrumentationScopeMarshaler.create(entryIs.getKey()),

            entryIs.getKey().getSchemaUrl(),
            entryIs.getValue());

        instrumentationScopeMetricsMarshalersList.add(mutableInstrumentationScopeMetricsMarshaler);
      }

      mutableResourceMetricsMarshaler.set(
          // Has internal cache and probably low cardinality hence no need to make it mutable
          ResourceMarshaler.create(entry.getKey()),

          entry.getKey().getSchemaUrl(),
          instrumentationScopeMetricsMarshalersList);

      resourceMetricsMarshalers.add(mutableResourceMetricsMarshaler);
    }
  }

  protected static Map<Resource, Map<InstrumentationScopeInfo, List<Marshaler>>>
  groupByResourceAndScope(
      Collection<MetricData> metricDataList,
      MarshallerObjectPools marshallerObjectPools) {
    // TODO Asaf: Change the implementation to accept a provider of list so we can use dynamic list
    // or duplicate the logic
    return MarshalerUtil.groupByResourceAndScope(
        metricDataList,
        // TODO(anuraaga): Replace with an internal SdkData type of interface that exposes these
        // two.
        MetricData::getResource,
        MetricData::getInstrumentationScopeInfo,
        metricData -> MutableMetricMarshaler.create(metricData, marshallerObjectPools));
  }

  public MutableResourceMetricsMarshaler() {
  }

  private void set(
      ResourceMarshaler resourceMarshaler,
      String schemaUrl,
      DynamicList<InstrumentationScopeMetricsMarshaler> instrumentationScopeMetricsMarshalers) {
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
