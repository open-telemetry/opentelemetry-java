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

package io.opentelemetry.sdk.distributedcontext;

import static com.google.common.truth.Truth.assertThat;

import io.grpc.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.distributedcontext.DistributedContext;
import io.opentelemetry.distributedcontext.EmptyDistributedContext;
import io.opentelemetry.distributedcontext.unsafe.ContextUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/** Unit tests for {@link DistributedContextManagerSdk}. */
@RunWith(JUnit4.class)
public class DistributedContextManagerSdkTest {
  @Mock private DistributedContext distContext;
  private final DistributedContextManagerSdk contextManager = new DistributedContextManagerSdk();

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testGetCurrentContext_DefaultContext() {
    assertThat(contextManager.getCurrentContext())
        .isSameInstanceAs(EmptyDistributedContext.getInstance());
  }

  @Test
  public void testGetCurrentContext_ContextSetToNull() {
    Context orig = ContextUtils.withValue(null).attach();
    try {
      DistributedContext distContext = contextManager.getCurrentContext();
      assertThat(distContext).isNotNull();
      assertThat(distContext.getIterator().hasNext()).isFalse();
    } finally {
      Context.current().detach(orig);
    }
  }

  @Test
  public void testWithDistributedContext() {
    assertThat(contextManager.getCurrentContext())
        .isSameInstanceAs(EmptyDistributedContext.getInstance());
    try (Scope wtm = contextManager.withContext(distContext)) {
      assertThat(contextManager.getCurrentContext()).isSameInstanceAs(distContext);
    }
    assertThat(contextManager.getCurrentContext())
        .isSameInstanceAs(EmptyDistributedContext.getInstance());
  }

  @Test
  public void testWithDistributedContextUsingWrap() {
    Runnable runnable;
    try (Scope wtm = contextManager.withContext(distContext)) {
      assertThat(contextManager.getCurrentContext()).isSameInstanceAs(distContext);
      runnable =
          Context.current()
              .wrap(
                  new Runnable() {
                    @Override
                    public void run() {
                      assertThat(contextManager.getCurrentContext()).isSameInstanceAs(distContext);
                    }
                  });
    }
    assertThat(contextManager.getCurrentContext())
        .isSameInstanceAs(EmptyDistributedContext.getInstance());
    // When we run the runnable we will have the DistributedContext in the current Context.
    runnable.run();
  }
}
