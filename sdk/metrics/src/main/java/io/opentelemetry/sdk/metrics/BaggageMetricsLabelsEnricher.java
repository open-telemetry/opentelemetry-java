package io.opentelemetry.sdk.metrics;

import io.opentelemetry.baggage.DefaultBaggageManager;
import io.opentelemetry.common.ReadWriteLabels;
import java.util.Set;

public class BaggageMetricsLabelsEnricher implements MetricsProcessor {
  private final Set<String> prefixes;

  public BaggageMetricsLabelsEnricher(Set<String> prefixes) {
    this.prefixes = prefixes;
  }

  private void enrichLabelsFromBaggage(ReadWriteLabels labels) {
    DefaultBaggageManager.getInstance().getCurrentBaggage().getEntries().stream().filter(e ->
        prefixes.contains(e.getKey().substring(0, e.getKey().indexOf('.'))))
        .forEach(e -> labels.put(e.getKey(), e.getValue()));
  }

  @Override
  public void onMetricRecorded(AbstractSynchronousInstrument<?> instr, ReadWriteLabels labels,
      Object value) {
    // do nothing, we already enrich labels in onLabelsBound()
  }

  @Override
  public void onLabelsBound(AbstractSynchronousInstrument<?> instr, ReadWriteLabels labels) {
    enrichLabelsFromBaggage(labels);
  }
}
