val snapshot = false
val apidiffBaselineVersion = "1.65.0"

allprojects {
  var ver = "1.66.0"
  val release = findProperty("otel.release")
  if (release != null) {
    ver += "-" + release
  }
  if (snapshot) {
    ver += "-SNAPSHOT"
  }
  version = ver
  extra["apidiffBaselineVersion"] = apidiffBaselineVersion
}
