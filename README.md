JCSA-Tools
==========

Java Card Static Code Analysis Tools (JCSA-Tools) is a collection of Static
analysis tools developed for the Java Card platform. It includes the Extendable
SootUp Analysis Runner (ESAR), the ESAR Java Card plugin, and the Java Card
Stack Analyser.

- **ESAR** (`esar.jar`) runs SootUp analyses. Java Card detectors ship in `javacard.jar`.
- **jcsda** (`jcsda.jar`) stack depth analyser and debugging tool.

See [Getting Started](docs/getting-started.md).

Licensed under Apache License 2.0. See `LICENSE`. Third-party notices are under `third-party/`.

Requirements
------------

- Java 21

Building
--------

For building steps and documentation see: [BUILDING.md](BUILDING.md)

Contributing
------------

Please read the [CONTRIBUTING.md](CONTRIBUTING.md) before submitting issues or PRs.

Usage
-----

To get usage help page:

```sh
java -jar esar.jar --help
java -jar jcsda.jar stack_depth --help
java -jar jcsda.jar class --help
```

### ESAR

For running ESAR with Java Card plugin it is recommended to create a configuration file.
Examples for configuration files available in `examples/`.

```sh
java -jar esar.jar --config examples/isoapplet.yaml
```

To configure and disable individual rules, a rule configuration must be created.
Dump example configuration with:

```sh
java -jar esar.jar -p bin/javacard.jar \
  --dump-default-rule-configuration=rule-config.yaml
```

#### Findings Output

ESAR supports [SARIF](https://sarifweb.azurewebsites.net/) and console output formats, enabling seamless CI integration.
Jenkins [Warnings Plugin](https://plugins.jenkins.io/warnings-ng/) supports parsing SARIF-formatted results.

Example console output:

```
[warning] [JcImplicitIntegralWidening] xyz.wendland.javacard.pki.isoapplet.DedicatedFile:80-81
Integral widened to a wider type without masking the sign extension (implicit sign extension) (ACSAP 2)

[warning] [JcHotField] xyz.wendland.javacard.pki.isoapplet.DedicatedFile:80-81
Field accessed inside a loop; cache in a local variable before the loop (ACSAP 6)

[warning] [JcHotField] xyz.wendland.javacard.pki.isoapplet.DedicatedFile:81-82
Field accessed inside a loop; cache in a local variable before the loop (ACSAP 6)

[warning] [JcImplicitIntegralWidening] xyz.wendland.javacard.pki.isoapplet.DedicatedFile:169-170
Integral widened to a wider type without masking the sign extension (implicit sign extension) (ACSAP 2)
...
```

#### Suppressing Findings

Findings can be suppressed in source code.

Suppress every detector for this line:

```java
// NOESAR
```

Suppress detector for the whole file:

```java
// EsarSuppress: JcHotField
```

Suppress one rule on the following lines. `line` and `lines` are both accepted:

```java
// EsarSuppress: JcHotField +3 lines
```

### Stack Analyser

Get stack depth listing from applet (descending order, default limit 100 results):

```sh
java -jar jcsda.jar stack_depth \
  -c path/to/classes \
  -a com.example.Applet
```

Print stack usage in individual class:

```sh
java -jar jcsda.jar class \
  -c path/to/class
```

References
----------

This project is based on the master's thesis `A practical approach to static code analysis for the Java Card platform`, which has more details.

```bibtex
@mastersthesis{nissinen2026,
    author = {Nissinen, Noa-Emil},
    title = {{A practical approach to static code analysis for the Java Card platform}},
    school = {Jamk University of Applied Sciences},
    year = {2026},
    type = {Master's thesis},
    url = {TBA}
}
```
