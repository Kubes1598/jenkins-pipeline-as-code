package org

class Helpers implements Serializable {
  static String shortSha(String fullSha) {
    return fullSha?.take(7) ?: 'unknown'
  }

  static String safeBranchName(String raw) {
    return raw?.replaceAll('[^a-zA-Z0-9._-]', '-')?.take(63) ?: 'no-branch'
  }

  static Map parseEnvFile(String content) {
    def out = [:]
    content?.eachLine { line ->
      def trimmed = line.trim()
      if (!trimmed || trimmed.startsWith('#')) return
      def idx = trimmed.indexOf('=')
      if (idx == -1) return
      out[trimmed.substring(0, idx)] = trimmed.substring(idx + 1)
    }
    return out
  }
}
