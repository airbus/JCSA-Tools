Building
========

Requirements
------------

- JDK 21 in `PATH` or `JAVA_HOME` variable set.
- Apache Ant
- Java Card Development Kit tools with Classic API 3.1.0 [Download](https://www.oracle.com/java/technologies/javacard-downloads.html)
- Python 3, only for generating the FOSS list.

Setup
-----

`JAVA_HOME` and `JC_HOME_TOOLS` environment variables must be set.
`JC_HOME_TOOLS` can also be passed as `-Djc.home.tools=...`.

```sh
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
export JC_HOME_TOOLS=/path/to/java_card_devkit_tools
ant bootstrap
```

`ant bootstrap` copies the API jars into `lib/javacard` and writes host-JVM copies
(without `java.*` stubs) into `lib/javacard-host`.

Build
-----

```sh
ant all
```

`ant jcsa_dist` generates the ZIP distributable `output/jcsa-x.x.x.zip`.

`ant clean` removes build output. `ant clean_all` also removes resolved dependencies.

Test
----

```sh
ant test_esar            # Only ESAR core tests
ant test_javacard_rules  # Only Java Card plugin tests
ant checkstyle           # Only checkstyle
ant test_all             # Run all tests
```

`ant verify` cleans, builds, and runs the full test suite.
This must be ran before submitting a commit or PR.

Code layout
-----------

|Path         |Documentation               |
|-------------|----------------------------|
|`src/esar`     |ESAR core                   |
|`test/esar`    |ESAR core tests             |
|`src/rules`    |Java Card Rule plugin       |
|`test/rules`   |Java Card Rule plugin tests |
|`src/stack`    |Java Card Stack Analyser    |
