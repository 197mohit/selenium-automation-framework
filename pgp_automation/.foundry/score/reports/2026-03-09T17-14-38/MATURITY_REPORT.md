# Repository Maturity Report

**Score**: 46/120 (38.3%, F)
**Date**: 2026-03-09
**Repository**: pgp_automation
**Maturity Level**: Critical

## Summary

The PGP automation test suite has an impressive test corpus (14,597 tests across 898 files) and strong AI agent configuration, but is critically undermined by pervasive hardcoded credentials committed across 36+ files, a CI pipeline that never executes tests, and zero static analysis enforcement. Security posture and code hygiene are the weakest areas, while AI agent readiness is the standout strength. Immediate remediation of credential exposure and CI gaps is essential before addressing other maturity dimensions.

## Score Breakdown

| Criterion | Score | Confidence | Key Finding |
|-----------|-------|------------|-------------|
| AI Agent Readiness | 7/10 | high | Exceptionally detailed agent instructions (CLAUDE.md, AGENTS.md, 14 cast files, .cursor rules) |
| Testing & Quality Assurance | 6/10 | high | Massive test suite but no coverage tooling, CI never runs tests, SonarQube disabled |
| CI/CD Pipeline | 4/10 | high | Jenkinsfile exists with quality gate, but tests skipped (-DskipTests=true), no lint stages |
| Infrastructure & Deployment | 4/10 | high | 14+ environment profiles, but 300+ hardcoded secrets across config files |
| Observability & Reliability | 4/10 | high | Three reporting frameworks (Allure, Extent, ReportPortal) but 1,593 System.out.println calls |
| Documentation & Knowledge | 4/10 | high | No README.md; CONTRIBUTING.md partially fills gap; minimal Javadoc |
| Developer & Agent Experience | 4/10 | high | No Maven wrapper, no local dev setup, no .editorconfig or formatting tools |
| Dependency Management | 3/10 | high | Outdated deps with known CVEs (jackson 2.9, jsoup 1.8), 5 SNAPSHOT deps, system-scoped JARs |
| Release & Change Management | 3/10 | high | No release automation, no git tags, skeleton CHANGELOG, VERSION stuck on SNAPSHOT |
| Git & Collaboration Practices | 3/10 | high | Credentials in git, binary JARs committed, no commit conventions, incomplete .gitignore |
| Code Hygiene & Maintainability | 2/10 | high | No linting, 200+ wildcard imports, 15K-line god class, 134 printStackTrace() calls |
| Security Posture | 2/10 | high | 87+ hardcoded secrets, no SAST, no dependency scanning, HTTP artifact repos |

**Penalties Applied**: -4 points (hardcoded secrets: -4)

## Top Issues

- **CRITICAL: Hardcoded credentials everywhere** -- 87+ plaintext passwords, JWT keys, DB credentials, SonarQube tokens, and SMTP passwords committed across 13+ localconfig.properties files, Jenkinsfile, pom.xml, settings.xml, and Java source code. Immediate rotation and remediation required.
- **HIGH: CI pipeline never runs tests** -- Jenkinsfile uses `-DskipTests=true` and has no test stage. For a QA automation repo, this defeats the core purpose.
- **HIGH: SonarQube disabled by default** -- `sonar.skip=true` in pom.xml negates the quality gate in the Jenkinsfile, leaving zero static analysis enforcement.
- **HIGH: No static analysis or linting** -- No checkstyle, SpotBugs, PMD, ErrorProne, or formatter configured anywhere in the build or CI.
- **HIGH: Severely outdated dependencies with known CVEs** -- jackson-core 2.9.1, jsoup 1.8.3 (CVE-2015-6748, CVE-2021-37714), jjwt 0.9.1, guice 4.1.0. No OWASP dependency-check or vulnerability scanning.
- **HIGH: No code coverage measurement** -- No JaCoCo or equivalent configured; no coverage thresholds enforced.
- **HIGH: No README.md** -- The single most important documentation file is missing; acknowledged as tech debt.
- **HIGH: SNAPSHOT dependencies** -- 5 internal dependencies use -SNAPSHOT versions, making builds non-reproducible.
- **HIGH: System-scoped JARs committed to git** -- checksum.jar and EncryptDecrypt.jar bypass Maven dependency resolution.
- **HIGH: No release automation or git tags** -- 2,790+ PRs merged but no version tags, no release workflow.

## Inconclusive Areas

Several aspects could not be fully evaluated because configuration likely lives outside this repository:

- **CI test execution**: Test execution may be handled by separate Jenkins jobs or external orchestration (the 100+ testsuite XML files and ReportPortal integration suggest regular execution from outside this repo).
- **Branch protection & PR rules**: Bitbucket server-side settings (required reviewers, merge checks, branch permissions) are not visible in repository files.
- **SonarQube enforcement**: May be enforced for specific branches or PR builds via Jenkins pipeline parameters not visible in the Jenkinsfile.
- **Dependency vulnerability scanning**: May be handled at an organization level (centralized Snyk or Nexus IQ) with no evidence in this repo.
- **Parent POM configuration**: The parent POM (`parent-automation:3.2.11-SNAPSHOT`) may include additional plugins, dependency management, or analysis configuration not visible here.
- **ReportPortal dashboards**: Disabled by default (`rp.enable=false`) but likely enabled in CI jobs externally.
- **Internal wiki/Confluence**: Developer documentation and architecture docs may exist outside this repo.
- **Docker agent image**: The Jenkins Docker agent (`build_env:v1`) is maintained in a separate infra repo.

## Recommendations

### High Priority

1. **Remediate all hardcoded credentials immediately.** Move secrets to HashiCorp Vault or environment variables. Replace plaintext values in localconfig.properties with `${env.VAR}` placeholders. Rotate all exposed credentials (DB passwords, JWT keys, SonarQube token, SMTP password). Add git-secrets or detect-secrets as a pre-commit hook to prevent future leaks. *Effort: HIGH, Impact: Fixes the single biggest security and compliance risk.*

2. **Add a test execution stage to the CI pipeline.** Add a dedicated Test stage to the Jenkinsfile that runs at least a smoke suite: `mvn test -s settings.xml -DsuiteXmlFile=testsuite/Regression.xml`. Archive test reports and add failure notifications. *Effort: MEDIUM, Impact: Validates the core purpose of this QA automation repo on every build.*

3. **Enable static analysis.** Remove `sonar.skip=true` from pom.xml. Add maven-checkstyle-plugin (with google_checks.xml) and SpotBugs with FindSecBugs plugin. Configure these as blocking CI steps. *Effort: MEDIUM, Impact: Catches code quality, security, and style issues automatically.*

### Medium Priority

4. **Upgrade outdated dependencies and add vulnerability scanning.** Update jackson-core, jsoup, jjwt, guice, and other outdated libraries. Add OWASP dependency-check-maven plugin to the CI pipeline. Switch all artifact repository URLs from HTTP to HTTPS. *Effort: MEDIUM.*

5. **Add code coverage with JaCoCo.** Configure JaCoCo Maven plugin with reporting and set minimum thresholds (e.g., 70% for helper/utility code in src/main/java). *Effort: LOW.*

6. **Create a README.md.** Consolidate content from CLAUDE.md and CONTRIBUTING.md into a proper README covering project purpose, prerequisites, setup, test execution, and repo layout. *Effort: LOW.*

7. **Add Maven wrapper and JDK version pinning.** Run `mvn wrapper:wrapper` and add a `.sdkmanrc` or `.java-version` file pinning JDK 11. Add `.editorconfig` for consistent formatting. *Effort: LOW.*

8. **Implement release workflow.** Adopt git tagging (e.g., v3.2.11), configure maven-release-plugin or foundry release, and maintain the CHANGELOG. Pin internal dependencies to release versions for tagged releases. *Effort: MEDIUM.*

### Low Priority

9. **Replace System.out.println with proper logging.** Adopt SLF4J + Logback with a logback.xml configuration. Replace 1,593 println calls and 134 printStackTrace() calls with structured logger calls. *Effort: HIGH (volume of changes).*

10. **Migrate system-scoped JARs.** Publish checksum.jar and EncryptDecrypt.jar to internal Artifactory/Nexus and declare them as normal Maven dependencies. Add `*.jar` to .gitignore. *Effort: LOW.*

11. **Adopt commit message conventions.** Implement Conventional Commits format and enforce via commitlint or Bitbucket server-side hook. Add a CODEOWNERS file for review routing. *Effort: LOW.*

---

## Scoring Cost

| Metric | Value |
|--------|-------|
| Total cost | $7.49 |
| Input tokens | 4,960,071 |
| Output tokens | 71,486 |
| Total tokens | 5,031,557 |
