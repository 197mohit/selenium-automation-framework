# dev environment

## commands

| purpose | command |
|---------|---------|
| test | `mvn test -s settings.xml -DsuiteXmlFile=<suite.xml>` |
| build | `mvn clean install -DskipTests=true -s settings.xml -Pautomation` |

## code style notes

- Java 11 source and target (maven-compiler-plugin)
- UTF-8 project encoding
- Groovy sources compiled alongside Java via gmavenplus-plugin
- TestNG test framework with Allure reporting and maven-surefire-plugin
- Multiple Maven profiles for environments (ite, automation, sandbox, staging, etc.); `ite` is active by default
- SonarQube used for static analysis in CI (not a local lint step)
