Security
========

Report a vulnerability in JCSA-Tools to [https://github.com/](https://github.com/airbus/JCSA-Tools/security/advisories/new) or
via email to the core maintainers listed in [AUTHORS.md](AUTHORS.md).
Do not open a public issue!

Include the JCSA-Tools version or commit id, a description of the problem, proof-of-concept, and steps to reproduce it.

Supported versions
------------------

Security fixes are provided for the latest release only.

Scope
-----

### In Scope

- A flaw in `esar.jar`, `javacard.jar`, or `jcsa.jar` that can be triggered by
  the inputs the tool reads, such as class files, source files, plugins, or
  configuration.
- A flaw in the build or the published distribution.

### Out of Scope

- A defect in an applet analysed by the tools.
- A false positive or false negative. Report those as bugs, as
  described in [CONTRIBUTING.md](CONTRIBUTING.md).
