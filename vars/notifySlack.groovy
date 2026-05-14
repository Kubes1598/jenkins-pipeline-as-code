/**
 * Post a build status update to Slack.
 * Usage: notifySlack(channel: '#builds', status: 'PASSED', mention: '@oncall')
 */
def call(Map cfg = [:]) {
  def channel = cfg.channel ?: '#builds'
  def status  = cfg.status  ?: currentBuild.currentResult
  def mention = cfg.mention ? "${cfg.mention} " : ''

  def colors = [PASSED: 'good', FAILED: 'danger', UNSTABLE: 'warning']
  def color  = colors[status] ?: 'warning'

  def msg = """
    ${mention}*${env.JOB_NAME}* #${env.BUILD_NUMBER} — *${status}*
    Branch: `${env.BRANCH_NAME ?: 'unknown'}` · Commit: `${env.GIT_COMMIT?.take(7) ?: '-'}`
    <${env.BUILD_URL}|Open build>
  """.stripIndent().trim()

  slackSend(channel: channel, color: color, message: msg)
}
