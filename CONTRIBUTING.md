Contributing
============

Bug report
----------

Include the following in your bug report:

- Operating System info and version
- JCSA-Tools version or commit id
- Bug type: (crash,false-positive,false-negative)
- Sample source code pattern (if applicable)
- Full output with verbose logging enabled (enabled with "--verbose")

Feature request
---------------

Describe your use-case thoroughly and include possible examples of this feature
existing in other static code analysis tools (if applicable).

Submitting PR
-------------

PR submission checklist:

- Run `ant verify` to validate the commit.
- Use Conventional Commits format in commit messages.
- If the commit closes or is related to an issue, reference it in the commit message.


Git Prehook
------------

The git commit message format can be validated with git-prehook:

```sh
pre-commit install --hook-type commit-msg
```

Conventional Commits
--------------------

For more info see [https://www.conventionalcommits.org/](https://www.conventionalcommits.org/).

| Type     | Description & Purpose                                        | Modifies "src/" | Default SemVer Impact |
|----------|--------------------------------------------------------------|-----------------|-----------------------|
| feat     | Introduces a new feature or functionality                    | Yes             | Minor (0.X.0)         |
| fix      | Patches a bug or unintended behavior                         | Yes             | Patch (0.0.X)         |
| docs     | Documentation-only updates (README, docs, docstrings)        | No              | None                  |
| style    | Code formatting, whitespace, semicolons (no logic change)    | Yes             | None                  |
| refactor | Restructuring code without fixing bugs or adding features    | Yes             | None                  |
| perf     | Code changes specifically aimed at improving performance     | Yes             | Patch (0.0.X)         |
| test     | Adding, fixing, or updating test suites (no app code change) | No              | None                  |
| build    | Build system configs, bundlers, external dependencies        | No              | None / Patch          |
| ci       | CI/CD pipelines, workflows, and test runner configurations   | No              | None                  |
| chore    | General repo maintenance, scripts, tooling, .gitignore       | No              | None                  |
| revert   | Reverts a previous commit                                    | Yes             | Varies                |

If your commit clearly falls into one of these scopes, please use it in your commit:

| Scope    | Covers                                             | Example                                            |
|----------|----------------------------------------------------|----------------------------------------------------|
| stack    | Stack analyser in `src/stack`                        | `fix(stack): count invokeinterface operand stack`    |
| esar     | ESAR engine, CLI, and Soot helpers in `src/esar`     | `feat(esar): add entry-class traversal`              |
| analysis | Shared analyses under `src/esar/.../analysis`        | `fix(analysis): propagate constants through casts`   |
| rules    | Java Card detectors in `src/rules`                   | `feat(rules): warn on missing uninstall`             |
| report   | Console output, SARIF, and suppressions            | `feat(report): write SARIF to the given path`        |
| config   | Rule YAML and CLI configuration                    | `fix(config): honor an empty traversal blacklist`    |
| build    | Ant build, bootstrap, and environment checks       | `fix(build): require JDK 21 before tests`            |
| test     | `test/esar` and `test/rules`                           | `test(rules): cover implicit int widening`           |
| docs     | `README.md`, `BUILDING.md`, `CONTRIBUTING.md`            | `docs: describe ant bootstrap`                       |
| deps     | Resolved jars and `third-party/`                     | `build(deps): refresh the ESAR classpath`            |
| scripts  | `scripts/`                                           | `chore(scripts): list third-party licenses`          |
