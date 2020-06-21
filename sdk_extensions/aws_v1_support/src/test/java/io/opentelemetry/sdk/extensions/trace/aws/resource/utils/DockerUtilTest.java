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

package io.opentelemetry.sdk.extensions.trace.aws.resource.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(DockerUtil.class)
public class DockerUtilTest {

  @Test
  public void testGetContainerId() throws Exception {
    assertNull(DockerUtil.getInstance().getContainerId());

    FileReader mockFileReader = mock(FileReader.class);
    whenNew(FileReader.class).withAnyArguments().thenReturn(mockFileReader);
    BufferedReader mockBufferReader = mock(BufferedReader.class);
    when(mockBufferReader.readLine())
        .thenReturn("dummy")
        .thenReturn(
            "11:devices:/ecs/bbc36dd0-5ee0-4007-ba96-c590e0b278d2/"
                + "386a1920640799b5bf5a39bd94e489e5159a88677d96ca822ce7c433ff350163");
    whenNew(BufferedReader.class).withAnyArguments().thenReturn(mockBufferReader);
    assertEquals(
        DockerUtil.getInstance().getContainerId(),
        "386a1920640799b5bf5a39bd94e489e5159a88677d96ca822ce7c433ff350163");

    when(mockBufferReader.readLine()).thenThrow(new IOException());
    assertNull(DockerUtil.getInstance().getContainerId());
  }
}
