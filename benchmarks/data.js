window.BENCHMARK_DATA = {
  "lastUpdate": 1769537498884,
  "repoUrl": "https://github.com/open-telemetry/opentelemetry-java",
  "entries": {
    "Benchmark": [
      {
        "commit": {
          "author": {
            "email": "34418638+jack-berg@users.noreply.github.com",
            "name": "Jack Berg",
            "username": "jack-berg"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "fd0ffde651dbdbb2ccf208dba76ccf9914316a71",
          "message": "Rework and publish metric benchmarks (#8000)",
          "timestamp": "2026-01-26T14:19:25-06:00",
          "tree_id": "70c87d29a0bdb6dce3a33d5437558a2bb7e21774",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/fd0ffde651dbdbb2ccf208dba76ccf9914316a71"
        },
        "date": 1769459299853,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2252.3764054955923,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2284.8029921282377,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 2943.788286737482,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1475.8467010692104,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1710.071460469224,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2048.875996423438,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2108.3972846969673,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 2450.735031109217,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1326.282780172016,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1353.341461015033,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2331.7876720621707,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2299.947180379145,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 2949.4910601735487,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1469.2816470239218,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1718.122636428685,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2022.465541001414,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2074.887107623059,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 2513.1848291631545,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1335.8710674893803,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1308.103988095989,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 396.255073377377,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 398.86573085251536,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 551.6092745195666,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 342.88791571641076,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 331.64936824279187,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 411.1303725550433,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 430.8867060628152,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 757.1321334033959,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 441.46494101304035,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 453.0731351639377,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 392.95248562268466,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 536.0588280343569,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 240.1357225146165,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 157.90317098804053,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 224.88819727073692,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 471.7005752735122,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 495.462927280428,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 722.8731639639984,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 586.6648184763383,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 421.6811101098462,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "34418638+jack-berg@users.noreply.github.com",
            "name": "Jack Berg",
            "username": "jack-berg"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "c8ba093c5f4a5bcca207a8416a3c375275d64a50",
          "message": "Sync instrument stress test (#7986)",
          "timestamp": "2026-01-26T15:09:00-06:00",
          "tree_id": "a467767a18dddd07dc549e2f5a08e339f185a025",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/c8ba093c5f4a5bcca207a8416a3c375275d64a50"
        },
        "date": 1769462273885,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2321.0657669075626,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2270.7483885725155,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 2878.38114894664,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1493.2992571759632,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1717.8034877939378,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2091.850730540682,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2039.575021240942,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 2466.3147668913966,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1334.7855793982646,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1340.6048763233453,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2339.8432858505926,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2293.3392996979555,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 2954.310532521385,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1464.0938557779627,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1548.8312801110449,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2037.0488310427586,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2042.4651990856432,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 2495.5788505125506,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1322.030129479613,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1360.9637260603145,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 392.50486223488576,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 367.9173294590157,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 439.46510574907387,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 177.1356677335139,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 132.11035409515338,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 653.3698238219471,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 667.2673012147154,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 735.9595186954468,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 379.87537496032877,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 519.0667995236942,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 431.7385928607475,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 364.3660218631989,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 314.574446099683,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 198.276863926908,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 268.82863260461613,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 626.9618200926782,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 635.6047198519668,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 534.4692762349571,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 502.5774026025739,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 565.2819446817327,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "34418638+jack-berg@users.noreply.github.com",
            "name": "Jack Berg",
            "username": "jack-berg"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "d6e7e6e4e930756606c650a6f1ca8c3e23effa71",
          "message": "Fix intermittent Windows build failure in GraalVM integration tests (#8013)",
          "timestamp": "2026-01-27T08:35:20-06:00",
          "tree_id": "d5a54b148dfe05ce6c9843048ee4fb51b3bbc1c6",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/d6e7e6e4e930756606c650a6f1ca8c3e23effa71"
        },
        "date": 1769525051583,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2300.4961606830475,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2272.581988312909,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 2827.7863543865124,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1468.8866463385834,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1728.403959960633,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2085.59774576696,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2057.792516687533,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 2355.854877196652,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1316.542439121944,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1328.313500350194,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2269.184816887232,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2287.400286034004,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 2938.656715856279,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1460.012069990455,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1710.070216636134,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2058.7148024714625,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2061.0027393000914,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 2457.2395782127114,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1326.3251240874606,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1310.1791393211374,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 438.69729374868575,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 467.7839110162664,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 195.39436129321376,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 172.12584025611423,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 234.53044278363558,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 500.48878058842945,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 463.72053333115616,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 750.4465238911786,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 378.5727038300487,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 625.3716481618527,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 421.3952761800916,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 383.98940431321654,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 200.2531684977575,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 325.52901890238377,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 206.48168600498192,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 449.21674522880903,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 286.00776277246234,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 526.830150397323,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 463.22337991515894,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 372.5719640788771,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "gregor.zeitlinger@grafana.com",
            "name": "Gregor Zeitlinger",
            "username": "zeitlinger"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "8dc1e038767b0b0343ebae742550f791f64dbb4c",
          "message": "feat: always return ExtendedOpenTelemetry when incubator is available (#7991)\n\nCo-authored-by: Jack Berg <johnmberg8@gmail.com>",
          "timestamp": "2026-01-27T08:36:30-06:00",
          "tree_id": "a6522e3427407a8d8cbf425edce75bba54afbf35",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/8dc1e038767b0b0343ebae742550f791f64dbb4c"
        },
        "date": 1769525633097,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2319.627882018833,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2278.638878607845,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 2920.215840928634,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1471.8121945769267,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1723.8551993004185,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2082.22262606349,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2043.5911836008684,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 2489.9470245894186,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1319.7139499377295,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1308.6868991163867,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2322.609107336856,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2286.432794910933,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 2950.623390309203,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1464.817691116061,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1650.8500944658379,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2039.0305658155085,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2084.173195233304,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 2532.965006662814,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1326.9070410384527,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1349.160981882369,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 485.6439111431335,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 353.03714986293573,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 208.42467854236287,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 261.1749870724358,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 175.80547488052213,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 481.55018032645864,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 495.6903463294064,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 503.8617467907828,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 617.6096919893977,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 550.5222606797961,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 407.83525579454965,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 388.0631895339628,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 258.04843042530126,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 244.82619542383327,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 388.29578771676063,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 423.6368572650893,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 430.1708831554847,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 535.9211126821757,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 549.2909159211582,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 434.85556246689447,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "29139614+renovate[bot]@users.noreply.github.com",
            "name": "renovate[bot]",
            "username": "renovate[bot]"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "f1d02b5f4442491443811f9f053c5fa422c253b4",
          "message": "fix(deps): update dependency com.uber.nullaway:nullaway to v0.13.1 (#8016)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2026-01-27T09:45:19-08:00",
          "tree_id": "1052343ea9af2b6a918215bd09ef7f5f85b14aa9",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/f1d02b5f4442491443811f9f053c5fa422c253b4"
        },
        "date": 1769537497869,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2254.449587433561,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2297.1977817859183,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 2837.5038262494118,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1491.1286170161834,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1724.6675188820961,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2055.7452648353615,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2027.223842127767,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 2478.6721078073087,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1311.528502682275,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1304.4694590595002,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2309.639643980893,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2282.1829610139475,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 2942.734239445576,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1404.1135701761566,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1705.223935164424,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2062.692351362007,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2122.583378586668,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 2533.476688804329,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1330.0167254138955,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1325.050424563672,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 320.2559088377373,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 375.6555453884233,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 259.0733428977859,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 248.16541691883884,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 165.46819922093462,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 645.5286858014991,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 533.1223670472094,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 590.2384592064054,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 413.09158991188343,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 415.1900755196909,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 514.6781371369937,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 403.6919534770996,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 261.5617187232042,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 162.65339418314187,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 237.69873014322812,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 485.5942277119424,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 667.7350603983012,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 512.6868147050847,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 603.8964456627052,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 429.38811623482826,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          }
        ]
      }
    ]
  }
}