window.BENCHMARK_DATA = {
  "lastUpdate": 1771471136961,
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
          "id": "0b0929e570d76c230f2f73a60d75c637ee3e2ca7",
          "message": "fix(deps): update dependency io.opentelemetry.contrib:opentelemetry-aws-xray-propagator to v1.54.0-alpha (#8103)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2026-02-18T18:29:15-08:00",
          "tree_id": "96c332efb49b761e7d02835b5b071a4e16f9a147",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/0b0929e570d76c230f2f73a60d75c637ee3e2ca7"
        },
        "date": 1771471136053,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 23924044.2192796,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 23378491.47495828,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 19452090.15401414,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 15032621.684120024,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 17815646.8955279,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 20222508.31370308,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 20982201.470637,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 17625736.895689063,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 13486406.744710336,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 13433655.003561541,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 47166983.47161724,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 47571217.39293308,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 38310882.79439125,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 19890517.897654563,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 20121585.957359746,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 38877538.027706444,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 39538542.00237883,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 31921808.403800227,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 17249379.712906174,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads1 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 15625431.545557633,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 4646597.337717434,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 5457701.759631791,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 2731660.336973126,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 3679301.7054706276,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 2869425.450402638,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 4914095.06269815,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 7700060.530124573,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 5726615.106015891,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 5959452.463276406,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"DELTA\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 6378730.978111797,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 23690562.737321056,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 24494720.477171827,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 2417009.4399523647,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 4158617.7860566364,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"1\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 1917819.6247686078,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"COUNTER_SUM\"} )",
            "value": 13551718.402856076,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"UP_DOWN_COUNTER_SUM\"} )",
            "value": 10773363.78936016,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"GAUGE_LAST_VALUE\"} )",
            "value": 16872804.157968882,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_EXPLICIT\"} )",
            "value": 9965218.386240548,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.MetricRecordBenchmark.threads4 ( {\"aggregationTemporality\":\"CUMULATIVE\",\"cardinality\":\"100\",\"instrumentTypeAndAggregation\":\"HISTOGRAM_BASE2_EXPONENTIAL\"} )",
            "value": 8324512.462481727,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.SpanRecordBenchmark.threads1 ( {\"spanSize\":\"SMALL\"} )",
            "value": 2957802.4494675132,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.SpanRecordBenchmark.threads1 ( {\"spanSize\":\"MEDIUM\"} )",
            "value": 353801.2470809164,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.SpanRecordBenchmark.threads1 ( {\"spanSize\":\"LARGE\"} )",
            "value": 29849.87851883834,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.SpanRecordBenchmark.threads4 ( {\"spanSize\":\"SMALL\"} )",
            "value": 1740072.488450382,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.SpanRecordBenchmark.threads4 ( {\"spanSize\":\"MEDIUM\"} )",
            "value": 866704.2457381056,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          },
          {
            "name": "io.opentelemetry.sdk.SpanRecordBenchmark.threads4 ( {\"spanSize\":\"LARGE\"} )",
            "value": 123690.403817092,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 1\nthreads: 4"
          }
        ]
      }
    ]
  }
}