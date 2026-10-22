# Verification tests

End-to-end checks of the crmlcv2 compiler. Each CRML model is translated to Modelica,
simulated with OpenModelica, and compared against a reference result.

From the repository root:

```
make test-verification              # every suite: crml.compiler.crmlcv2.verification.Verification*
make test-verification SUITE=ETL    # one suite: VerificationETL
```

A new suite named `Verification<Suite>` in that package is picked up without changing the
Makefile. The target calls `./gradlew :compiler:verificationTest` with a `--tests` filter.

You need `omc` on the `PATH` (or `OPENMODELICAHOME` set). Without it every case is skipped.
The normal `test` task excludes these tests, which are tagged `omc`.

## Where to look

- `build/verification_report.html` has one entry per case: the generated Modelica, the
  diagnostics, the per-variable table, the scripts and the `omc` output. Long blocks start
  collapsed.
- `build/testSuiteGenerated/verification/<suite>/<Name>/` is each case's working directory:
  - `summary.txt`: the outcome and the status of every variable;
  - `src/<Name>/`: the generated package and the harness, which you can load into OMEdit
    to debug;
  - `simulate.mos` / `simulate.log`, `compare.mos` / `compare.log`: what `omc` ran and
    printed;
  - `<Name>_res.mat` (the simulation) and `*_ref.mat` (the reference);
  - `diff.<var>.csv`: one file per variable that differs.

## What is simulated

| Mode | When | Simulated class | Reference |
|---|---|---|---|
| HARNESS | `verificationModels/<suite>/<Name>/` exists | `<Name>_verif`, which extends the model and binds its externals | `<Name>_verif_ref.mat` |
| STANDALONE | otherwise (the `*_no_ext` models) | `<Name>` itself | `<Name>_ref.mat` |

The references are Dymola simulations of hand-written Modelica models (in
`libraries/modelica/CRML_test`), not output of any compiler. A mismatch therefore means
either the compiler or the reference is wrong. Never regenerate a reference from compiler
output.

## Outcomes

Outcomes are listed in pipeline order. Each one means every earlier stage passed.

| Outcome | JUnit | Meaning | Usually |
|---|---|---|---|
| `NO_REFERENCE` | skipped | There is no reference result for the model. | A fixture is missing. |
| `TRANSLATION_FAILED` | failed | A syntax error, an exception, or an ERROR diagnostic. | A parser or compiler gap. The reason names which. |
| `LOAD_FAILED` | failed | `omc` could not load a library or the generated package. | Invalid Modelica syntax, or a library problem. |
| `CHECK_FAILED` | failed | `checkModel` rejected the model. | The compiler generated ill-typed Modelica. The reason lists `omc`'s errors. |
| `SIMULATION_FAILED` | failed | No result file, or `omc` timed out. | A build or runtime error. See `simulate.log`. |
| `NOTHING_TO_COMPARE` | failed | No variable could be compared. | Every variable has a non-scalar type, or the reference doesn't contain the model's variables. Check the fixture. |
| `MISSING_VARIABLES` | failed | The reference has variables the result lacks. | The compiler dropped or renamed a variable. |
| `MISMATCH` | failed | At least one compared variable differs. | A behavioural defect. Check the `DIFFERS` rows. |
| `PARTIAL_MATCH` | skipped | Everything compared matches, but some variables couldn't be compared. | The model's interesting outputs are Clocks or Periods (see below). |
| `MATCH` | passed | Every variable of the model matches the reference. | |

## Variable statuses (`summary.txt` and the Compared variables table)

Every case that translates lists its compared variables, including a case that stops before
the comparison.

The model's own CRML variables are compared by name, using the names the generated Modelica
gives them:

- a scalar variable is compared as `x`;
- a class instance is compared through each of its members, inherited ones included, as
  `p.member`, and a member that is itself a class instance is walked the same way
  (`p.v.open`);
- a set is compared through each element, `R[1]` to `R[n]`.

Whether a variable or a class member was dropped is read from the translation trace.
Internal names, such as block instances and hoisted helpers, are ignored.

| Status | Meaning |
|---|---|
| `MATCH` | Equal to the reference, within `diffSimulationResults`' default tolerances. |
| `DIFFERS` | Not equal. `diff.<var>.csv` shows where. |
| `MISSING` | The reference has the variable, but the simulation result doesn't. |
| `DROPPED` | The translation produced no component for this CRML variable or class member, or for the instance that contains it. |
| `NOT_IN_REFERENCE` | The reference doesn't record the variable. It isn't counted against the case. |
| `NOT_COMPARABLE` | The type isn't Boolean, Requirement, Real, Integer or String, and isn't a class to walk into. A class that contains itself also stops here. |
| `NOT_REACHED` | The variable is comparable, but the case stopped before the comparison. The outcome says where. |

Clock, Period, Periods and Event values are represented differently in the compiler's
library and in the reference models, so no comparison is defined for them yet.

Boolean values appear as `Boolean4` ordinals: 1 = undefined, 2 = undecided, 3 = false,
4 = true. To read a value at a given time, put this in a `.mos` file in the case
directory and run `omc probe.mos` there:

```
print(String(val(b1, 0.5, "<Name>_res.mat")) + " vs " + String(val(b1, 0.5, "<Name>_ref.mat")) + "\n");
```

## Adding a suite

Subclass `VerificationTest` and pass the suite's path to `VerificationCase.discover(...)`.
See `VerificationETL`.

## Code layout (`src/test/java/crml/compiler/crmlcv2/verification/`)

| Package | Contents |
|---|---|
| `verification` | The tests only: `VerificationTest`, the base class, and one subclass per suite. |
| `verification.engine` | What runs a case: `VerificationCase` (fixture discovery), `Verification` (the stages and outcomes), `ComparedVariables` (which names are compared), and `OmcRunner` (runs `omc` scripts). |
| `crml.compiler.util` | The report wrappers shared with the other compiler tests, including `VariablesWrapper`, and `CrmlFrontEnd`. |
