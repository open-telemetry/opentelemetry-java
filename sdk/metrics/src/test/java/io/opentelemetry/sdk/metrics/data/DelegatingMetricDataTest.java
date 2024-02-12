package io.opentelemetry.sdk.metrics.data;

import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.metrics.TestMetricData;
import org.junit.jupiter.api.Test;

class DelegatingMetricDataTest {
  private static final class NoOpDelegatingMetricData extends DelegatingMetricData {
    private String description;
    private NoOpDelegatingMetricData(MetricData delegate) {
      super(delegate);
    }
  }

  private static final class MetricDataWithClientDescription extends DelegatingMetricData {
    private final String description;

    private MetricDataWithClientDescription(MetricData delegate) {
      super(delegate);
      this.description = "test";
    }

    @Override
    public String getDescription() {
      return description;
    }

    private static String parseUserAgent(String userAgent) {
      if (userAgent.startsWith("Mozilla/")) {
        return "browser";
      } else if (userAgent.startsWith("Phone/")) {
        return "phone";
      }
      return "unknown";
    }
  }

  @Test
  void equals() {
    MetricData metricData = createBasicMetricBuilder().build();
    MetricData noOpWrapper = new NoOpDelegatingMetricData(metricData);
  }

  private static TestMetricData.Builder createBasicMetricBuilder() {
    return TestMetricData.builder()
        .setResource(Resource.empty())
        .setInstrumentationScopeInfo(InstrumentationScopeInfo.empty())
        .setDescription("")
        .setUnit("1")
        .setData(null)  // TODO: Need to figure out how to set the Data value
        .setType(MetricDataType.SUMMARY); // Not sure what type should be here
  }
}
