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

package io.opentelemetry.sdk.contrib.trace.aws.resource;

import static com.google.common.truth.Truth.assertThat;
import static io.opentelemetry.common.AttributeValue.booleanAttributeValue;
import static io.opentelemetry.common.AttributeValue.stringAttributeValue;
import static org.mockito.Mockito.doAnswer;

import io.opentelemetry.common.AttributeValue;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Map;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.stubbing.Answer;

public class AwsResourceTest {

  @Rule public MockitoRule mocks = MockitoJUnit.rule();

  @Mock private AwsResource populator1;

  @Mock private AwsResource populator2;

  @Test
  public void createsResource() {
    doAnswer(
            new Answer<Void>() {
              @Override
              public Void answer(InvocationOnMock invocation) {
                Map<String, AttributeValue> attributes = invocation.getArgument(0);
                attributes.put("key1", stringAttributeValue("value1"));
                attributes.put("key2", booleanAttributeValue(true));
                return null;
              }
            })
        .when(populator1)
        .create(ArgumentMatchers.<String, AttributeValue>anyMap());
    doAnswer(
            new Answer<Void>() {
              @Override
              public Void answer(InvocationOnMock invocation) {
                Map<String, AttributeValue> attributes = invocation.getArgument(0);
                attributes.put("key3", stringAttributeValue("value2"));
                // Duplicate keys clobber but don't worry about it.
                attributes.put("key1", stringAttributeValue("value3"));
                return null;
              }
            })
        .when(populator2)
        .create(ArgumentMatchers.<String, AttributeValue>anyMap());

    Resource resource = AwsResource.create(populator1, populator2);
    assertThat(resource.getAttributes())
        .containsExactly(
            "key1", stringAttributeValue("value3"),
            "key2", booleanAttributeValue(true),
            "key3", stringAttributeValue("value2"));
  }
}
