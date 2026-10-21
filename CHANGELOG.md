Changelog
=========

[1.2.0] - 2026-10-13
------------------

First public release of JCSA-Tools.

Features
--------

- The ESAR Java Card plugin (`javacard.jar`) includes these detectors:
  - `JcDeprecatedRandomDataAlg` reports deprecated `RandomData` algorithms.
  - `JcDeprecatedCipherAlg` reports deprecated `Cipher` algorithms.
  - `JcDeprecatedSignatureAlg` reports deprecated `Signature` algorithms.
  - `JcCompileTimeUncertainty` reports cryptographic factory arguments that are not known at compile time.
  - `JcOneShotUsage` requires `OneShot.open` in a `try` block with `close` in `finally`.
  - `JcDirectRecursion` reports a method that calls itself.
  - `JcCallGraphCycle` reports a cycle in the call graph.
  - `JcRegisterMissingLifeCycle` reports an applet that never calls `register`.
  - `JcSignedRightShift` reports a signed right shift.
  - `JcPackageBlackList` reports a reference to a forbidden API.
  - `JcShareableMultiSelectable` reports an applet that provides a shareable object without implementing `MultiSelectable`.
  - `JcMissingUninstall` reports an applet that does not implement `uninstall`.
  - `JcCustomException` reports an application-specific exception.
  - `JcCatchException` reports a catch of `java.lang.Exception`.
  - `JcRuntimeExceptionsNotAllowed` reports a thrown `java.lang.RuntimeException` or subtype. `CardRuntimeException` and its subtypes are allowed.
  - `JcProcessAllocation` reports object allocation reachable from `Applet.process`.
  - `JcRecordLikeClass` reports a class whose methods are only getters and setters.
  - `JcAtomicTransactionBalance` reports a transaction that is not committed or aborted on every path.
  - `JcApduBufferState` reports an outgoing APDU sequence that is out of order or unfinished.
  - `JcImplicitIntegralWidening` reports an integral value widened without masking the sign extension.
  - `JcUnknownArraySize` reports an array allocated with a size that is not a compile-time constant.
  - `JcIsoExceptionInvalidSw` reports an `ISOException.throwIt` status word outside ISO/IEC 7816-4.
  - `JcHotField` reports a field read repeatedly in one method or inside a loop.
  - `JcOwnerPinCheck` reports an ignored `OwnerPIN.check` result.
- `jcsa.jar` estimates the stack depth of an applet and prints the stack use of methods in a single class.
