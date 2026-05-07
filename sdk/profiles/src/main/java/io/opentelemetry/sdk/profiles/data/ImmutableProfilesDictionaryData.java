/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.profiles.data;

import com.google.auto.value.AutoValue;
import javax.annotation.concurrent.Immutable;

/**
 * Auto value implementation of {@link ProfilesDictionaryData}, which represents profiles data
 * shared across the entire message being sent.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
@Immutable
@AutoValue
abstract class ImmutableProfilesDictionaryData implements ProfilesDictionaryData {

  ImmutableProfilesDictionaryData() {}
}
