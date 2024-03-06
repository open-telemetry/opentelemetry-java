/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.marshal;

import java.util.List;

public interface MessageSize {

  /** Returns the size of the protobuf-encoded message in bytes. */
  int getEncodedSize();

  /** Returns the size of the message-type fields in the message. */
  List<MessageSize> getMessageTypedFieldSizes();

  /**
   * Returns the size of the field at the given position.
   *
   * <p>A message is composed of message-type fields or primitive fields. For example:
   *
   * <pre>
   *   message MyMessage {
   *   string field1 = 1;
   *   Address field2 = 2;
   *   int32 field3 = 3;
   *   FullName field4 = 4;
   *   }
   * </pre>
   *
   * <p>The field sizes that are returned are the sizes of the embedded message types only. In the
   * above example, {@code getMessageTypeFieldSize(0)} will return the size of {@code field2} and
   * {@code getMessageTypeFieldSize(1)} will return the size of {@code field4}.
   *
   * @param messageFieldPosition The embedded message sequence number, starting from 0.
   */
  MessageSize getMessageTypeFieldSize(int messageFieldPosition);
}
