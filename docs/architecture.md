# Architecture

The current phase is a foundation. `music` owns the musical domain. `interpretation` is a deterministic layer over that domain. `ai` is downstream and must never be a dependency of interpretation.

`SimulatedVocalEngine` and the current `ChoirEngine` are development implementations, not production singing synthesis.
