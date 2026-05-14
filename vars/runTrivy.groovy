/**
 * Scan a container image with Trivy.
 * Usage: runTrivy(image: 'api:123', severity: 'CRITICAL,HIGH', failOn: 'CRITICAL')
 *
 * failOn: 'CRITICAL' — exits non-zero only on critical CVEs.
 *         'HIGH'     — also fails on high.
 *         'NONE'     — reports only, never fails the build.
 */
def call(Map cfg = [:]) {
  def image    = cfg.image    ?: error('runTrivy: image required')
  def severity = cfg.severity ?: 'CRITICAL,HIGH'
  def failOn   = cfg.failOn   ?: 'CRITICAL'

  def exitCode = (failOn == 'NONE') ? '0' : '1'

  sh """
    docker run --rm \\
      -v /var/run/docker.sock:/var/run/docker.sock \\
      -v "\$(pwd):/work" \\
      aquasec/trivy:0.55.0 image \\
        --severity ${severity} \\
        --exit-code ${exitCode} \\
        --ignore-unfixed \\
        --format json -o /work/trivy-report.json \\
        ${image}
  """
  archiveArtifacts artifacts: 'trivy-report.json', allowEmptyArchive: true
}
