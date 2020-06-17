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

package io.opentelemetry.sdk.extensions.trace.aws.resource;

import static com.google.common.truth.Truth.assertThat;
import static io.opentelemetry.common.AttributeValue.booleanAttributeValue;
import static io.opentelemetry.common.AttributeValue.stringAttributeValue;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import io.opentelemetry.sdk.resources.Resource;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class AwsResourceTest {

  @Rule public MockitoRule mocks = MockitoJUnit.rule();

  @Mock private AwsResource populator1;

  @Mock private AwsResource populator2;

  @Test
  public void createsResource() {
    when(populator1.createAttributes())
        .thenReturn(
            ImmutableMap.of(
                "key1", stringAttributeValue("value1"), "key2", booleanAttributeValue(true)));
    when(populator2.createAttributes())
        .thenReturn(
            ImmutableMap.of(
                "key3", stringAttributeValue("value2"), "key1", stringAttributeValue("value3")));

    Resource resource = AwsResource.create(populator1, populator2);
    assertThat(resource.getAttributes())
        .containsExactly(
            "key1", stringAttributeValue("value3"),
            "key2", booleanAttributeValue(true),
            "key3", stringAttributeValue("value2"));
  }
}
