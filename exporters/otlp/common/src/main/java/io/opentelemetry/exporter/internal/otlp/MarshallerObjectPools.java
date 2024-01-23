package io.opentelemetry.exporter.internal.otlp;

import io.opentelemetry.exporter.internal.otlp.metrics.MutableExemplarMarshaler;
import io.opentelemetry.exporter.internal.otlp.metrics.MutableInstrumentationScopeMetricsMarshaler;
import io.opentelemetry.exporter.internal.otlp.metrics.MutableMetricMarshaler;
import io.opentelemetry.exporter.internal.otlp.metrics.MutableMetricsRequestMarshaler;
import io.opentelemetry.exporter.internal.otlp.metrics.MutableNumberDataPointMarshaler;
import io.opentelemetry.exporter.internal.otlp.metrics.MutableResourceMetricsMarshaler;
import io.opentelemetry.exporter.internal.otlp.metrics.MutableSumMarshaler;
import io.opentelemetry.sdk.internal.DynamicList;
import io.opentelemetry.sdk.metrics.internal.state.ObjectPool;

public final class MarshallerObjectPools {
  private final ObjectPool<DynamicList> dynamicListObjectPool
      = new ObjectPool<>(DynamicList::new);
  private final ObjectPool<MutableNumberDataPointMarshaler> mutableNumberDataPointMarshalerPool
      = new ObjectPool<>(MutableNumberDataPointMarshaler::new);
  private final ObjectPool<MutableExemplarMarshaler> mutableExemplarMarshallerPool
      = new ObjectPool<>(MutableExemplarMarshaler::new);
  private final ObjectPool<MutableMetricsRequestMarshaler> mutableMetricsRequestMarshallerPool
      = new ObjectPool<>(MutableMetricsRequestMarshaler::new);
  private final ObjectPool<MutableKeyValueMarshaler> mutableKeyValueMarshallerPool
      = new ObjectPool<>(MutableKeyValueMarshaler::new);
  private final ObjectPool<MutableResourceMetricsMarshaler> mutableResourceMetricsMarshalerPool
      = new ObjectPool<>(MutableResourceMetricsMarshaler::new);
  private final ObjectPool<MutableInstrumentationScopeMetricsMarshaler>
      mutableInstrumentationScopeMetricsMarshalerPool
      = new ObjectPool<>(MutableInstrumentationScopeMetricsMarshaler::new);
  private final ObjectPool<MutableMetricMarshaler> mutableMetricMarshalerPool
      = new ObjectPool<>(MutableMetricMarshaler::new);
  private final ObjectPool<MutableSumMarshaler> mutableSumMarshalerPool
      = new ObjectPool<>(MutableSumMarshaler::new);


  // Accepts listSize to enable choosing the right size reusable list from the pool
  // in the future
  @SuppressWarnings({"rawtypes", "unchecked"})
  public <T> DynamicList<T> borrowDynamicList(int listSize) {
    DynamicList dynamicList = dynamicListObjectPool.borrowObject();
    dynamicList.resizeAndClear(listSize);
    return dynamicList;
  }

  @SuppressWarnings("rawtypes")
  void returnDynamicList(DynamicList dynamicList) {
    dynamicListObjectPool.returnObject(dynamicList);
  }

  public ObjectPool<MutableNumberDataPointMarshaler> getMutableNumberDataPointMarshallerPool() {
    return mutableNumberDataPointMarshalerPool;
  }

  public ObjectPool<MutableExemplarMarshaler> getMutableExemplarMarshallerPool() {
    return mutableExemplarMarshallerPool;
  }

  public ObjectPool<MutableMetricsRequestMarshaler> getMutableMetricsRequestMarshallerPool() {
    return mutableMetricsRequestMarshallerPool;
  }

  public ObjectPool<MutableKeyValueMarshaler> getMutableKeyValueMarshallerPool() {
    return mutableKeyValueMarshallerPool;
  }

  public ObjectPool<MutableResourceMetricsMarshaler> getMutableResourceMetricsMarshallerPool() {
    return mutableResourceMetricsMarshalerPool;
  }

  public ObjectPool<MutableInstrumentationScopeMetricsMarshaler>
  getMutableInstrumentationScopeMetricsMarshalerPool() {
    return mutableInstrumentationScopeMetricsMarshalerPool;
  }

  public ObjectPool<MutableMetricMarshaler> getMutableMetricMarshalerPool() {
    return mutableMetricMarshalerPool;
  }

  public ObjectPool<MutableSumMarshaler> getMutableSumMarshalerPool() {
    return mutableSumMarshalerPool;
  }
}
