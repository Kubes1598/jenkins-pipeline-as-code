/**
 * Archive coverage and forward to Codecov.
 * Looks for ./coverage/lcov.info or ./coverage/coverage-final.json.
 */
def call(Map cfg = [:]) {
  def files = findFiles(glob: 'coverage/**/lcov.info').collect { it.path }
  if (!files) {
    echo 'publishCoverage: no coverage files found, skipping.'
    return
  }
  files.each { archiveArtifacts artifacts: it, allowEmptyArchive: true }

  withCredentials([string(credentialsId: 'codecov-token', variable: 'CODECOV_TOKEN')]) {
    sh 'curl -fsSL https://uploader.codecov.io/latest/linux/codecov -o codecov && chmod +x codecov && ./codecov'
  }
}
