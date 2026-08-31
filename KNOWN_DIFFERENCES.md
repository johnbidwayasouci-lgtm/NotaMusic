# KNOWN_DIFFERENCES

Date: 2026-08-31

This file contains only differences that cannot be responsibly resolved from the information available in the reference APK or cannot be validated with the current execution environment.

## A. Cosmetic

- Exact font metrics, icon geometry, anti-aliasing and pixel-level colors cannot be reconstructed reliably from APK string/class inspection alone. The independent implementation must therefore use its own assets while matching the observable visual hierarchy.

## B. Behavioral

- Some exact gesture thresholds, timing values and animation durations are not recoverable from the available archive/dex evidence without executing the reference application interactively.
- The exact semantics of every complex multi-voice editing gesture cannot be established from names and resources alone.

## C. Technical

- The reference application's complete internal persistence binary/schema is not safely reproducible from the observable evidence alone. NotaMusic therefore uses an independent versioned format.
- Exact proprietary MIDI synthesis/sample implementation cannot be established from the APK metadata alone.

## D. Android modernisation

- The reference uses legacy Android-era storage/sharing patterns. NotaMusic intentionally uses modern Android URI and sharing mechanisms rather than reproducing obsolete direct-storage behavior.
- The reference's original package/signing/advertising identifiers are intentionally not reproduced.

## E. Information impossible to deduce from the APK

- Exact source-level algorithms for engraving/layout where only compiled code and resources are observable.
- Exact commercial/pro-version backend behavior, purchase validation and entitlement service behavior.
- Exact remote services or accounts used by the original application when their complete endpoint/authentication behavior is not exposed in a safe, observable form.

## Not classified as known differences

Anything marked PARTIAL or UNVERIFIED in `QA_MATRIX.md` remains a work item, not an accepted difference. It must be implemented or tested before release whenever the required behavior can be established from the reference or from normal Android requirements.
