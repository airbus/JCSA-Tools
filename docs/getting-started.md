Getting started
===============

See [BUILDING.md](../BUILDING.md) for building.

Configuring ESAR
----------------

`examples/esar.yaml` lists every available option:

```yaml
classPath:
  - test1
  - test2
sourcePath:
  - src
entryMethods:
  - "com.airbus.Test1.process(javacard.framework.APDU)"
  - "com.airbus.Test1.install(byte[],short,byte)"
  - "com.airbus.Test1.uninstall()"
entryClasses:
  - "com.airbus.Test2"
plugins:
  - javacard.jar
traversalPackageBlacklist:
  - com.example.vendor.
ignoreDefaultTraversalPackageBlacklist: false
ruleConfiguration: examples/jc-detectors.yaml
sarifOutput: findings.sarif
failOnFinding: true
```

Add the path to the compiled applet classes, the Java Card API, and libraries to `classPath`.
`sourcePath` lists the applet source files and is needed for processing suppressions.
`plugins` lists the detector JARs and usually has a path to `javacard.jar`.
`entryMethods` lists the analysis entry points. It should include the Java Card runtime entry points.

```sh
java -jar bin/esar.jar --config applet.yaml
```

Findings are printed to the console. With `failOnFinding: true`,
the process exits with a non-zero exit code when any are reported.
`sarifOutput` writes a SARIF file in addition to the console output.

Rule configuration
------------------

`ruleConfiguration` defines the rule containing rule configuration.
`examples/jc-detectors.yaml` contains an example rule configuration.
Dump the default rule configuration with the following command:

```sh
java -jar bin/esar.jar -p bin/javacard.jar \
  --dump-default-rule-configuration=rule-config.yaml
```

Each entry in `rules` names one rule and can set `enabled` and `severity`.
If `enabled` is omitted, the rule stays on. A rule that is not listed keeps
its default severity and stays enabled. `severity` is `note`, `warning`,
`error`, or `none`. `none` only changes the label: the finding is still
printed, and it still fails the run when `failOnFinding` is true. Set
`enabled` to false to turn a rule off. Any other key is a setting for that
rule. `JcIsoExceptionInvalidSw` uses `allowedCustomSws` for status words that
should not be reported.

```yaml
rules:
- JcHotField:
    enabled: false
- JcSignedRightShift:
    severity: note
- JcIsoExceptionInvalidSw:
    allowedCustomSws:
      - 0x9000
```

Suppressing findings
--------------------

Comments are read from `.java` files under `sourcePath`.

Suppress every detector that covers this line:

```java
// NOESAR
```

Suppress one detector in the whole file:

```java
// EsarSuppress: JcHotField
```

Suppress one detector on the following lines. The count does not include the comment line. `line` and `lines` are both accepted:

```java
// EsarSuppress: JcHotField +1 line
```

```java
// EsarSuppress: JcHotField +3 lines
```

Stack use
---------

```sh
java -jar bin/jcsda.jar stack_depth \
  -c path/to/classes \
  -a com.example.Applet

java -jar bin/jcsda.jar class \
  -c path/to/Applet.class
```

`stack_depth` estimates how deep the applet stack gets.
`class` prints the stack use of methods in one class file.
Run either command with `--help` for the remaining options.
