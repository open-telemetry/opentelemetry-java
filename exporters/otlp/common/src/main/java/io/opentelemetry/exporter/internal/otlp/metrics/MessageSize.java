package io.opentelemetry.exporter.internal.otlp.metrics;

public interface MessageSize {

  /**
   * @return the size of the protobuf-encoded message in bytes.
   */
  long getEncodedSize();

  /**
   * Returns the size of the field at the given position.
   * <p>
   * A message is composed of message-type fields or primitive fields.
   * For example:
   * <pre>
   *   message MyMessage {
   *   string field1 = 1;
   *   Address field2 = 2;
   *   int32 field3 = 3;
   *   FullName field4 = 4;
   *   }
   * </pre>
   * <p>
   *   The field sizes that are returned are the sizes of the embedded message types only.
   *   In the above example, {@code getFieldSize(0)} will return the size of {@code field2}
   *   and {@code getFieldSize(1)} will return the size of {@code field4}.
   * </p>
   *
   * @param fieldPosition The embedded message sequence number, starting from 0.
   */
  MessageSize getFieldSize(int fieldPosition);
}
