window.BENCHMARK_DATA = {
  "lastUpdate": 1770750264664,
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
          "id": "ff8e171e7e09ca9925c5785aaf16b1f810c8fd89",
          "message": "fix(deps): update errorproneversion to v2.46.0 (#7963)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>\nCo-authored-by: Jack Berg <johnmberg8@gmail.com>",
          "timestamp": "2026-01-27T14:01:09-06:00",
          "tree_id": "c8b6d6332a6bb527d4f209087997bb6083e3221a",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/ff8e171e7e09ca9925c5785aaf16b1f810c8fd89"
        },
        "date": 1769544711550,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2255.91193496253,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2282.2506122252917,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 2915.3986798427986,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1464.1407464112472,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1731.6214907300687,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2080.0768765425273,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2054.2652897144,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 2494.4361951214078,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1330.0148937933177,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1354.6005432159577,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2285.171643899544,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2287.2904854907865,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 2950.9788566556645,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1484.0459212944393,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1722.8404621566162,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2059.7554117779423,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2078.5922428562585,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 2488.063456451292,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1314.5530977713784,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1315.3510158243485,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 613.990445441643,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 451.6628190459063,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 344.7972171636451,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 148.7141186392557,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 306.0412406989843,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 474.50426104262243,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 505.11348286760614,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 348.899833876541,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 468.28075299291686,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 518.6451851793189,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 557.9278622848836,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 386.1454912870512,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 440.11174893676963,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 151.13619024657157,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 227.8783579986303,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 416.29070275211154,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 441.26820964104365,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 373.8525985816523,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 402.39005474119546,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 366.73176058411354,
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
          "id": "8126573e4e08bff4903fc4099661e6b3685a27dd",
          "message": "Update LongLastValueAggregator algo to avoid allocations (#8017)",
          "timestamp": "2026-01-28T10:38:14-06:00",
          "tree_id": "d11ee67f85adb4995669d3c68283ae87af255b99",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/8126573e4e08bff4903fc4099661e6b3685a27dd"
        },
        "date": 1769619914570,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2287.7223261463423,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2284.886502945492,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1916.676993295369,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1479.1611643506287,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1702.7017768942528,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2072.2017837842786,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2063.665367252557,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1729.4990262383405,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1320.7163287456801,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1316.4071193349032,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2258.751687825376,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2287.284419997718,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1897.7676075980676,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1462.444111805921,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1695.5542431234928,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2035.1861332632238,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2139.0376010639693,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1747.0743064576284,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1337.4720761497915,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1340.8314132684059,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 371.305496337339,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 408.48636196222003,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 401.8479193499014,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 187.29880941992548,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 252.50594865805132,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 471.6926949600929,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 470.16253099881425,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 526.8786249192144,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 433.2350616760542,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 412.7605406599432,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 522.5143452172365,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 345.2715111388217,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 265.8709115800598,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 327.0533397830378,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 267.05449877282797,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 410.6105270758576,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 436.7531387068906,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 555.1540358843971,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 628.645026283689,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 372.3994279151655,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "trask.stalnaker@gmail.com",
            "name": "Trask Stalnaker",
            "username": "trask"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "87b0d9a5721a583e49e26b686e41044ff86a8d5a",
          "message": "Align generate release contributors (#8023)",
          "timestamp": "2026-01-28T12:46:03-06:00",
          "tree_id": "6efab84ded1611f6c18365d02a86b0fc44e2fd96",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/87b0d9a5721a583e49e26b686e41044ff86a8d5a"
        },
        "date": 1769626484678,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2264.9473423901863,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2292.0758950180534,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1955.0724083462628,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1478.5256007092335,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1690.0788448640665,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2073.559831251764,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2036.2032948181718,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1711.0029654998718,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1343.0725074811294,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1328.6268018777196,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2289.8152654889714,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2305.5559767522286,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1963.0721361349272,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1465.6597796099854,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1714.8493729525812,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2070.7513049753816,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2062.980915181881,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1776.4380096310283,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1331.7470372524604,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1346.3810591513497,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 540.788566954039,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 574.583832696527,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 205.202761213374,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 260.83977599689086,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 234.78363247531152,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 480.02748411895817,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 469.42366480539914,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 660.1723694908706,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 643.0190760260111,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 537.659837441464,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 403.55097594703227,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 386.06050217531583,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 208.83712986849213,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 226.07475891640348,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 170.71247643835233,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 754.4073589896622,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 485.1602324351158,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 553.7907242489821,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 583.8925054597927,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 399.1202233380058,
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
          "id": "98856e6c7740eeda462802efa43ed82d6ef79b76",
          "message": "fix(deps): update dependency com.gradle.develocity:com.gradle.develocity.gradle.plugin to v4.3.2 (#8027)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2026-01-28T16:37:54-06:00",
          "tree_id": "6f8f20f04e8572bbae6a51ea91ea7c48b69c8b5b",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/98856e6c7740eeda462802efa43ed82d6ef79b76"
        },
        "date": 1769640535258,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2311.0594850371767,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2288.8727287738934,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1945.6567013008091,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1457.6638075748053,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1726.4809308365652,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2086.924183244308,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2074.7472371377735,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1750.7587061241998,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1313.5692080060703,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1317.1477971356903,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2322.6286385235458,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2279.1735122964224,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1909.378008368465,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1457.799967154953,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1724.3836125876485,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2037.9213070142112,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2096.1087566077294,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1725.9316151955452,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1317.8758403106328,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1349.6681786780096,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 506.0800240292154,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 435.3524307448819,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 295.3427874355197,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 295.93887043560335,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 273.1579455501923,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 503.24741982838066,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 444.84582455869713,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 615.2672527155548,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 600.1044804409655,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 455.98609326081976,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 479.8758599185796,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 412.0471351455023,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 352.6419947402473,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 147.3971411310383,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 287.50467824850557,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 651.4838709218781,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 485.57744331293844,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 451.3717804185373,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 494.2332091208139,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 445.03664848481975,
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
          "id": "c17f6136d982ecdcc368e97f46d349d8cc6375ec",
          "message": "chore(deps): update plugin com.gradle.develocity to v4.3.2 (#8026)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2026-01-28T17:00:43-06:00",
          "tree_id": "1096d90eaf09ea060f87ce28641bd2678aa5d341",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/c17f6136d982ecdcc368e97f46d349d8cc6375ec"
        },
        "date": 1769641901205,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2262.6955612853467,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2315.2343848160067,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1925.662936576266,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1467.8190521441763,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1718.1924880494403,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2058.2380354819907,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2045.6463005273927,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1809.1978441218703,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1338.5804681294105,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1330.9752131988594,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2294.107842340035,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2281.783282406989,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1957.5643077895961,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1492.3383234762368,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1682.9818546083748,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2079.3978831739023,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2091.8276054002126,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1752.833186219466,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1321.9289656171754,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1251.1974161940163,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 550.0580022600088,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 510.8019594935543,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 276.5159350412076,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 236.67734715697193,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 197.31047150364088,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 403.9383303664603,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 446.8540653491001,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 534.4951968009556,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 416.54577906306656,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 357.5719786083576,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 413.84744898191013,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 384.18168384478156,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 291.3415550149344,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 153.56067585336558,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 222.1307704270684,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 576.815139423878,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 637.0473594505922,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 497.9083682568174,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 396.9326910214395,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 551.642015206226,
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
          "id": "83d139d15a10cd88a37c4b1587577d26d0857d4b",
          "message": "fix(deps): update dependency net.ltgt.gradle:gradle-nullaway-plugin to v2.4.1 (#8025)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2026-01-28T17:01:21-06:00",
          "tree_id": "6bae2bc91ae3b5ab9fc7942a2d451ae0c9b1b65e",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/83d139d15a10cd88a37c4b1587577d26d0857d4b"
        },
        "date": 1769642596904,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2334.578246590185,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2297.04674919734,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1931.597551152058,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1459.833904526126,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1693.077806991246,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2018.5896737693224,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2071.6241757207827,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1792.546411531018,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1328.9396297880896,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1180.5764029695433,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2291.000983207203,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2308.9894561559863,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1929.8121031835758,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1473.2091236546898,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1657.0881690851268,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2068.917128060178,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2052.044193283882,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1741.6665391372935,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1324.7563259997448,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1376.9587951898002,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 428.2888930281504,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 367.0096125079021,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 335.6661156320343,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 207.35019674504707,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 216.2163132458631,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 405.54901368187336,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 605.7047239042283,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 463.30162552576786,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 471.7765031570334,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 310.8886165919061,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 313.4229951131746,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 330.0147737946633,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 362.289709973796,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 263.8593569353755,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 253.36159590283245,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 533.8518846990315,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 560.6561038559337,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 503.5344669734528,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 613.5830833659236,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 502.01710851569896,
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
          "id": "775e5789ab127679a51ef523a4390240946cc4b0",
          "message": "fix(deps): update spotless packages to v8.2.1 (#8021)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2026-01-28T17:00:19-08:00",
          "tree_id": "cc717bdbd090792e2135a5052cea216d1d6262e0",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/775e5789ab127679a51ef523a4390240946cc4b0"
        },
        "date": 1769650647873,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2275.5349880186504,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2291.302191365816,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1901.5250422705012,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1461.8547012967967,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1713.3136979074982,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2013.0428110207656,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2076.7339541164474,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1745.7967092906988,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1330.7982741238566,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1313.5566058526126,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2312.5662096550523,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2302.9733047449145,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1948.8148809977083,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1472.4327892441738,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1724.0965196542643,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2084.4015042042693,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2114.5522448523056,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1735.3005633299392,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1307.3046482937066,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1350.9589856592374,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 513.7038155751837,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 409.9804008812781,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 337.1099557330137,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 157.4653941071857,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 344.5566046697119,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 477.10710586076067,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 463.05114092446075,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 539.8702490049369,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 444.24432933478437,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 550.7837034509963,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 294.51524122785213,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 415.8579078301142,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 258.39195805291286,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 190.98434547468668,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 203.05229047052836,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 468.5856633948223,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 464.39023705299235,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 675.563363770266,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 609.3513058408977,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 452.54429144320403,
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
          "id": "fd7d014d02a4638ad91788a1746f87f27aa402b9",
          "message": "fix(deps): update dependency io.opentelemetry.semconv:opentelemetry-semconv-incubating to v1.39.0-alpha (#8022)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>\nCo-authored-by: Jack Berg <johnmberg8@gmail.com>",
          "timestamp": "2026-01-28T19:08:08-08:00",
          "tree_id": "2eb1830c56fe90d63ad3079029b7a10533253751",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/fd7d014d02a4638ad91788a1746f87f27aa402b9"
        },
        "date": 1769656677803,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2302.1440616031564,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2285.9193846536423,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1942.9464996985564,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1492.6140313721198,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1698.54316547462,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2077.9674549765887,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2121.9505144946647,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1732.358795511785,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1323.750873583584,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1355.6735462856086,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2316.8881544026726,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2302.40983892968,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1961.5927533398979,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1483.9564505802223,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1721.1213702695834,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2051.385649341384,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2128.9170075206175,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1747.9079220488845,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1312.4882040638508,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1297.8367316155368,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 435.1335176484101,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 369.49905436789714,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 231.2801177764286,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 155.81200048947647,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 280.1817633583021,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 651.5066646573866,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 442.64677330228795,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 496.75818779899083,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 396.78047564869826,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 560.1949503870526,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 372.07677996568015,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 341.92348444922305,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 275.6687899832169,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 339.92598314398344,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 341.42387373049587,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 617.89264111506,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 396.77016364435303,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 467.72618318357,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 406.30918232484925,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 382.6234215401162,
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
          "id": "f79ade5f2b529440036bd5c02f18247d077aa19b",
          "message": "Delete obsolete exception serialization benchmarks (#8029)",
          "timestamp": "2026-01-29T09:27:41-06:00",
          "tree_id": "f3c4975ae40bef8354de9cdb82c76cf86697f65f",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/f79ade5f2b529440036bd5c02f18247d077aa19b"
        },
        "date": 1769700997033,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2271.4523675190726,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2296.0913310443852,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1899.132268286483,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1463.09996408662,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1726.7998341703678,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2029.4539942685951,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2107.357897878512,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1763.9412604780798,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1316.9328377341924,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1334.9197837499808,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2330.8714282585825,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2284.3985921414246,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1926.1962304504173,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1462.0619916089202,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1697.7484091526458,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2061.969498241374,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2066.524374373108,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1803.3610535472064,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1324.6858159686058,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1219.5198506011054,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 572.1301038599652,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 367.91520025177886,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 287.77657947534055,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 146.753130864578,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 274.8995166604433,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 639.0599061974675,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 423.98249797689243,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 537.9206973166282,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 433.31709053588037,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 446.75328715508374,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 422.51762484496294,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 419.56834240500973,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 293.98687356641403,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 151.3859316536353,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 165.03331354102542,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 455.46098794391327,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 444.4779564400516,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 623.2642528658153,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 607.8930080757591,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 570.5616921732719,
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
          "id": "298247d5c7953452f648c5e3cfd3e8c7e135eb11",
          "message": "fix(deps): update dependency net.ltgt.gradle:gradle-nullaway-plugin to v3 (#8034)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2026-01-29T07:39:46-08:00",
          "tree_id": "1b7fa142ce017f90bf0548a6baa03764ff1514d9",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/298247d5c7953452f648c5e3cfd3e8c7e135eb11"
        },
        "date": 1769701846572,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2313.398277093465,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2289.808318746989,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1924.4214458485214,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1471.5681340062417,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1709.4385611987516,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2015.6845738635998,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2094.097374572876,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1750.572549555396,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1331.020794420108,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1360.5315366649731,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2282.393798487029,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2299.116980241588,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1908.3852260592744,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1468.1814109255033,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1748.108595667918,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2074.0336300702934,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2104.0633127260953,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1807.0520443264352,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1318.433418045272,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1202.9829097149016,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 432.880798817497,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 444.56448317168645,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 294.60523204710927,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 361.54467516143984,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 192.6028835327055,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 478.1393688993106,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 444.79661811389406,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 522.3596815308445,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 401.1136083939973,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 432.6980769759351,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 464.67287006528267,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 359.94853430737925,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 268.0431454493554,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 209.86992536896872,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 193.65253242400652,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 667.1791110876205,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 617.8623680842487,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 695.5637603695006,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 549.9948275854521,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 513.3669468346291,
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
          "id": "619ccc5c71ee1818332eed10126903d97eb859ee",
          "message": "chore(deps): update gradle to v9.3.1 (#8035)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2026-01-29T08:00:47-08:00",
          "tree_id": "68910bf025ffd0137eaabf4bb0879e8c51cd1116",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/619ccc5c71ee1818332eed10126903d97eb859ee"
        },
        "date": 1769703203457,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2271.670168323532,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2293.572248685935,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1898.898522651818,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1574.7336405643466,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1724.5450782511639,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2013.7972217215774,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2129.145397940801,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1767.5155096094463,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1316.2598894895182,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1313.744295153167,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2294.731265934622,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2280.8763355120254,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1913.5193076111125,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1464.0685677524812,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1720.5779178962032,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2033.3016417391561,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2095.407163326015,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1743.5864841974303,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1321.178820881481,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1312.3847007027393,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 396.43223584958923,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 386.6005889666577,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 495.8467975194147,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 186.07112195188697,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 221.3470782499773,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 424.04334672680113,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 495.6215013074956,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 555.6431023092588,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 539.5485278973723,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 436.04275222480817,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 434.9741901428985,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 338.4685133840233,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 323.9973602164433,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 147.3413296315966,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 247.54781370350037,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 488.68084617800497,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 609.2574214688077,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 439.3155276483127,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 585.8743360643014,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 399.87425961692605,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "trask.stalnaker@gmail.com",
            "name": "Trask Stalnaker",
            "username": "trask"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "6054204df87d045f1a15849f014cc5964eaf3e41",
          "message": "Add assertion support for Span hasException(null) (#8033)",
          "timestamp": "2026-01-29T08:01:55-08:00",
          "tree_id": "c94ae90ee06f0b915be009642bbe98e77bdaa6d4",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/6054204df87d045f1a15849f014cc5964eaf3e41"
        },
        "date": 1769703811332,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2297.0795142918532,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2315.0827457699725,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1910.313173705387,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1488.805613799872,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1721.9589634383306,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2051.639966891051,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2073.863996060476,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1744.59892520471,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1344.1598266920168,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1354.7351564700016,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2282.0172824959873,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2307.20280964832,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1913.8784449813834,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1464.88124743059,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1741.047709424881,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2057.3713907958677,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2065.781559582219,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1746.6511232949244,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1322.59095228726,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1316.9893308701107,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 472.1466830297886,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 444.33248608779223,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 270.58387445084117,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 250.9993243860813,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 132.06721680902137,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 629.716764382373,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 657.8052158053843,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 470.09826140766444,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 354.44420176805454,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 394.2589585876948,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 528.2735701822791,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 331.49123292056333,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 396.57859094179696,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 275.751690073112,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 321.15447736960493,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 560.308656905602,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 454.1470291792235,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 516.6505883830598,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 570.2960825072357,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 444.2460656739571,
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
          "id": "3d4ed0835d2cfc7f6e95e861104f36fe3392b079",
          "message": "fix(deps): update dependency net.ltgt.gradle:gradle-errorprone-plugin to v5 (#8032)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>\nCo-authored-by: Jack Berg <johnmberg8@gmail.com>",
          "timestamp": "2026-01-29T08:48:52-08:00",
          "tree_id": "d4127ddd72092d42a9fec02ac547f2bb0517aa10",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/3d4ed0835d2cfc7f6e95e861104f36fe3392b079"
        },
        "date": 1769707398933,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2301.304484742511,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2113.3625968578244,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1934.2889700406358,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1463.1062200126466,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1712.9458700277078,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2039.938085947749,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2132.055533639355,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1720.3236137101835,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1316.0323734568997,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1371.9241452642618,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2245.1164486863604,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2301.8569597883543,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1898.6030794242565,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1491.272774969953,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1727.5400854511838,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2028.223398966291,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2104.0863122823293,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1755.5020009700959,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1330.9416786303705,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1141.3793359691745,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 393.4010752785559,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 433.8008422022316,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 205.30023337081852,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 280.02448388969543,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 144.99438165119432,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 478.7198481692566,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 505.8440517711264,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 391.7017551975755,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 376.8294690471215,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 484.8251384386531,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 534.1157314289273,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 445.3314123239769,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 283.7844634721055,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 172.3636378183689,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 225.18909061941426,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 487.80389404022407,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 383.50733739034905,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 696.2284495607391,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 446.5557652554883,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 467.01283354345935,
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
          "id": "0a1980b682388bfae1f8b73284857a8fe6e68b60",
          "message": "Deprecate OtTracePropagator (#8020)",
          "timestamp": "2026-01-29T13:12:28-06:00",
          "tree_id": "dce3139a97d896408868b7e5f5513e01a534edb9",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/0a1980b682388bfae1f8b73284857a8fe6e68b60"
        },
        "date": 1769714487364,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2324.8231822693733,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2296.7667119715397,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1924.6702046344926,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1465.274364736841,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1722.5827737362154,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2072.901883541036,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2068.515117641736,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1772.5691985214658,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1318.2354535402578,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1351.6995338676336,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2287.820544356538,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2299.144839921379,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1969.1235128512387,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1487.7116821860907,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1716.6754224031738,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2069.2135546198624,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2121.423099485647,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1790.612901619384,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1329.1453068154658,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1309.4784085147264,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 460.6982783079293,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 442.6885784007527,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 254.74253961668705,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 286.71968677436735,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 265.42122602570373,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 717.1294099790646,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 469.33854922794796,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 512.7784207233932,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 490.9986698532299,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 384.47362491228,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 511.8788909707358,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 401.6092298265243,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 325.5012456020927,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 296.6129749292046,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 257.120327560601,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 611.3647927793363,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 486.51871068845503,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 520.9053106485662,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 469.287980269666,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 542.2863079780827,
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
          "id": "b56bc5476b3cb68a6145457b0a92b38323ee56ab",
          "message": "Deprecate JaegerPropagator (#8019)",
          "timestamp": "2026-01-29T13:58:29-06:00",
          "tree_id": "5fb17ba6b18d4623c393fef09428c0f935fe647a",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/b56bc5476b3cb68a6145457b0a92b38323ee56ab"
        },
        "date": 1769717232922,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2269.488899101032,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2294.949910445109,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1904.5178077370365,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1449.8709692344148,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1730.07848759752,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2019.91218573352,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2090.5534049929793,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1814.3052254934682,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1309.7441289893386,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1386.00367781597,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2287.913446780017,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2304.389133795513,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1918.594494012444,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1480.7511821307885,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1729.1610126749533,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2067.616116248562,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2099.432169741954,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1743.8669788339707,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1339.5175520709984,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1326.6759384730472,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 274.8872592341582,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 358.3317954883275,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 285.3497251187292,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 217.29267213651923,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 240.2744046056378,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 499.35254465161535,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 465.53606693948075,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 474.5330357034307,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 570.8002381555158,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 353.5795263321629,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 467.03490274205103,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 348.633669126929,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 219.69775029410394,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 181.17592826713408,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 268.3064541931468,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 420.4996344210716,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 465.21011045481976,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 550.1847984543882,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 407.70572556204087,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 390.5778558865886,
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
          "id": "e9107da0b9997f5d78a5db192afbbd206025be1e",
          "message": "fix(deps): update dependency com.google.protobuf:protobuf-bom to v4.33.5 (#8036)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2026-01-30T08:58:16-08:00",
          "tree_id": "f4735576100465f10e85f35d68bb2fc3bca6f32e",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/e9107da0b9997f5d78a5db192afbbd206025be1e"
        },
        "date": 1769792882467,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2292.86408102562,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2103.077575748643,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1969.0308728041387,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1463.0373331134838,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1726.0488589657175,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2056.553770083204,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2091.5789018891373,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1820.0005794926778,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1331.5164583418602,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1305.3278343058023,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2344.472597426858,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2279.211032296484,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1921.0657884861425,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1466.4121630581699,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1720.4304763267305,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2009.487177074147,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2103.625443802889,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1721.1480420131707,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1318.9920516296847,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1251.1317645165514,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 454.3779983320327,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 454.2146639314818,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 253.00263748554917,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 179.2503142439581,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 289.2505576170232,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 666.70625699689,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 486.5434888207741,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 627.9186584385216,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 449.2083294857416,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 478.4824642726479,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 570.7679390376646,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 418.6333645722755,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 223.23639212697435,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 220.25865542800207,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 326.56156487405997,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 505.7949116715742,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 494.52172739108516,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 480.6457424564129,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 370.8422812089444,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 427.70187648881335,
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
          "id": "34cb2821ff62e03b4b5bf1a88a915194628e52e8",
          "message": "fix(deps): update dependency checkstyle to v13.1.0 (#8039)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2026-02-01T10:03:24-08:00",
          "tree_id": "7c4219b655039195ddc09b58b9103cf03111fb2d",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/34cb2821ff62e03b4b5bf1a88a915194628e52e8"
        },
        "date": 1769969634036,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2310.588556678943,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2285.0456354992066,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1918.120647388383,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1491.31327507734,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1711.6993313297921,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2049.363968226337,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2123.1727520811464,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1801.1441510852096,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1322.0021150801806,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1322.7291273704977,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2290.7038426544627,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2279.525693801194,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1910.2996093183924,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1493.0495696637986,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1723.153483908247,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2090.604172778713,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2058.400200649897,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1798.937321003218,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1318.8817323333649,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1352.831106513077,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 551.743882190068,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 335.66851421689665,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 201.94025246540977,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 194.79433031678332,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 228.56062005814238,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 406.8535601503189,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 574.3898257397288,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 523.8362761706346,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 475.1272836207971,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 482.84844328023854,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 459.58802811493734,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 432.3864826334167,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 280.99902804803105,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 180.4592415687318,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 222.44525326126683,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 492.17161719283575,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 411.7848447289598,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 538.9696907355841,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 550.1123674501403,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 498.9863932315825,
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
          "id": "e1940ab3f337904b8e3aed2ac364b634dd3de7ee",
          "message": "chore(deps): update plugin org.jsonschema2pojo to v1.3.1 (#8040)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2026-02-01T17:41:14-08:00",
          "tree_id": "5437ec572d20d3167f8a0bdfa80b220f26e1f8bf",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/e1940ab3f337904b8e3aed2ac364b634dd3de7ee"
        },
        "date": 1769997417441,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2283.0818507764657,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2288.050436772826,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1907.0461470825735,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1491.5176351163227,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1689.907013539637,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2042.1899671884198,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2042.5513558310042,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1730.9897054869675,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1341.710341175283,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1369.1380255886174,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2318.0363187057,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2280.9723486562348,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1950.2331090074726,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1474.1138512314842,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1713.9741343398223,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2054.6191780841928,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2089.677271233713,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1669.3089670703484,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1313.7384743324606,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1351.3924173124806,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 426.69526258285475,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 389.07517861610455,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 215.05266214720686,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 265.54804878655034,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 278.2086345950989,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 647.623655042103,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 504.29877631725384,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 426.7256026004913,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 403.78089430371347,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 362.54663860928423,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 383.0027399098026,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 534.3469102821294,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 294.7520080808546,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 172.31037636935667,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 310.54919272757513,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 498.7996912547528,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 602.5272649952019,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 693.5893365583536,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 474.73953025196204,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 544.988130596879,
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
          "id": "a6a074de9f0e3e5f5204632a20d852f4edb1950c",
          "message": "chore(deps): update weekly update (#8041)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2026-02-01T17:41:37-08:00",
          "tree_id": "b1b55de0e2128a27b025261a87ce10c359f3c8fc",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/a6a074de9f0e3e5f5204632a20d852f4edb1950c"
        },
        "date": 1769997976889,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2343.1481223936803,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2290.6371279757996,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1910.0633978282845,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1487.0854682087677,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1472.7518315046818,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2032.1922957374368,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 1780.6909097981193,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1812.9409625918229,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1322.4489067411898,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1338.1688529460457,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2330.3499436309535,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2293.9636090370013,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1932.4957932255863,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1460.0340770295468,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1721.5615873484653,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2040.2524083549742,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2048.368119807378,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1807.9978203939695,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1347.582859360814,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1366.105087793859,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 384.5697021579478,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 409.4784942098085,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 419.64256243720484,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 174.4172601560982,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 307.726240175096,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 601.0947849364823,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 465.4318279307986,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 698.4733502049805,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 512.1275402933222,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 562.8031599189608,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 530.6848277841302,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 382.4237345835713,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 447.1686902645982,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 151.8423358286777,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 203.41172066347045,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 476.1949943774824,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 260.1728577436851,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 404.34360617780874,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 449.0559312315921,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 459.27392630947617,
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
          "id": "1c4835c9a62f1d316fe1bf0b83368c517510097e",
          "message": "chore(deps): update plugin org.jsonschema2pojo to v1.3.2 (#8042)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2026-02-03T13:41:07-08:00",
          "tree_id": "b3b18f81316c9c877788b5225376f0607e648900",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/1c4835c9a62f1d316fe1bf0b83368c517510097e"
        },
        "date": 1770155409530,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2309.516170264376,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2291.3803335116363,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1903.7490145685674,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1463.062678993843,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1723.7334351444194,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2056.991291936833,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2082.677850561838,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1721.129769753404,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1318.0273512697888,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1345.6937161707017,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2278.977342083668,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2297.7447996820088,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1910.5478604768912,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1462.4461529934256,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1727.7723511593672,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2070.292350518718,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2056.3267002600237,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1711.522701934528,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1324.8327605440772,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1307.4053318286337,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 491.30584592201023,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 391.6684164562959,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 204.1395067334858,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 277.58111541086123,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 279.52274023496403,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 497.5754839555906,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 495.8410445373187,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 641.3426598189617,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 444.0823494605876,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 432.12599961806364,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 363.05375069473905,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 435.9388290329537,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 263.84146248073955,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 154.6720855180693,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 343.43690435774835,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 574.0221055764532,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 420.9293819466778,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 531.8416013039196,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 517.8812313731839,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 382.2162651551022,
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
          "id": "c68989a238fa70077bd89b789e28b582a4ebcc9f",
          "message": "fix(deps): update dependency io.grpc:grpc-bom to v1.79.0 (#8044)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2026-02-03T13:42:01-08:00",
          "tree_id": "26da860a18cc76316d67ae4c856794c03bc13855",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/c68989a238fa70077bd89b789e28b582a4ebcc9f"
        },
        "date": 1770160039950,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2307.47207481358,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2296.4655914932073,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1912.1036725409708,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1465.5496490101254,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1711.6764250504802,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2048.8459763839164,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2108.6642376627656,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1752.8628213207187,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1328.00827669379,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1341.696593030194,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2282.774369173665,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2298.2606544343953,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1931.9409524413397,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1482.5940087485744,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1723.667847453266,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2004.4452494493676,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2127.386994738961,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1740.8282017378199,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1343.873313151349,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1313.125120541617,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 605.6344281101632,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 372.7207313071961,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 268.46819794683745,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 212.3401268909739,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 250.3334541537657,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 622.2484847431938,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 578.5891271497118,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 502.25678174826163,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 579.3652535196187,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 535.7209705604198,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 377.512270117693,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 365.5651558242106,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 202.5403703085795,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 280.55084025437554,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 226.50786064653212,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 808.8107248811428,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 468.88620489250343,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 543.7109496204132,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 594.4184247660639,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 459.76509908803973,
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
          "id": "d31d468927c04b7f5018abeda9e3c1108bcbf406",
          "message": "chore(deps): update plugin de.undercouch.download to v5.7.0 (#8043)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2026-02-03T15:22:20-08:00",
          "tree_id": "de7083912d856ac114095d50ceca936313d02d4b",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/d31d468927c04b7f5018abeda9e3c1108bcbf406"
        },
        "date": 1770163467251,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2271.3337475085923,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2283.7598021029467,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1902.0932031693792,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1464.9451173730733,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1622.1884153391943,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2033.2097528766026,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2090.5777382969522,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1751.3959023021246,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1327.851944597171,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1357.5361639314917,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2304.230797403052,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2295.714816571649,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1911.0581647063693,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1479.793464508361,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1694.9148337132076,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2064.8922981509418,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2083.32029038112,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1769.5654210005935,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1326.379296558501,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1362.5563150635073,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 451.13281616824605,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 333.3383117546838,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 256.2364192577471,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 300.3146331189832,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 236.09151905919845,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 426.822996503594,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 442.94584639426967,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 423.83863206526064,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 453.47445588357004,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 410.49208542333673,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 399.9732378390818,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 357.5730755451013,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 217.57116890366714,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 238.95770772472991,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 179.27241878683452,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 435.931556322192,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 456.4646476283748,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 425.3262440762405,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 582.8658507083563,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 549.0374340069026,
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
          "id": "089600f77a411a410f7c6382c53387b8bb17267f",
          "message": "fix(deps): update dependency com.google.api.grpc:proto-google-common-protos to v2.65.1 (#8047)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2026-02-04T18:28:50-08:00",
          "tree_id": "73ee1a5d82dad6267dccb9786703a93acd053cec",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/089600f77a411a410f7c6382c53387b8bb17267f"
        },
        "date": 1770259241092,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2304.8410695572393,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2289.087098992449,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1909.8993571331216,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1464.928502412568,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1697.4262483314365,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2073.6130647351574,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2060.1477876720164,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1736.7507015569215,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1327.4815148064445,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1334.6421433916482,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 1814.3921390481196,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2300.4646258582543,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1919.2112231430267,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1438.7200582438525,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1692.706338234364,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2043.9747881644223,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2060.9561035552856,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1782.6777937196052,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1310.739312245784,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1340.242085333039,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 403.8969588970961,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 395.9494216166632,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 298.27561046775656,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 281.9980370808865,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 284.8084135939617,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 466.91970804059866,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 491.3288580340826,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 543.4122925718356,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 407.99505916029295,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 398.3471703249981,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 421.47615775883605,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 343.802416048983,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 262.51062176624123,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 201.4455411267312,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 357.70195638861185,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 405.0344197669613,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 393.2474031987176,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 512.2486938453796,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 467.04419113315043,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 518.9186871448579,
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
          "id": "a01befba2cb6165b639f626b340aa8dbbf9f6887",
          "message": "chore(deps): update otel/opentelemetry-collector-contrib docker tag to v0.145.0 (#8050)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2026-02-05T07:45:23-08:00",
          "tree_id": "9e8eda3d2ae16dd4d84cb66ad411dab49b5cb58e",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/a01befba2cb6165b639f626b340aa8dbbf9f6887"
        },
        "date": 1770307348137,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2320.128872946748,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2280.0148865122596,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1909.8850893921772,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1478.966988719732,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1716.315450572938,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2052.5444269873774,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2076.5805068986597,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1745.0314853087675,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1328.6192592152468,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1305.4410727590994,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2335.744200574593,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2301.26624650501,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1913.9181454106479,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1465.9980844220222,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1720.1362641685132,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2098.173124364539,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2056.847502791175,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1736.6139768567086,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1333.22639659912,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1329.2212946233778,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 394.93027133920924,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 413.5723738376784,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 428.4310819832328,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 176.39033227904082,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 235.3934846495478,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 651.6501802738727,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 525.530907782296,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 473.90449944135287,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 413.01668077358784,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 544.5496253948511,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 376.37505944722886,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 323.9773528011616,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 303.7367975361343,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 285.8100840208417,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 272.1726287272162,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 474.84169420420255,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 656.9675710574761,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 629.8517283817928,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 501.4310095304316,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 450.3246453271794,
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
          "id": "ad771ea65e54acaa81b357ead10c36c9a1b4906d",
          "message": "fix(deps): update armeriaversion to v1.36.0 (#8048)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2026-02-05T07:45:50-08:00",
          "tree_id": "4ce9f44db35abcbfb45eed0f71bf17cc0616d3a6",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/ad771ea65e54acaa81b357ead10c36c9a1b4906d"
        },
        "date": 1770307929058,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2283.065385395129,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2308.3184171778853,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1910.0418000775335,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1491.5983612575646,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1712.7065135096764,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2039.9242930235512,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2092.7216985554523,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1730.6576899249842,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1319.9675145046576,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1322.3691747573998,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2321.515818470993,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2290.9228865421646,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1908.756164671402,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1462.2320265796109,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1713.537658038687,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2079.001257966904,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2048.284340096891,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1715.4831236796158,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1315.1301557276709,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1343.3366527796954,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 443.26379207446826,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 363.4529425591455,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 218.33544247608614,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 331.2246957644817,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 304.23448110261904,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 516.7113975137627,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 490.9808073580296,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 670.7834071535892,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 506.643859823526,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 358.64429138208834,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 445.6426064804941,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 431.064233641457,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 291.33332234991656,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 259.8449701970462,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 215.32608919470468,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 574.0708199799194,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 512.676602650461,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 533.0081977927364,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 278.0602668678065,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 350.57606747499193,
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
          "id": "3f5411d6bdd3e5d7f59c70214d8de6cfbbadfe94",
          "message": "fix(deps): update dependency io.netty:netty-bom to v4.2.10.final (#8049)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2026-02-05T07:46:22-08:00",
          "tree_id": "26f8441d0003da23817828bc6ca2aa968c44df2f",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/3f5411d6bdd3e5d7f59c70214d8de6cfbbadfe94"
        },
        "date": 1770308501011,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2300.820141705188,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2303.5118626422036,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1904.1748418970315,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1475.9744324749677,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1730.2039758651995,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2042.4078242583132,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2119.340236285804,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1730.1049689814834,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1322.2373300474487,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1308.5605823449375,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2306.603199616511,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2289.127600078642,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1965.2298014774667,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1463.070494588017,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1696.1944304949743,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2051.5186143790497,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2036.0021906383904,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1789.341218818524,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1331.8714761005863,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1331.9530072040227,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 400.0695144940613,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 407.08248003600187,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 305.5098448259211,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 181.55878108988804,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 284.0391832844617,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 648.5749555759328,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 469.61114104428054,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 645.9976263036627,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 599.9761464828949,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 477.028759211615,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 417.29314420594983,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 396.9644267527856,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 266.630089356623,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 311.44430811726306,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 237.80274575487337,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 432.8865938125409,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 487.38878824799986,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 443.64992048244557,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 414.16522268818545,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 410.3503563775589,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "trask.stalnaker@gmail.com",
            "name": "Trask Stalnaker",
            "username": "trask"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "4335cf293dca8b9bbe91bd83bc8055c357ef205c",
          "message": "Stabilize complex attributes (#7973)",
          "timestamp": "2026-02-05T13:34:19-06:00",
          "tree_id": "2c14ec4d9f253c8dcf8b5998713fb7d1c212a90f",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/4335cf293dca8b9bbe91bd83bc8055c357ef205c"
        },
        "date": 1770326817868,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2340.3308491151583,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2297.0881428735784,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1971.0003108383346,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1474.4462190812003,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1731.1034866840782,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2064.3090629029157,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2071.1845380197888,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1754.2480374865074,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1317.85755642843,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1317.847746247245,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2320.103003473292,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2293.5357457769833,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1917.0403413638312,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1465.1634441502772,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1722.721294602055,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2011.9053028424964,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2089.7518155020675,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1765.1024793117892,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1338.1511211076966,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1317.4201882826287,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 373.03602505998373,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 616.0937339631075,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 312.01131546350643,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 325.8724221130738,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 241.46133403821892,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 394.03609223087915,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 479.6171588728822,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 357.9068630853396,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 430.54613307806574,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 445.6065858743458,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 485.07242712983,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 633.6613102840403,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 245.49099119749727,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 223.13331612985297,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 236.77389244104296,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 569.7431478408262,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 656.3746417088315,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 448.1753363055721,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 470.1593632532984,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 391.7346081633233,
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
          "id": "c2d6229676d95328bd4993e80c0d83b27d236e9a",
          "message": "Prepare for 1.59.0 release (#8052)",
          "timestamp": "2026-02-06T07:57:03-06:00",
          "tree_id": "c2286d215cb340f643e843f1898e5cc7ee61ceb2",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/c2d6229676d95328bd4993e80c0d83b27d236e9a"
        },
        "date": 1770389422581,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2292.0092356248074,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2282.450705615115,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1907.163162880584,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1463.5243223243522,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1727.0642263204984,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2098.335122175669,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2124.492274967593,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1760.0015595856125,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1339.541999793448,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1350.826941076389,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2253.607170720382,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2293.9400222295535,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1908.9896059562027,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1465.1931566555506,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1743.8319494648106,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2052.908544974402,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2056.7098277500327,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1735.1048046731544,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1336.696148408536,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1331.0325407951846,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 486.28598482690234,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 384.7694167279078,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 273.3619086326943,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 174.71990851817245,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 147.65592053666586,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 643.1592219202146,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 390.0464261108401,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 402.8022642821223,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 430.256396196856,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 363.3953072227658,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 555.2025416965373,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 535.0495169531159,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 243.34172335946118,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 197.14257795040876,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 213.0549124142136,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 480.5727245674063,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 619.3632599720879,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 640.5237973566503,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 497.89761107081733,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 397.84867891021605,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "197425009+otelbot[bot]@users.noreply.github.com",
            "name": "otelbot[bot]",
            "username": "otelbot[bot]"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "5e5a322391cafbaaa251b0057d1af8b724bc89ff",
          "message": "Update version to 1.60.0 (#8054)\n\nCo-authored-by: otelbot <197425009+otelbot@users.noreply.github.com>",
          "timestamp": "2026-02-06T08:21:40-06:00",
          "tree_id": "2deca0a61db987af9c0b2663f137546a821b2caf",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/5e5a322391cafbaaa251b0057d1af8b724bc89ff"
        },
        "date": 1770390789197,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2301.5687356424482,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2277.1993712884937,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1922.7461585072651,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1487.0010535089632,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1715.7435770696716,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2014.2832776855362,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2063.688323288689,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1822.0376999406951,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1330.2373086777432,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1389.369178037224,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2317.226939573143,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2272.945830808358,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1914.0730977331955,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1492.3526342005002,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1741.1744796992364,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2065.8065206914193,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2049.7979051276106,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1770.752109496884,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1326.7283696096245,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1334.216504583771,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 405.79599016946,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 418.23991515635146,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 186.66414351873476,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 324.252520651982,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 170.3616716510709,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 447.7045961713723,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 629.7285636471319,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 490.40242447446724,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 428.54264124776347,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 538.6524083584137,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 447.7323065422329,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 450.0164348299635,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 293.2140521298547,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 187.48632914299247,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 314.2202869613242,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 686.2034172613002,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 470.62486412729425,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 529.1122358404607,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 379.6727402281302,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 409.618066695641,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "197425009+otelbot[bot]@users.noreply.github.com",
            "name": "otelbot[bot]",
            "username": "otelbot[bot]"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "7149ffde2a82bc3ea2b5cf7e6d3129dbb3703c39",
          "message": "Post-release updates for 1.59.0 (#8056)\n\nCo-authored-by: otelbot <197425009+otelbot@users.noreply.github.com>",
          "timestamp": "2026-02-06T11:27:40-08:00",
          "tree_id": "37b290d3e0ccd9586de3b15e2dc0bbc0653db7ea",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/7149ffde2a82bc3ea2b5cf7e6d3129dbb3703c39"
        },
        "date": 1770409790667,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2284.418542650955,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2289.2458368765438,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1900.9516392859848,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1464.4321794710222,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1726.9905855147722,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2064.822599090871,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2057.748361319574,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1748.2668301155584,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1339.4102198030073,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1315.7114463048242,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2336.1145895480195,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2286.403175534079,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1903.5007618398947,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1469.7790396482103,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1713.5486092495162,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2005.538608617022,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2059.8962067921743,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1780.9224501041463,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1339.7682823764405,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1354.1180041007167,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 377.71122119510915,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 485.18389444372707,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 218.07967211210743,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 369.44104026853535,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 207.14741672568925,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 433.8858430354635,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 597.9235656267135,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 450.60359420896066,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 553.6450293475611,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 657.9765379009662,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 591.5297266962855,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 438.59414221626764,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 279.63020748323567,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 306.99506220950605,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 254.29160996546017,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 450.7853897885769,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 389.86982704239665,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 437.3844889957339,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 390.8737425854668,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 478.4139557031118,
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
          "id": "68933803da14a168ef9d039615282881c667f971",
          "message": "fix(deps): update dependency checkstyle to v13.2.0 (#8053)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2026-02-06T11:28:10-08:00",
          "tree_id": "29991684ff8f37260a5d0edf53e25df9933e641c",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/68933803da14a168ef9d039615282881c667f971"
        },
        "date": 1770410613628,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2254.176577078535,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2294.356766320191,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1935.2915668201633,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1462.711326318975,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1730.6351093713179,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2052.6598119448972,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2054.8564929686577,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1754.3802882687937,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1312.7827723116777,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1319.8562967626506,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2301.014662428622,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2282.0299347079717,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1921.2836598060399,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1483.7012403687982,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1722.8544756291024,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2025.0059792971545,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2031.4641956236496,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1809.7771029928335,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1319.341862810035,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1385.5473461008667,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 549.5735147905275,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 628.9604736461627,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 280.597721821352,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 168.5595406745187,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 247.13177721387638,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 430.689320724863,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 622.9246197679898,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 521.3651610821622,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 573.3946217822049,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 527.5194393633961,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 445.72478474759146,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 371.3360703501306,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 407.24399639624585,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 327.28908471810894,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 221.99510749732198,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 350.3066843958856,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 518.6677662123144,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 452.20112069907753,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 370.3797571970899,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 443.1198781658733,
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
          "id": "859af39321af0f88643c39b031192f12ff6de787",
          "message": "chore(deps): update plugin org.jsonschema2pojo to v1.3.3 (#8062)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2026-02-08T17:51:46-08:00",
          "tree_id": "6158730afbbd5d3e0f84063b512779a56aa2d3cb",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/859af39321af0f88643c39b031192f12ff6de787"
        },
        "date": 1770606378352,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2277.037920895044,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2289.205384103979,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1892.5257719396293,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1464.2707114894347,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1698.4890686745123,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2040.784242787721,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2101.0082328107915,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1702.2387000055573,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1321.4136860595615,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1315.4681061274991,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2307.037373829531,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2281.037594101561,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1954.6870384715949,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1466.8782055522804,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1706.4121869796331,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2020.428317125759,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2078.2060880521344,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1737.8172501401828,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1324.2784456625736,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1342.4156357807176,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 365.14790717069906,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 405.60359484194953,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 288.34792089572227,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 185.76337208843157,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 255.25342709045177,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 649.15299375222,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 451.9724370831441,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 498.38638727972193,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 438.1084955910137,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 453.00150898790815,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 338.1180164093495,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 460.36456221871106,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 309.11648851452844,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 264.2361831193461,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 332.428628335895,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 472.29265413693236,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 508.7510764907482,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 605.1564547077702,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 361.3487131880553,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 453.79578579171476,
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
          "id": "857cd7b80677b939c35d89e233580413f5ab1988",
          "message": "chore(deps): update weekly update (#8063)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2026-02-08T17:52:10-08:00",
          "tree_id": "29c257be82a24ee37598479db72bffe90cfdc085",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/857cd7b80677b939c35d89e233580413f5ab1988"
        },
        "date": 1770606946031,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2320.629418781381,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2292.561018682166,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1913.305243703347,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1463.717124645721,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1743.0812305539469,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2031.9108063951269,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2066.8510284301674,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1708.4042391610542,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1333.7665734860113,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1370.714270395023,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2280.170613494319,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2301.7808327882863,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1907.250646954081,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1466.5291545539853,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1706.1285069537832,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2033.692079306141,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2084.8305439906662,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1740.2626299052783,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1315.1046425495717,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1358.1557637677668,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 457.9191645332542,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 421.42179026899714,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 401.5522703114266,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 129.82297534864477,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 151.12708077453394,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 498.2074515416683,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 453.7408517898398,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 494.79872056966195,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 458.5267801224666,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 377.0652426704804,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 364.2146300305392,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 496.0639612294026,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 264.4797386893941,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 225.10323001588966,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 306.97380449837533,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 486.6309985767663,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 475.06524997150336,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 368.4140473796612,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 426.14543662329544,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 380.8520844968342,
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
          "id": "87345a1c8c8696d129c675f724809d5a0dc9f6a7",
          "message": "Split out cumulative vs. delta storage (#8015)",
          "timestamp": "2026-02-10T12:06:40-06:00",
          "tree_id": "ff2a6888d293459e8230ad5858ea4e6abe70844d",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/87345a1c8c8696d129c675f724809d5a0dc9f6a7"
        },
        "date": 1770749020557,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2269.4143167273346,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2283.0354893549456,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1934.6769464768881,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1458.1341354232914,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1744.1933349505048,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2088.5347894221577,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2055.7105738641812,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1741.6601001921113,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1277.962989351728,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1194.4127542991548,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 4608.67343163319,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 4607.671516903768,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 3817.4582970285146,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1930.1566806377893,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1967.0392157234394,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 3761.515164123055,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 3899.9462717651936,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 3221.673849546131,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1710.1700437145287,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1363.0561363567815,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 420.1693518117025,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 386.90029845100236,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 254.3623142518958,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 147.96241979179695,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 275.00255624942156,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 432.773204296405,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 361.0435457258701,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 481.0354676277463,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 578.0729469341603,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 452.34870475255764,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 1985.409021136459,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2688.0628765838546,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 389.8253130893214,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 171.67517762394814,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 259.47605015637794,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 1070.9950920165575,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 1539.242075464596,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1092.559556207097,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1111.112113253165,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 911.6288917519059,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "anuraaga@gmail.com",
            "name": "Anuraag (Rag) Agrawal",
            "username": "anuraaga"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "adecf606f9ecd7fd957486fd55fc6ae50e48dca1",
          "message": "Add metrics for PeriodicMetricReader (#8038)",
          "timestamp": "2026-02-10T12:09:09-06:00",
          "tree_id": "a0b7c039a6b19d017b4f955575eab37f1886db19",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/adecf606f9ecd7fd957486fd55fc6ae50e48dca1"
        },
        "date": 1770749696436,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2273.215203422382,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2287.38015041784,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1904.9323808011382,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1454.6263861907253,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1698.9988630703544,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2062.7952747193,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2048.4785525813522,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1744.4951219992756,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1329.9331538193626,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1324.6480474094221,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 4588.096600165517,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 4644.590535997011,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 3789.4721101876917,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1936.2733421607863,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1489.8579732741891,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 3812.611062817884,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 3810.775442407174,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 3211.1765479547794,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1687.8718614134136,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1550.8153124658122,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 457.6603073311402,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 412.80514954311303,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 166.73014821998748,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 361.81435446131076,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 185.95474601725135,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 435.37984673480105,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 423.60767493247783,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 597.225434171252,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 444.06174958908576,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 542.7490566236448,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2188.817463777556,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2530.778356620095,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 288.46172148340685,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 390.9699681106891,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 391.92325913481034,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 1221.1259448589785,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 1163.0978904828207,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1634.9352683969978,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 972.7431798813861,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 887.74162019382,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "anuraaga@gmail.com",
            "name": "Anuraag (Rag) Agrawal",
            "username": "anuraaga"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "9a089b61cd99fc804dc3a43ac648d9b091181bde",
          "message": "Add property to allow autoconfiguration of SDK telemetry version (#8037)",
          "timestamp": "2026-02-10T12:11:25-06:00",
          "tree_id": "4e4fdabd88ffff856362114534d05581891c49b0",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/9a089b61cd99fc804dc3a43ac648d9b091181bde"
        },
        "date": 1770750263685,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2320.3731953292126,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2293.1773738682086,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1902.5244403735146,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1485.0947104568836,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1740.916075321061,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2046.2939071973688,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2081.3601654153467,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1799.263078424889,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1329.0380636747761,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1320.3040543315922,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 4585.510824088298,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 4577.073328246329,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 3784.583286969694,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1868.9179686876978,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1963.6623436756333,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 3762.3913803263667,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 3859.9847191513413,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 3141.863020969974,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1686.5422547529295,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1465.4307168220134,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 431.8567036559868,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 462.60500511050975,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 226.50338384725313,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 164.6647633198691,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 134.07310115943233,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 685.4879574176654,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 422.03261338611253,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 486.7971403894032,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 472.62360143391834,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 437.67193202025976,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 2596.8257024997592,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 2439.821435144045,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 525.328371654872,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 233.25842855326954,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 272.25098991671337,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 1024.2284132313448,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 1260.1661186955423,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 1140.4043223302428,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1079.5900386964418,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 823.541076748946,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          }
        ]
      }
    ]
  }
}