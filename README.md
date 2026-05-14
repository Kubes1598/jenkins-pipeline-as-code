# jenkins-pipeline-as-code

A declarative Jenkinsfile, a scripted variant for the edge cases declarative can't model, and a shared library of common steps. Build time on a real project dropped from 18 minutes to 6 once eight stages started running in parallel.

This is what I switched to after inheriting a Jenkins instance full of freestyle jobs configured in the UI. Freestyle jobs are un-reviewable, un-versioned, and a nightmare to roll back. Pipeline-as-Code puts the build config in the repo where it belongs.

## What's in this repo

| Path                          | What it is                                              |
| ----------------------------- | ------------------------------------------------------- |
| `Jenkinsfile`                 | The main declarative pipeline.                          |
| `Jenkinsfile.scripted`        | Scripted variant for things declarative can't do.       |
| `vars/notifySlack.groovy`     | Shared lib: post status to Slack.                       |
| `vars/publishCoverage.groovy` | Shared lib: archive coverage and forward to Codecov.    |
| `vars/runTrivy.groovy`        | Shared lib: scan an image, fail on CRITICAL.            |
| `vars/uploadArtifact.groovy`  | Shared lib: archive + stash for downstream stages.      |
| `src/org/Helpers.groovy`      | Utility class used by the vars/ files.                  |

## The pipeline

Eight stages, parallelised where possible:

1. Checkout (sequential).
2. Parallel batch: Unit, Lint, Type-check, Trivy.
3. Build image (sequential — needs checkout output).
4. Parallel batch: Integration tests, Smoke E2E.
5. Publish (sequential — image tags must be unique).

Eighteen-minute serial pipeline became six-minute parallel pipeline. Same agent fleet, just used properly.

## Wiring the shared library

Jenkins → Manage Jenkins → Configure System → Global Pipeline Libraries:

| Field            | Value                                                       |
| ---------------- | ----------------------------------------------------------- |
| Name             | `shared`                                                    |
| Default version  | `v2`                                                        |
| Source           | Git: `https://github.com/Kubes1598/jenkins-pipeline-as-code`|
| Load implicitly  | unchecked                                                   |

Then in any consumer repo's `Jenkinsfile`:

```groovy
@Library('shared@v2') _
```

## Docker agents

Every stage runs in a known-clean container:

```groovy
agent { docker { image 'node:20-alpine' } }
```

This kills "works on the Jenkins box but not the agents" for good. If you can't reproduce a build inside the same image, the build wasn't reproducible to begin with.

## Companion

- Same patterns on GitHub Actions: [github-actions-cicd](https://github.com/Kubes1598/github-actions-cicd).

## License

MIT. See [LICENSE](./LICENSE).
