/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.processcontext.data;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.resources.Resource;
import javax.annotation.concurrent.Immutable;

/**
 * Describes a ProcessContext.
 *
 * @see <a
 *     href="https://github.com/open-telemetry/opentelemetry-specification/blob/main/oteps/profiles/4719-process-ctx.md#payload-format">
 *     OTEP-4719 Process Context : Payload</a>
 */
@Immutable
public interface ProcessContextData {

  /**
   * Returns a new ProcessContextData encapsulating the given Resource and supplemental Attributes.
   *
   * @return a new ProcessContextData.
   */
  @SuppressWarnings("AutoValueSubclassLeaked")
  static ProcessContextData create(Resource resource, Attributes attributes) {
    return new AutoValue_ImmutableProcessContextData(resource, attributes);
  }

  /** Returns the resource of this process. */
  Resource getResource();

  /** Additional attributes that are not part of the Resource. */
  Attributes getAttributes();
}
