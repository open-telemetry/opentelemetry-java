window.BENCHMARK_DATA = {
  "lastUpdate": 1769641902089,
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
      }
    ]
  }
}