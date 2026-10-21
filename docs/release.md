Release checklist
=================

Before release
--------------

- Update CONTRIBUTING.md
- Set versions correctly for jcsa-stack, ESAR, and Java Card plugin (use scripts/changes.py)

  - `src/stack/com/airbus/Main.java`
  - `src/esar/com/airbus/esar/frontend/Cli.java`
  - `src/rules/com/airbus/esar/JavacardDetectorRegistry.java`

- Update change log
- Make the release commit
- Add git tag for the release commit (the JCSA version)
- Generate EPL and LGPL source distributable `ant epl_lgpl_sources`
- If updates to dependencies:
  - Update FOSS list: `ant foss_list`

After release
-------------

- Bump JCSA-Tools version number in `build.xml`
