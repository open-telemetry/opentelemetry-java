/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.spi;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collections;
import java.util.Set;

/**
 * A service provider interface (SPI) for providing a {@link Resource} that is merged into the
 * {@linkplain Resource#getDefault() default resource}.
 */
public interface ResourceProvider extends Ordered {

  Resource createResource(ConfigProperties config);

  /**
   * Returns the set of attribute keys that this provider supports. This is used to determine if a
   * provider should be used to create a resource.
   *
   * @return the set of attribute keys that this provider supports - an empty set indicates that the
   *     provider should always be used
   */
  default Set<AttributeKey<?>> supportedKeys() {
    return Collections.emptySet();
  }
}
