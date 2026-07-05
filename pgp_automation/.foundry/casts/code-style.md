# Code Style

## Formatter

- code should be readable with comments at the top of the function for easy understanding and building knowledge base about the repo.

## Language

**Primary Language(s):** Java 11, Groovy 2.5

**Framework/Runtime:** TestNG (test execution), Autumn (internal Paytm test framework), REST Assured (API testing), Selenium WebDriver (UI testing), Allure + ExtentReports + ReportPortal (reporting)

**Language-Specific Guidelines:**

### Java
- Follow standard Java naming conventions: `PascalCase` for classes, `camelCase` for methods and variables, `UPPER_SNAKE_CASE` for constants.
- Test classes live under `src/test/java/scripts/` and extend `PGPBaseTest` (from the Autumn framework).
- API helper classes live under `src/main/java/com/paytm/api/` -- one class per API endpoint.
- Use TestNG `@Test` annotations for test methods. Group related tests using TestNG XML suite files in `testsuite/`.
- Use Lombok annotations (`@Data`, `@Builder`, etc.) where the project already uses them.
- Prefer `Duration.parse()` for time-based configuration values.
- Do not use wildcard imports.

### Groovy
- Groovy test scripts follow the same package structure as Java tests under `src/test/java/scripts/api/`.
- Groovy source files co-exist in the same directories as Java files -- the `gmavenplus-plugin` compiles them.
- Use Groovy idioms (closures, GStrings) where they improve readability, but keep test logic clear and debuggable.

## Separation of Concerns

Each class/module has one job. Do not mix concerns.
