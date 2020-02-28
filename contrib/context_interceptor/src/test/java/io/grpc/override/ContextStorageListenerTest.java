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

package io.grpc.override;

import static com.google.common.truth.Truth.assertThat;

import io.grpc.Context;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import javax.annotation.Nullable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link io.grpc.override.ContextStorageListener}. */
@RunWith(JUnit4.class)
public class ContextStorageListenerTest {
  private static final Context.Key<String> contextKey = Context.key("myKeyContext");

  @Test
  public void testDefault() throws IOException {
    File serviceFile = createService(ContextStorageListener.Provider.class, FirstProvider.class);
    try {
      assertThat(contextKey.get()).isNull();
      assertThat(FirstProvider.listener.currentValue).isNull();
    } finally {
      serviceFile.delete();
    }
  }

  @Test
  public void testAttachDetach() throws IOException {
    File serviceFile =
        createService(
            ContextStorageListener.Provider.class, FirstProvider.class, SecondProvider.class);
    try {
      Context context = Context.ROOT.withValue(contextKey, "myValue1");
      Context prev = context.attach();
      try {
        assertThat(FirstProvider.listener.currentValue).isEqualTo("myValue1");
        assertThat(SecondProvider.listener.currentValue).isEqualTo("myValue1");
        Context newContext = context.withValue(contextKey, "myValue2");
        Context newPrev = newContext.attach();
        try {
          assertThat(FirstProvider.listener.currentValue).isEqualTo("myValue2");
          assertThat(SecondProvider.listener.currentValue).isEqualTo("myValue2");
        } finally {
          newContext.detach(newPrev);
        }
        assertThat(FirstProvider.listener.currentValue).isEqualTo("myValue1");
        assertThat(SecondProvider.listener.currentValue).isEqualTo("myValue1");
      } finally {
        context.detach(prev);
      }
    } finally {
      serviceFile.delete();
    }
  }

  public static final class FirstProvider implements ContextStorageListener.Provider {

    @Override
    public ContextStorageListener create() {
      return listener;
    }

    private static final TestContextStorageListener listener = new TestContextStorageListener();
  }

  public static final class SecondProvider implements ContextStorageListener.Provider {

    @Override
    public ContextStorageListener create() {
      return listener;
    }

    private static final TestContextStorageListener listener = new TestContextStorageListener();
  }

  private static final class TestContextStorageListener implements ContextStorageListener {
    @Nullable private String currentValue = null;

    @Override
    public void contextUpdated(Context oldContext, Context newContext) {
      assertThat(contextKey.get(oldContext)).isEqualTo(currentValue);
      currentValue = contextKey.get(newContext);
    }
  }

  private static File createService(Class<?> service, Class<?>... impls) throws IOException {
    URL location = ContextStorageListener.class.getProtectionDomain().getCodeSource().getLocation();
    File file = new File(location.getPath() + "META-INF/services/" + service.getName());
    file.getParentFile().mkdirs();

    @SuppressWarnings("DefaultCharset")
    Writer output = new FileWriter(file);
    for (Class<?> impl : impls) {
      output.write(impl.getName());
      output.write(System.getProperty("line.separator"));
    }
    output.close();
    return file;
  }
}
