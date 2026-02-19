window.BENCHMARK_DATA = {
  "lastUpdate": 1771468994153,
  "repoUrl": "https://github.com/open-telemetry/opentelemetry-java",
  "entries": {
    "Benchmark": [
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
          "id": "595d9a6fbf7ec57df2f3f5926a6b51708c0d6107",
          "message": "chore(deps): update otel/opentelemetry-collector-contrib docker tag to v0.146.1 (#8102)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2026-02-18T18:28:47-08:00",
          "tree_id": "79cc4e3174f5a19ce595310f4973d4e91ea06a5a",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/595d9a6fbf7ec57df2f3f5926a6b51708c0d6107"
        },
        "date": 1771468981844,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 23866729.59933152,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 23465818.37036454,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 19448699.055064566,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 15038200.343545556,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 16833512.42902273,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 21160019.40994454,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 20954773.48983615,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 17789837.616494525,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 13603360.554193694,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 13595225.431744402,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 46983350.20881768,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 47335223.71763645,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 39149342.35553315,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 18955987.11095916,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 20249378.66616287,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 39525967.14943823,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 38819761.903174534,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 32123321.3974246,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 17394410.429917417,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 14856716.856811047,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 5702033.330684963,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 5763256.314547474,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 2334157.487626077,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 1993238.5762279592,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 3193006.212332858,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 4945853.306142187,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 6273642.936435722,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 4925709.552833475,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 3572999.11921791,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 4268268.757804416,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 25603276.23131301,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 26122848.85434745,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 2435198.5447645322,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 2522253.9786969647,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 3115322.3138101185,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 11910946.305395031,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 14820231.857527256,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 14292168.375968266,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 11594756.973973159,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 8203393.233357795,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.SpanRecordBenchmark.threads1 ( {\"spanSize\":\"SMALL\"} )",
            "value": 2910890.528926955,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.SpanRecordBenchmark.threads1 ( {\"spanSize\":\"MEDIUM\"} )",
            "value": 352636.16728619474,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.SpanRecordBenchmark.threads1 ( {\"spanSize\":\"LARGE\"} )",
            "value": 29478.580785752485,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.SpanRecordBenchmark.threads4 ( {\"spanSize\":\"SMALL\"} )",
            "value": 2516619.0314432816,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.SpanRecordBenchmark.threads4 ( {\"spanSize\":\"MEDIUM\"} )",
            "value": 1027974.2104265876,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.SpanRecordBenchmark.threads4 ( {\"spanSize\":\"LARGE\"} )",
            "value": 118366.4595377577,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          }
        ]
      }
    ]
  }
}