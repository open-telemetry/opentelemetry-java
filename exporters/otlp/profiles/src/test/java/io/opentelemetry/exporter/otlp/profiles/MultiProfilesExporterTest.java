/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.profiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.when;

import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import io.opentelemetry.sdk.common.CompletableResultCode;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit tests for {@link MultiProfilesExporter}. */
@ExtendWith(MockitoExtension.class)
public class MultiProfilesExporterTest {

  @Mock private ProfilesExporter profilesExporter1;
  @Mock private ProfilesExporter profilesExporter2;

  private static final List<ProfileData> PROFILE_LIST =
      Collections.singletonList(FakeTelemetryUtil.generateFakeProfileData());

  @Test
  void empty() {
    ProfilesExporter multiProfilesExporter = ProfilesExporter.composite(Collections.emptyList());
    multiProfilesExporter.export(PROFILE_LIST);
    multiProfilesExporter.shutdown();
  }

  @Test
  void oneProfilesExporter() {
    ProfilesExporter multiProfilesExporter =
        ProfilesExporter.composite(Collections.singletonList(profilesExporter1));

    when(profilesExporter1.export(same(PROFILE_LIST)))
        .thenReturn(CompletableResultCode.ofSuccess());
    assertThat(multiProfilesExporter.export(PROFILE_LIST).isSuccess()).isTrue();
    Mockito.verify(profilesExporter1).export(same(PROFILE_LIST));

    when(profilesExporter1.flush()).thenReturn(CompletableResultCode.ofSuccess());
    assertThat(multiProfilesExporter.flush().isSuccess()).isTrue();
    Mockito.verify(profilesExporter1).flush();

    when(profilesExporter1.shutdown()).thenReturn(CompletableResultCode.ofSuccess());
    multiProfilesExporter.shutdown();
    Mockito.verify(profilesExporter1).shutdown();
  }

  @Test
  void twoProfilesExporter() {
    ProfilesExporter multiProfilesExporter =
        ProfilesExporter.composite(Arrays.asList(profilesExporter1, profilesExporter2));

    when(profilesExporter1.export(same(PROFILE_LIST)))
        .thenReturn(CompletableResultCode.ofSuccess());
    when(profilesExporter2.export(same(PROFILE_LIST)))
        .thenReturn(CompletableResultCode.ofSuccess());
    assertThat(multiProfilesExporter.export(PROFILE_LIST).isSuccess()).isTrue();
    Mockito.verify(profilesExporter1).export(same(PROFILE_LIST));
    Mockito.verify(profilesExporter2).export(same(PROFILE_LIST));

    when(profilesExporter1.flush()).thenReturn(CompletableResultCode.ofSuccess());
    when(profilesExporter2.flush()).thenReturn(CompletableResultCode.ofSuccess());
    assertThat(multiProfilesExporter.flush().isSuccess()).isTrue();
    Mockito.verify(profilesExporter1).flush();
    Mockito.verify(profilesExporter2).flush();

    when(profilesExporter1.shutdown()).thenReturn(CompletableResultCode.ofSuccess());
    when(profilesExporter2.shutdown()).thenReturn(CompletableResultCode.ofSuccess());
    multiProfilesExporter.shutdown();
    Mockito.verify(profilesExporter1).shutdown();
    Mockito.verify(profilesExporter2).shutdown();
  }

  @Test
  void twoProfilesExporter_OneReturnFailure() {
    ProfilesExporter multiProfilesExporter =
        ProfilesExporter.composite(Arrays.asList(profilesExporter1, profilesExporter2));

    when(profilesExporter1.export(same(PROFILE_LIST)))
        .thenReturn(CompletableResultCode.ofSuccess());
    when(profilesExporter2.export(same(PROFILE_LIST)))
        .thenReturn(CompletableResultCode.ofFailure());
    assertThat(multiProfilesExporter.export(PROFILE_LIST).isSuccess()).isFalse();
    Mockito.verify(profilesExporter1).export(same(PROFILE_LIST));
    Mockito.verify(profilesExporter2).export(same(PROFILE_LIST));

    when(profilesExporter1.flush()).thenReturn(CompletableResultCode.ofSuccess());
    when(profilesExporter2.flush()).thenReturn(CompletableResultCode.ofFailure());
    assertThat(multiProfilesExporter.flush().isSuccess()).isFalse();
    Mockito.verify(profilesExporter1).flush();
    Mockito.verify(profilesExporter2).flush();

    when(profilesExporter1.shutdown()).thenReturn(CompletableResultCode.ofSuccess());
    when(profilesExporter2.shutdown()).thenReturn(CompletableResultCode.ofFailure());
    assertThat(multiProfilesExporter.shutdown().isSuccess()).isFalse();
    Mockito.verify(profilesExporter1).shutdown();
    Mockito.verify(profilesExporter2).shutdown();
  }

  @Test
  @SuppressLogger(MultiProfilesExporter.class)
  void twoProfilesExporter_FirstThrows() {
    ProfilesExporter multiProfilesExporter =
        ProfilesExporter.composite(Arrays.asList(profilesExporter1, profilesExporter2));

    Mockito.doThrow(new IllegalArgumentException("No export for you."))
        .when(profilesExporter1)
        .export(ArgumentMatchers.anyList());
    when(profilesExporter2.export(same(PROFILE_LIST)))
        .thenReturn(CompletableResultCode.ofSuccess());
    assertThat(multiProfilesExporter.export(PROFILE_LIST).isSuccess()).isFalse();
    Mockito.verify(profilesExporter1).export(same(PROFILE_LIST));
    Mockito.verify(profilesExporter2).export(same(PROFILE_LIST));

    Mockito.doThrow(new IllegalArgumentException("No flush for you."))
        .when(profilesExporter1)
        .flush();
    when(profilesExporter2.flush()).thenReturn(CompletableResultCode.ofSuccess());
    assertThat(multiProfilesExporter.flush().isSuccess()).isFalse();
    Mockito.verify(profilesExporter1).flush();
    Mockito.verify(profilesExporter2).flush();

    Mockito.doThrow(new IllegalArgumentException("No shutdown for you."))
        .when(profilesExporter1)
        .shutdown();
    when(profilesExporter2.shutdown()).thenReturn(CompletableResultCode.ofSuccess());
    assertThat(multiProfilesExporter.shutdown().isSuccess()).isFalse();
    Mockito.verify(profilesExporter1).shutdown();
    Mockito.verify(profilesExporter2).shutdown();
  }
}
