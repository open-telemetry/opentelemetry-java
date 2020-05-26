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

import static com.google.common.truth.Truth.assertThat;
import static io.opentelemetry.common.AttributeValue.stringAttributeValue;

import com.google.common.collect.ImmutableMap;
import io.opentelemetry.common.AttributeValue;
import java.util.Map;
import org.junit.Test;

/** Tests for the {@link EnvVarResource}. */
public class EnvVarResourceTest {

  @Test
  public void parseResourceAttributes_null() {
    Map<String, AttributeValue> result = EnvVarResource.parseResourceAttributes(null);
    assertThat(result).isEmpty();
  }

  @Test
  public void parseResourceAttributes_empty() {
    Map<String, AttributeValue> result = EnvVarResource.parseResourceAttributes("");
    assertThat(result).isEmpty();
  }

  @Test
  public void parseResourceAttributes_malformed() {
    Map<String, AttributeValue> result = EnvVarResource.parseResourceAttributes("value/foo");
    assertThat(result).isEmpty();
  }

  @Test
  public void parseResourceAttributes_single() {
    Map<String, AttributeValue> result = EnvVarResource.parseResourceAttributes("value=foo");
    assertThat(result).isEqualTo(ImmutableMap.of("value", stringAttributeValue("foo")));
  }

  @Test
  public void parseResourceAttributes_multi() {
    Map<String, AttributeValue> result =
        EnvVarResource.parseResourceAttributes("value=foo, other=bar");
    assertThat(result)
        .isEqualTo(
            ImmutableMap.of(
                "value", stringAttributeValue("foo"),
                "other", stringAttributeValue("bar")));
  }

  @Test
  public void parseResourceAttributes_whitespace() {
    Map<String, AttributeValue> result = EnvVarResource.parseResourceAttributes(" value = foo ");
    assertThat(result).isEqualTo(ImmutableMap.of("value", stringAttributeValue("foo")));
  }

  @Test
  public void parseResourceAttributes_quotes() {
    Map<String, AttributeValue> result = EnvVarResource.parseResourceAttributes("value=\"foo\"");
    assertThat(result).isEqualTo(ImmutableMap.of("value", stringAttributeValue("foo")));
  }
}
