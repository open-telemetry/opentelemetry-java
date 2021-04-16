package io.opentelemetry.sdk.extension.resources;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

class HostResourceTest {
    @Test
    void shouldCreateRuntimeAttributes() {
        // when
        Attributes attributes = HostResource.buildResource().getAttributes();

        // then
        assertThat(attributes.get(ResourceAttributes.HOST_NAME)).isNotBlank();
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @ExtendWith(SecurityManagerExtension.class)
    static class SecurityManagerEnabled {

        @Test
        void empty() {
            Attributes attributes = HostResource.buildResource().getAttributes();
            assertThat(attributes.asMap()).containsOnlyKeys(ResourceAttributes.HOST_NAME);
        }
    }
}
