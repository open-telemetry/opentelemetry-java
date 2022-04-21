package io.opentelemetry.sdk.metrics.internal.exemplar;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.metrics.data.ExemplarData;
import java.util.List;

public interface ExemplarReservoir<T extends ExemplarData> {

  /** An exemplar reservoir that stores no exemplars. */
  // Empty reservoir so the concrete type does not matter.
  @SuppressWarnings("unchecked")
  static <T extends ExemplarData> ExemplarReservoir<T> noSamples() {
    return (ExemplarReservoir<T>) NoopDoubleExemplarReservoir.INSTANCE;
  }

  /**
   * Returns an immutable list of Exemplars for exporting from the current reservoir.
   *
   * <p>Additionally, clears the reservoir for the next sampling period.
   *
   * @param pointAttributes the {@link Attributes} associated with the metric point. {@link
   *     ExemplarData}s should filter these out of their final data state.
   * @return An (immutable) list of sampled exemplars for this point. Implementers are expected to
   *     filter out {@code pointAttributes} from the original recorded attributes.
   */
  List<T> collectAndReset(Attributes pointAttributes);
}
