/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.profiles.data;

import com.google.auto.value.AutoValue;
import javax.annotation.concurrent.Immutable;

/**
 * Auto value implementation of {@link LineData}, which details a specific line in a source code,
 * linked to a function.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
@Immutable
@AutoValue
abstract class ImmutableLineData implements LineData {

  ImmutableLineData() {}
}
