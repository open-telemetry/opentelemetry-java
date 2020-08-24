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

package io.opentelemetry.sdk.extensions.resources;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.common.Attributes;
import io.opentelemetry.common.ReadableAttributes;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.resources.ResourceConstants;
import org.junit.jupiter.api.Test;

class ProcessResourceTest {

  private static final ProcessResource RESOURCE = new ProcessResource();

  @Test
  void normal() {
    Attributes attributes = RESOURCE.getAttributes();

    assertThat(attributes.get(ResourceConstants.PROCESS_PID).getLongValue()).isGreaterThan(1);
    assertThat(attributes.get(ResourceConstants.PROCESS_EXECUTABLE_PATH).getStringValue())
        .contains("java");
    assertThat(attributes.get(ResourceConstants.PROCESS_COMMAND_LINE).getStringValue())
        .contains(attributes.get(ResourceConstants.PROCESS_EXECUTABLE_PATH).getStringValue());
  }

  @Test
  void inDefault() {
    ReadableAttributes attributes = Resource.getDefault().getAttributes();
    assertThat(attributes.get(ResourceConstants.PROCESS_PID)).isNotNull();
    assertThat(attributes.get(ResourceConstants.PROCESS_EXECUTABLE_PATH)).isNotNull();
    assertThat(attributes.get(ResourceConstants.PROCESS_COMMAND_LINE)).isNotNull();
  }
}
