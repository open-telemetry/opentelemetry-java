/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp.metrics;

import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.internal.marshal.MarshalerContext;
import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.MarshalerWithSize;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.exporter.internal.otlp.AbstractScopeListSizeCalculator;
import io.opentelemetry.exporter.internal.otlp.AbstractScopeListWriter;
import io.opentelemetry.exporter.internal.otlp.InstrumentationScopeMarshaler;
import io.opentelemetry.exporter.internal.otlp.ResourceMarshaler;
import io.opentelemetry.proto.metrics.v1.internal.ResourceMetrics;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.resources.Resource;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * A Marshaler of ResourceMetrics.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class ResourceMetricsMarshaler extends MarshalerWithSize {
  private final ResourceMarshaler resourceMarshaler;
  private final byte[] schemaUrl;
  private final InstrumentationScopeMetricsMarshaler[] instrumentationScopeMetricsMarshalers;

  /** Returns Marshalers of ResourceMetrics created by grouping the provided metricData. */
  @SuppressWarnings("AvoidObjectArrays")
  public static ResourceMetricsMarshaler[] create(Collection<MetricData> metricDataList) {
    Map<Resource, Map<InstrumentationScopeInfo, List<Marshaler>>> resourceAndScopeMap =
        groupByResourceAndScope(metricDataList);

    ResourceMetricsMarshaler[] resourceMetricsMarshalers =
        new ResourceMetricsMarshaler[resourceAndScopeMap.size()];
    int posResource = 0;
    for (Map.Entry<Resource, Map<InstrumentationScopeInfo, List<Marshaler>>> entry :
        resourceAndScopeMap.entrySet()) {
      InstrumentationScopeMetricsMarshaler[] instrumentationLibrarySpansMarshalers =
          new InstrumentationScopeMetricsMarshaler[entry.getValue().size()];
      int posInstrumentation = 0;
      for (Map.Entry<InstrumentationScopeInfo, List<Marshaler>> entryIs :
          entry.getValue().entrySet()) {
        instrumentationLibrarySpansMarshalers[posInstrumentation++] =
            new InstrumentationScopeMetricsMarshaler(
                InstrumentationScopeMarshaler.create(entryIs.getKey()),
                MarshalerUtil.toBytes(entryIs.getKey().getSchemaUrl()),
                entryIs.getValue());
      }
      resourceMetricsMarshalers[posResource++] =
          new ResourceMetricsMarshaler(
              ResourceMarshaler.create(entry.getKey()),
              MarshalerUtil.toBytes(entry.getKey().getSchemaUrl()),
              instrumentationLibrarySpansMarshalers);
    }

    return resourceMetricsMarshalers;
  }

  ResourceMetricsMarshaler(
      ResourceMarshaler resourceMarshaler,
      byte[] schemaUrl,
      InstrumentationScopeMetricsMarshaler[] instrumentationScopeMetricsMarshalers) {
    super(calculateSize(resourceMarshaler, schemaUrl, instrumentationScopeMetricsMarshalers));
    this.resourceMarshaler = resourceMarshaler;
    this.schemaUrl = schemaUrl;
    this.instrumentationScopeMetricsMarshalers = instrumentationScopeMetricsMarshalers;
  }

  @Override
  public void writeTo(Serializer output) throws IOException {
    output.serializeMessage(ResourceMetrics.RESOURCE, resourceMarshaler);
    output.serializeRepeatedMessage(
        ResourceMetrics.SCOPE_METRICS, instrumentationScopeMetricsMarshalers);
    output.serializeString(ResourceMetrics.SCHEMA_URL, schemaUrl);
  }

  public static void writeTo(
      Serializer output,
      Map<InstrumentationScopeInfo, List<MetricData>> scopeMap,
      MarshalerContext context)
      throws IOException {
    ResourceMarshaler resourceMarshaler = context.getObject(ResourceMarshaler.class);
    output.serializeMessage(ResourceMetrics.RESOURCE, resourceMarshaler);

    ScopeMetricListWriter scopeMetricListWriter =
        context.getInstance(ScopeMetricListWriter.class, ScopeMetricListWriter::new);
    scopeMetricListWriter.initialize(output, ResourceMetrics.SCOPE_METRICS, context);
    scopeMap.forEach(scopeMetricListWriter);

    byte[] schemaUrlUtf8 = context.getByteArray();
    output.serializeString(ResourceMetrics.SCHEMA_URL, schemaUrlUtf8);
  }

  private static int calculateSize(
      ResourceMarshaler resourceMarshaler,
      byte[] schemaUrl,
      InstrumentationScopeMetricsMarshaler[] instrumentationScopeMetricsMarshalers) {
    int size = 0;
    size += MarshalerUtil.sizeMessage(ResourceMetrics.RESOURCE, resourceMarshaler);
    size += MarshalerUtil.sizeBytes(ResourceMetrics.SCHEMA_URL, schemaUrl);
    size +=
        MarshalerUtil.sizeRepeatedMessage(
            ResourceMetrics.SCOPE_METRICS, instrumentationScopeMetricsMarshalers);
    return size;
  }

  public static int calculateSize(
      MarshalerContext context,
      Resource resource,
      Map<InstrumentationScopeInfo, List<MetricData>> scopeMap) {

    int size = 0;
    int sizeIndex = context.addSize();

    ResourceMarshaler resourceMarshaler = ResourceMarshaler.create(resource);
    context.addData(resourceMarshaler);
    size += MarshalerUtil.sizeMessage(ResourceMetrics.RESOURCE, resourceMarshaler);

    ScopeMetricListSizeCalculator scopeMetricListSizeCalculator =
        context.getInstance(
            ScopeMetricListSizeCalculator.class, ScopeMetricListSizeCalculator::new);
    scopeMetricListSizeCalculator.initialize(ResourceMetrics.SCOPE_METRICS, context);
    scopeMap.forEach(scopeMetricListSizeCalculator);
    size += scopeMetricListSizeCalculator.getSize();

    byte[] schemaUrlUtf8 = MarshalerUtil.toBytes(resource.getSchemaUrl());
    context.addData(schemaUrlUtf8);
    size += MarshalerUtil.sizeBytes(ResourceMetrics.SCHEMA_URL, schemaUrlUtf8);

    context.setSize(sizeIndex, size);

    return size;
  }

  private static Map<Resource, Map<InstrumentationScopeInfo, List<Marshaler>>>
      groupByResourceAndScope(Collection<MetricData> metricDataList) {
    return MarshalerUtil.groupByResourceAndScope(
        metricDataList,
        // TODO(anuraaga): Replace with an internal SdkData type of interface that exposes these
        // two.
        MetricData::getResource,
        MetricData::getInstrumentationScopeInfo,
        MetricMarshaler::create);
  }

  private static class ScopeMetricListWriter extends AbstractScopeListWriter<MetricData> {

    @Override
    protected void handle(
        InstrumentationScopeMarshaler instrumentationScopeMarshaler,
        List<MetricData> list,
        byte[] schemaUrlUtf8)
        throws IOException {
      InstrumentationScopeMetricsMarshaler.writeTo(
          output, context, instrumentationScopeMarshaler, list, schemaUrlUtf8);
    }
  }

  private static class ScopeMetricListSizeCalculator
      extends AbstractScopeListSizeCalculator<MetricData> {

    @Override
    public int calculateSize(
        InstrumentationScopeMarshaler instrumentationScopeMarshaler,
        byte[] schemaUrlUtf8,
        List<MetricData> list) {
      return InstrumentationScopeMetricsMarshaler.calculateSize(
          instrumentationScopeMarshaler, schemaUrlUtf8, context, list);
    }
  }
}
