# Instructions

## Ant

- Compile the project "ant compile_javacard_rule_tests"
- Run tests with: "ant test_javacard_rules"
- Full-classpath traversal: "ant test_javacard_rules -Desar.test.traverseEntryOnly=false"
- Compile ESAR core tests: "ant compile_esar_tests"
- Run ESAR core tests: "ant test_esar"
- Print Jimple example: "ant print-jimple -Dclasses=com.airbus.esar.fixture.implicitwiden.IbwShortArrayInitializerFromByte"

## Style

- Keep comments/docstrings simple, no extraneous detail, no links, no code blocks, no html tags, only short comments
- Method parameters when no fit in 120 lines will be formatted one parameter per line aligned

    public void visitStmt(final JavaSootMethod method,
                          final Body body,
                          final Stmt stmt,
                          final AnalysisContextIf context,
                          final BugReporterIf bugs);

- Prefer "static final" over constructor methods
- Never use builder pattern in this code base
- Use static factory method rather than builder

## Behaviour

- Don't do anything else than what strictly is asked from you
- Don't do changes when explanation or proposal is asked
- Only do the thing what is asked from you
- Only implement code or do any changes when the request has keyword "implement"
- No filler, answers need to be short and coherent, more details will be asked if necessary
- No small-talk, no filler
- The most important goal is to FOLLOW the INSTRUCTIONS EXACTLY and NOT passing tests
