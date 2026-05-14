/**
 * Archive a path as a Jenkins build artifact with explicit naming.
 * Usage: uploadArtifact(name: 'test-results', path: 'reports/')
 */
def call(Map cfg = [:]) {
  def name = cfg.name ?: 'artifact'
  def path = cfg.path ?: error('uploadArtifact: path required')

  if (!fileExists(path)) {
    echo "uploadArtifact: ${path} does not exist, skipping."
    return
  }

  archiveArtifacts artifacts: "${path}/**/*", fingerprint: true, allowEmptyArchive: true

  // Stash for downstream stages on the same build.
  stash includes: "${path}/**/*", name: name, allowEmpty: true
}
