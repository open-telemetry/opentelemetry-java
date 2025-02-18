/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs.data.internal;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.incubator.common.ExtendedAttributes;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import javax.annotation.Nullable;

/**
 * This class is internal and experimental. Its APIs are unstable and can change at any time. Its
 * APIs (or a version of them) may be promoted to the public stable API in the future, but no
 * guarantees are made.
 */
public interface ExtendedLogRecordData extends LogRecordData {

  @Nullable
  String getEventName();

  /** TODO. */
  ExtendedAttributes getExtendedAttributes();

  /** TODO. */
  @Override
  default Attributes getAttributes() {
    return getExtendedAttributes().asAttributes();
  }
}
