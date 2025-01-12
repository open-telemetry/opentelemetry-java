/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs.data.internal;

import io.opentelemetry.sdk.logs.data.LogRecordData;
import javax.annotation.Nullable;

/**
 * This class is internal and experimental. Its APIs are unstable and can change at any time. Its
 * APIs (or a version of them) may be promoted to the public stable API in the future, but no
 * guarantees are made.
 */
public interface ExtendedLogRecordData extends LogRecordData {

  // keep this class even if it is empty, since experimental methods may be added in the future.

  @Nullable
  String getEventName();
}
