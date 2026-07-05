# vision

## purpose

This repo is the QA automation test suite for Paytm's Payment Gateway Plus (PGP) platform. It provides end-to-end API and UI tests covering the full payment lifecycle -- transactions, refunds, EMI, UPI, saved cards, subscriptions, merchant onboarding, link-based payments, QR payments, and merchant configuration. It exists to validate PGP backend services across multiple QA and staging environments before production releases.

## principles

- api-first testing -- the majority of tests validate backend APIs via Rest-Assured before exercising UI flows with Selenium
- multi-environment parity -- profile-based configuration (ite, qa5, qa7, qa8, qa11, qa12, staging, sandbox, hotfix) ensures tests run identically across environments
- comprehensive payment coverage -- every payment mode (cards, UPI, wallets, net banking, EMI, COD, postpaid) has dedicated test scripts
- data-driven execution -- merchant and user test data is externalized in CSV/JSON/YAML files, decoupled from test logic
- observable results -- Allure, Extent Reports, and ReportPortal integrations provide rich execution reporting and traceability back to JIRA tickets

## milestones

Milestones could not be confidently determined from the codebase. The project tracks work via JIRA tickets (PGP-xxxxx). Update this section based on the current team roadmap.

| # | milestone | status | description |
|---|-----------|--------|-------------|
| 1 | credential externalization & security hardening | tech-debt | Move hardcoded DB passwords, JWT keys, SMTP credentials, and SonarQube tokens out of localconfig.properties and pom.xml into a vault or environment variables. Enable static analysis (SonarQube, checkstyle, or SpotBugs) by removing `sonar.skip=true` and adding quality gate configuration. |
| 2 | dependency modernization | tech-debt | Upgrade outdated dependencies (jackson-core 2.9.1, Guice 4.1.0, jsoup 1.8.3, lombok 1.18.2, maven-surefire-plugin 2.20, groovy-all 2.5.7, AspectJ 1.9.4) to current stable versions. Migrate system-scoped checksum.jar and EncryptDecrypt.jar to a proper Maven repository or install-file approach. |
| 3 | developer experience & code quality | tech-debt | Add a README.md with onboarding guide, setup instructions, and test execution documentation. Add unit tests for helper classes (apphelpers, listeners, DTOs). Standardize naming conventions across classes and packages to consistent camelCase. |

### tech debt

identified during `foundry init`. these are structural improvements that don't add features but improve the codebase health. the setup agent populates this from repo analysis.

| # | item | severity | description |
|---|------|----------|-------------|
| 1 | hardcoded credentials in config files | critical | DB passwords, JWT keys, SMTP credentials, and SonarQube tokens are hardcoded in localconfig.properties and pom.xml (e.g. `password=pgal1p@y`, `SMTP_PASSWORD=ptmautomation`). Move to vault or environment variables. |
| 2 | outdated dependencies | high | Multiple dependencies are years out of date: jackson-core 2.9.1 (2017), Guice 4.1.0 (2016), jsoup 1.8.3 (2015), lombok 1.18.2 (2018), maven-surefire-plugin 2.20 (2017), groovy-all 2.5.7 (2019), AspectJ 1.9.4 (2019). |
| 3 | system-scoped dependencies | high | checksum.jar and EncryptDecrypt.jar use deprecated Maven `system` scope with `systemPath`. Migrate to a proper repository or install-file approach only. |
| 4 | no README documentation | medium | No README.md exists. New contributors have no onboarding guide, setup instructions, or test execution documentation. |
| 5 | static analysis disabled | medium | `sonar.skip=true` in pom.xml. No checkstyle, SpotBugs, or PMD configuration. Code quality checks are effectively skipped. |
| 6 | no unit tests for helpers | low | All tests are integration/E2E tests. Helper classes (apphelpers, listeners, DTOs) have no unit test coverage. |
| 7 | inconsistent naming conventions | low | Mix of camelCase and snake_case in class names (e.g. `FetchUserPaymentModeStatus_withtxnAmount`), packages with uppercase names (`AOA`, `MDRPCF`), and lowercase class names (`generateEsnHelper`, `withdrawRequest`). |

severity: `critical`, `high`, `medium`, `low`

## non-goals

- this repo does not implement payment processing logic -- it only tests the PGP platform's APIs and UI
- this repo does not run in production -- it targets QA and staging environments exclusively
- this repo does not provide a user-facing application or dashboard -- reporting is handled by Allure/Extent/ReportPortal integrations
- this repo does not manage test infrastructure (Selenium Grid, Jenkins agents, Docker) -- it consumes infrastructure provisioned externally

## tech stack

| layer | technology |
|-------|-----------|
| language | Java 11, Groovy 2.5 |
| framework | Selenium WebDriver (UI), Rest-Assured (API), Allure + Extent Reports (reporting) |
| build tool | Maven 3.x (with parent-automation 3.2.11-SNAPSHOT) |
| test framework | TestNG |
| ci/cd | Jenkins (Declarative Pipeline, Docker agent, SonarQube integration) |
