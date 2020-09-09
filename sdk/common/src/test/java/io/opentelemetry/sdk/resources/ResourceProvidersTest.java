/*
 * Copyright 2020, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.sdk.resources;

import static io.opentelemetry.common.AttributeKeyImpl.longKey;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ResourceProvidersTest {

  @Test
  void default_resource_includes_attributes_from_providers() {
    Resource resource = Resource.getDefault();

    long providerAttribute = resource.getAttributes().get(longKey("providerAttribute"));
    assertThat(providerAttribute).isNotNull();
    assertThat(providerAttribute).isEqualTo(42);
  }
}
