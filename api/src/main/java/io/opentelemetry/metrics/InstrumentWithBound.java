package io.opentelemetry.metrics;

/**
 * Base interface for all metrics with bounds defined in this package.
 *
 * @param <B> the specific type of Bound Instrument this instrument can provide.
 * @since 0.1.0
 */
public interface InstrumentWithBound<B> extends Instrument {
  /**
   * Returns a {@code Bound Instrument} associated with the specified {@code labelSet}. Multiples
   * requests with the same {@code labelSet} may return the same {@code Bound Instrument} instance.
   *
   * <p>It is recommended that callers keep a reference to the Bound Instrument instead of always
   * calling this method for every operation.
   *
   * @param labelSet the set of labels.
   * @return a {@code Bound Instrument}
   * @throws NullPointerException if {@code labelValues} is null.
   * @since 0.1.0
   */
  B bind(LabelSet labelSet);

  /**
   * Removes the {@code Bound Instrument} from the Instrument. i.e. references to previous {@code
   * Bound Instrument} are invalid (not being managed by the instrument).
   *
   * @param boundInstrument the {@code Bound Instrument} to be removed.
   * @since 0.1.0
   */
  void unbind(B boundInstrument);
}
