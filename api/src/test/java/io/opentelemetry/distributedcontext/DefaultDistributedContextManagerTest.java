/*
 * Copyright 2019, OpenTelemetry Authors
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

package io.opentelemetry.distributedcontext;

import static com.google.common.truth.Truth.assertThat;

import io.grpc.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.distributedcontext.unsafe.ContextUtils;
import java.util.Arrays;
import java.util.Collection;
import javax.annotation.Nullable;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link DefaultCorrelationContextManager}. */
@RunWith(JUnit4.class)
public final class DefaultDistributedContextManagerTest {
  private static final CorrelationContextManager defaultDistributedContextManager =
      DefaultCorrelationContextManager.getInstance();
  private static final LabelKey KEY = LabelKey.create("key");
  private static final LabelValue VALUE = LabelValue.create("value");

  private static final CorrelationContext DIST_CONTEXT =
      new CorrelationContext() {

        @Override
        public Collection<Label> getEntries() {
          return Arrays.asList(Label.create(KEY, VALUE, Label.METADATA_UNLIMITED_PROPAGATION));
        }

        @Nullable
        @Override
        public LabelValue getEntryValue(LabelKey entryKey) {
          return VALUE;
        }
      };

  @Rule public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void builderMethod() {
    assertThat(defaultDistributedContextManager.contextBuilder().build().getEntries()).isEmpty();
  }

  @Test
  public void getCurrentContext_DefaultContext() {
    assertThat(defaultDistributedContextManager.getCurrentContext())
        .isSameInstanceAs(EmptyCorrelationContext.getInstance());
  }

  @Test
  public void getCurrentContext_ContextSetToNull() {
    Context orig = ContextUtils.withValue(null).attach();
    try {
      CorrelationContext distContext = defaultDistributedContextManager.getCurrentContext();
      assertThat(distContext).isNotNull();
      assertThat(distContext.getEntries()).isEmpty();
    } finally {
      Context.current().detach(orig);
    }
  }

  @Test
  public void withContext() {
    assertThat(defaultDistributedContextManager.getCurrentContext())
        .isSameInstanceAs(EmptyCorrelationContext.getInstance());
    Scope wtm = defaultDistributedContextManager.withContext(DIST_CONTEXT);
    try {
      assertThat(defaultDistributedContextManager.getCurrentContext())
          .isSameInstanceAs(DIST_CONTEXT);
    } finally {
      wtm.close();
    }
    assertThat(defaultDistributedContextManager.getCurrentContext())
        .isSameInstanceAs(EmptyCorrelationContext.getInstance());
  }

  @Test
  public void withContext_nullContext() {
    assertThat(defaultDistributedContextManager.getCurrentContext())
        .isSameInstanceAs(EmptyCorrelationContext.getInstance());
    Scope wtm = defaultDistributedContextManager.withContext(null);
    try {
      assertThat(defaultDistributedContextManager.getCurrentContext())
          .isSameInstanceAs(EmptyCorrelationContext.getInstance());
    } finally {
      wtm.close();
    }
    assertThat(defaultDistributedContextManager.getCurrentContext())
        .isSameInstanceAs(EmptyCorrelationContext.getInstance());
  }

  @Test
  public void withContextUsingWrap() {
    Runnable runnable;
    Scope wtm = defaultDistributedContextManager.withContext(DIST_CONTEXT);
    try {
      assertThat(defaultDistributedContextManager.getCurrentContext())
          .isSameInstanceAs(DIST_CONTEXT);
      runnable =
          Context.current()
              .wrap(
                  new Runnable() {
                    @Override
                    public void run() {
                      assertThat(defaultDistributedContextManager.getCurrentContext())
                          .isSameInstanceAs(DIST_CONTEXT);
                    }
                  });
    } finally {
      wtm.close();
    }
    assertThat(defaultDistributedContextManager.getCurrentContext())
        .isSameInstanceAs(EmptyCorrelationContext.getInstance());
    // When we run the runnable we will have the DistributedContext in the current Context.
    runnable.run();
  }

  @Test
  public void noopContextBuilder_SetParent_DisallowsNullParent() {
    CorrelationContext.Builder noopBuilder = defaultDistributedContextManager.contextBuilder();
    thrown.expect(NullPointerException.class);
    noopBuilder.setParent(null);
  }

  @Test
  public void noopContextBuilder_Put_DisallowsNullKey() {
    CorrelationContext.Builder noopBuilder = defaultDistributedContextManager.contextBuilder();
    thrown.expect(NullPointerException.class);
    noopBuilder.put(null, VALUE, Label.METADATA_UNLIMITED_PROPAGATION);
  }

  @Test
  public void noopContextBuilder_Put_DisallowsNullValue() {
    CorrelationContext.Builder noopBuilder = defaultDistributedContextManager.contextBuilder();
    thrown.expect(NullPointerException.class);
    noopBuilder.put(KEY, null, Label.METADATA_UNLIMITED_PROPAGATION);
  }

  @Test
  public void noopContextBuilder_Put_DisallowsNullEntryMetadata() {
    CorrelationContext.Builder noopBuilder = defaultDistributedContextManager.contextBuilder();
    thrown.expect(NullPointerException.class);
    noopBuilder.put(KEY, VALUE, null);
  }

  @Test
  public void noopContextBuilder_Remove_DisallowsNullKey() {
    CorrelationContext.Builder noopBuilder = defaultDistributedContextManager.contextBuilder();
    thrown.expect(NullPointerException.class);
    noopBuilder.remove(null);
  }
}
