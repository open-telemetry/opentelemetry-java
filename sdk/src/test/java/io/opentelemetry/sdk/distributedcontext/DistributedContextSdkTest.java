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
import static io.opentelemetry.sdk.distributedcontext.DistributedContextTestUtil.distContextToList;
import static io.opentelemetry.sdk.distributedcontext.DistributedContextTestUtil.listToDistributedContext;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.testing.EqualsTester;
import io.opentelemetry.distributedcontext.DistributedContext;
import io.opentelemetry.distributedcontext.DistributedContextManager;
import io.opentelemetry.distributedcontext.Entry;
import io.opentelemetry.distributedcontext.EntryKey;
import io.opentelemetry.distributedcontext.EntryMetadata;
import io.opentelemetry.distributedcontext.EntryValue;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for {@link DistributedContextSdk} and {@link DistributedContextSdk.Builder}.
 *
 * <p>Tests for {@link DistributedContextSdk.Builder#buildScoped()} are in {@link
 * ScopedDistributedContextTest}.
 */
@RunWith(JUnit4.class)
public class DistributedContextSdkTest {
  private final DistributedContextManager contextManager = new DistributedContextManagerSdk();

  private static final EntryMetadata TMD =
      EntryMetadata.create(EntryMetadata.EntryTtl.UNLIMITED_PROPAGATION);

  private static final EntryKey K1 = EntryKey.create("k1");
  private static final EntryKey K2 = EntryKey.create("k2");

  private static final EntryValue V1 = EntryValue.create("v1");
  private static final EntryValue V2 = EntryValue.create("v2");

  private static final Entry T1 = Entry.create(K1, V1, TMD);
  private static final Entry T2 = Entry.create(K2, V2, TMD);

  @Rule public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void getIterator_empty() {
    DistributedContextSdk distContext = new DistributedContextSdk.Builder().build();
    assertThat(distContextToList(distContext)).isEmpty();
  }

  @Test
  public void getIterator_nonEmpty() {
    DistributedContextSdk distContext = listToDistributedContext(T1, T2);
    assertThat(distContextToList(distContext)).containsExactly(T1, T2);
  }

  @Test
  public void getIterator_chain() {
    Entry t1alt = Entry.create(K1, V2, TMD);
    DistributedContextSdk parent = listToDistributedContext(T1, T2);
    DistributedContext distContext =
        contextManager
            .contextBuilder()
            .setParent(parent)
            .put(t1alt.getKey(), t1alt.getValue(), t1alt.getEntryMetadata())
            .build();
    assertThat(distContextToList(distContext)).containsExactly(t1alt, T2);
  }

  @Test
  public void put_newKey() {
    DistributedContextSdk distContext = listToDistributedContext(T1);
    assertThat(
            distContextToList(
                contextManager.contextBuilder().setParent(distContext).put(K2, V2, TMD).build()))
        .containsExactly(T1, T2);
  }

  @Test
  public void put_existingKey() {
    DistributedContextSdk distContext = listToDistributedContext(T1);
    assertThat(
            distContextToList(
                contextManager.contextBuilder().setParent(distContext).put(K1, V2, TMD).build()))
        .containsExactly(Entry.create(K1, V2, TMD));
  }

  @Test
  public void put_nullKey() {
    DistributedContextSdk distContext = listToDistributedContext(T1);
    DistributedContext.Builder builder = contextManager.contextBuilder().setParent(distContext);
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("key");
    builder.put(null, V2, TMD);
  }

  @Test
  public void put_nullValue() {
    DistributedContextSdk distContext = listToDistributedContext(T1);
    DistributedContext.Builder builder = contextManager.contextBuilder().setParent(distContext);
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("value");
    builder.put(K2, null, TMD);
  }

  @Test
  public void setParent_nullValue() {
    DistributedContextSdk parent = listToDistributedContext(T1);
    DistributedContext distContext =
        contextManager.contextBuilder().setParent(parent).setParent(null).build();
    assertThat(distContextToList(distContext)).isEmpty();
  }

  @Test
  public void setParent_setNoParent() {
    DistributedContextSdk parent = listToDistributedContext(T1);
    DistributedContext distContext =
        contextManager.contextBuilder().setParent(parent).setNoParent().build();
    assertThat(distContextToList(distContext)).isEmpty();
  }

  @Test
  public void remove_existingKey() {
    DistributedContextSdk.Builder builder = new DistributedContextSdk.Builder();
    builder.put(T1.getKey(), T1.getValue(), T1.getEntryMetadata());
    builder.put(T2.getKey(), T2.getValue(), T2.getEntryMetadata());

    assertThat(distContextToList(builder.remove(K1).build())).containsExactly(T2);
  }

  @Test
  public void remove_differentKey() {
    DistributedContextSdk.Builder builder = new DistributedContextSdk.Builder();
    builder.put(T1.getKey(), T1.getValue(), T1.getEntryMetadata());
    builder.put(T2.getKey(), T2.getValue(), T2.getEntryMetadata());

    assertThat(distContextToList(builder.remove(K2).build())).containsExactly(T1);
  }

  @Test
  public void remove_keyFromParent() {
    DistributedContextSdk distContext = listToDistributedContext(T1, T2);
    assertThat(
            distContextToList(
                contextManager.contextBuilder().setParent(distContext).remove(K1).build()))
        .containsExactly(T2);
  }

  @Test
  public void remove_nullKey() {
    DistributedContext.Builder builder = contextManager.contextBuilder();
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("key");
    builder.remove(null);
  }

  @Test
  public void testIterator() {
    DistributedContext distContext =
        contextManager
            .contextBuilder()
            .setParent(listToDistributedContext(T1))
            .put(T2.getKey(), T2.getValue(), T2.getEntryMetadata())
            .build();
    Iterator<Entry> i = distContext.getIterator();
    assertTrue(i.hasNext());
    Entry entry1 = i.next();
    assertTrue(i.hasNext());
    Entry entry2 = i.next();
    assertFalse(i.hasNext());
    assertThat(Arrays.asList(entry1, entry2))
        .containsExactly(Entry.create(K1, V1, TMD), Entry.create(K2, V2, TMD));
    thrown.expect(NoSuchElementException.class);
    i.next();
  }

  @Test
  public void disallowCallingRemoveOnIterator() {
    DistributedContextSdk distContext = listToDistributedContext(T1, T2);
    Iterator<Entry> i = distContext.getIterator();
    i.next();
    thrown.expect(UnsupportedOperationException.class);
    i.remove();
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            contextManager.contextBuilder().put(K1, V1, TMD).put(K2, V2, TMD).build(),
            contextManager.contextBuilder().put(K1, V1, TMD).put(K2, V2, TMD).build(),
            contextManager.contextBuilder().put(K2, V2, TMD).put(K1, V1, TMD).build())
        .addEqualityGroup(contextManager.contextBuilder().put(K1, V1, TMD).put(K2, V1, TMD).build())
        .addEqualityGroup(contextManager.contextBuilder().put(K1, V2, TMD).put(K2, V1, TMD).build())
        .testEquals();
  }
}
