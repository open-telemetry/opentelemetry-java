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

import static org.junit.Assert.assertEquals;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import io.opentelemetry.common.AttributeValue;
import io.opentelemetry.sdk.resources.ResourceConstants;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Map;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(BeanstalkResource.class)
public class BeanstalkResourceTest {

  private BeanstalkResource populator;

  @Before
  public void setUp() throws Exception {
    File file = new File("tempFile");
    PrintStream ps = new PrintStream(new FileOutputStream(file));
    ps.println(
        "{\"deployment_id\":4,\"version_label\":\"2\",\""
            + "environment_name\":\"HttpSubscriber-env\"}");
    whenNew(File.class).withAnyArguments().thenReturn(file);

    populator = new BeanstalkResource();
  }

  @After
  public void release() {
    new File("tempFile").delete();
  }

  @Test
  public void testCreateAttributes() {

    Map<String, AttributeValue> metadata = populator.createAttributes();

    assertEquals(3, metadata.size());
    assertEquals("4", metadata.get(ResourceConstants.SERVICE_INSTANCE).getStringValue());
    assertEquals("2", metadata.get(ResourceConstants.SERVICE_VERSION).getStringValue());
    assertEquals(
        "HttpSubscriber-env", metadata.get(ResourceConstants.SERVICE_NAMESPACE).getStringValue());
  }
}
