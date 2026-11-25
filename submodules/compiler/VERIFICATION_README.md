# Verification tests

End-to-end checks of the crmlcv2 compiler. Each CRML model is translated to Modelica, then
checked and simulated with OpenModelica. If it has a reference, its outputs are verified
against it. How the engine is built, with diagrams, is in
[VERIFICATION_DESIGN.md](VERIFICATION_DESIGN.md).

From the repository root:

```
make test-verification              # every suite: org.openmodelica.crml.compiler.crmlcv2.verification.Verification*
make test-verification SUITE=ETL    # one suite: VerificationETL
./gradlew :compiler:verificationTest --tests "*LiftCheckModelTest"
```

A new suite named `Verification<Suite>` in that package is picked up without changing the
Makefile. The target calls `./gradlew :compiler:verificationTest` with a `--tests` filter.

You need `omc` on the `PATH` (or `OPENMODELICAHOME` set). Without it, every case that goes
beyond translation is skipped. The normal `test` task excludes these tests, which are tagged
`omc`.

## Levels

A case is taken through four levels. Each level includes the ones before it.

| Level | Passes when | Needs |
|---|---|---|
| `translate` | CRML translates to Modelica, and nothing is dropped | nothing; no omc |
| `check` | omc loads the generated package (and the harness, if any), and `checkModel` passes | omc |
| `simulate` | the simulated class simulates | a harness, for a model with externals |
| `verify` | the outputs match the reference | a reference |

Each suite requires a level, set by `level()` in its test class. A case goes as far as
that level, or as far as its fixtures allow if they stop earlier:

| Suite | Required |
|---|---|
| `VerificationETL`, `VerificationFORML` | `verify` |
| `VerificationSpecDoc` | none: as far as each case's fixtures allow |
| `LiftCheckModelTest` | `simulate` |

To run a suite to another level, change its `level()`.

## Fixtures

| Model | Harness | Reference | Simulated class |
|---|---|---|---|
| `<Name>.crml` (has externals) | `verificationModels/<suite>/<Name>/` | `refResults/<suite>/<Name>_verif_ref.mat` | `<Name>_verif`, which extends the model and binds its externals |
| `<Name>_no_ext.crml` | none needed | `refResults/<suite>/<Name>_no_ext_ref.mat` | `<Name>` itself |

A model with externals cannot be simulated without its harness. Its generated model can
still be checked on its own. A case with a reference is simulated over the reference's
interval. Otherwise it uses its own `experiment` annotation, or omc's default of 0 to 1 s.

The references are Dymola simulations of hand-written Modelica models (in
`libraries/modelica/CRML_test`), not output of any compiler. A mismatch therefore means
either the compiler or the reference is wrong. Never regenerate a reference from compiler
output.

## When a case passes

The rules are in `MonitorTest`, the base class of the suites. They are ordinary JUnit
assertions in the callbacks the engine makes after each stage:

- An error at any stage fails the case, and ends it there.
- A translation that dropped elements fails the case. It fails at the end, so the later
  stages still run and show what they show.
- An output that differs from the reference, or that the result lacks, fails the case. So
  does a verification that compared nothing. Outputs the reference does not record are only
  listed.
- A case whose fixtures cannot reach the required level is skipped, unless it failed on the
  way.

## Where to look

- `build/verification_report.html` has one entry per case: the fixtures, the levels, the
  generated Modelica and its diagnostics, every omc script with what omc printed, and the
  Outputs table. Long blocks start collapsed.
- `build/testSuiteGenerated/verification/<suite>/<Name>/` is each case's working directory:
  - `src/<Name>/`: the generated package and the harness, which you can load into OMEdit
    to debug;
  - `check.mos`, `simulate.mos`, `reference.mos`, `*-names.mos`, `*-signals.mos`: what omc
    ran, each with its `.log`;
  - `<Name>_res.mat` (or `<Name>_verif_res.mat`) and `*_ref.mat`: the simulation and the
    reference;
  - `summary.txt`: every output with its status.

## Outputs

The outputs are the model's own CRML variables. They are found through the translation trace
and named as in the generated Modelica:

- a scalar variable is `x`;
- a class instance is each of its members, inherited ones included, as `p.member`, and a
  member that is itself a class instance is walked the same way (`p.v.open`);
- a set is each element, `R[1]` to `R[n]`.

Externals are inputs bound by the harness. They are listed, but not compared. Internal names,
such as block instances and hoisted helpers, are ignored.

A Clock, Period or Event is compared by what it means, not by the record that implements it.
The compiler's library (`CRMLtoModelica.mo`) and the reference models (the `CRML` library) store
these types differently:

| Type | Compiled model | Reference | Compared as |
|---|---|---|---|
| Real | `x` | `x` | trajectory |
| Boolean, Requirement, Integer, String | `x` | `x` | steps |
| Clock | `c.out` | `c.out` (a `CRMLClock` record), or else the `activationCount` of the synchronous clock that holds the harness's `show_c` or `showEvent_c` block | instants at which `out` becomes `true4` (the ticks), counted over time against the activation count |
| Period | `P.is_open`, `P.isLeft/RightBoundaryIncluded` | `P.timePeriod` (or `P.is_open`), and the same boundary flags | steps of the open flag, and of the boundary flags |
| Event | `e.t` | `e.t` | steps of the event time |

Both results are read into Java by omc (`readSimulationResult`), and compared there:

- **Steps** compare a discrete signal as the values it takes and the instants it takes
  them: the same values, changing at the same instants within 1e-4 × the stop time.
  Changes closer together than that count as one, so zero-width pulses and before/after
  samples at an event don't matter. A clock's `out` is such a pulse, so it is compared by
  its rises to `true4` instead.
- **Trajectory** compares a continuous signal at each of the reference's time points,
  interpolating the result. The values must agree within 1e-3 of the reference's range.
  Points at a discontinuity are skipped.

Everything else in those records is implementation and is not compared: a clock's counter
and tick buffer, a period's start and close events, and a time locator's slots and index.
Periods (plural) have no rule yet.

| Status | Meaning |
|---|---|
| `MATCH` | Equal to the reference. |
| `DIFFERS` | Not equal. The detail column shows the first difference. |
| `MISSING` | The reference records it, but the simulation result doesn't have the signal. |
| `DROPPED` | The translation produced no component for this variable or class member, or for the instance that contains it. |
| `NOT_IN_REFERENCE` | The reference doesn't record it, or (for a clock) has neither `c.out` nor a `show_c` or `showEvent_c` block. |
| `NO_RULE` | Its type has no comparison defined, such as Periods. |
| `INPUT` | An external, bound by the harness. |

Boolean values appear as `Boolean4` ordinals: 1 = undefined, 2 = undecided, 3 = false,
4 = true. To read a value at a given time, put this in a `.mos` file in the case
directory and run `omc probe.mos` there:

```
print(String(val(b1, 0.5, "<Name>_res.mat")) + " vs " + String(val(b1, 0.5, "<Name>_ref.mat")) + "\n");
```

## Adding a suite

Subclass `MonitorTest`, then:

- set its required level in `level()`, where null means as far as the fixtures allow;
- list its cases with a static `cases()` that returns `CaseDiscovery.discover(...)`.

See `VerificationETL`. To change when a case passes, override a callback.

## Code layout

| Package | Contents |
|---|---|
| `org.openmodelica.crml.compiler.crmlcv2.verification` | The tests: `MonitorTest`, and one subclass per suite. |
| `….verification.engine` | `Evaluator`, which runs the stages, and `Monitor`, the callbacks it makes. |
| `….verification.engine.discovery` | Stage I: `CaseDiscovery`, `TestCase`, `Level`. |
| `….verification.engine.evaluation` | Stage II: `Translator` and `Translation`, `Workspace`, `Omc` and `OmcRun`, `Simulation`. |
| `….verification.engine.verification` | Stage III: `Verifier`, `Outputs` (what is compared), `Observation` (which signals stand for it), `Signals` (reading them through omc), `Series` (comparing them), `OutputResult`, `VerificationResult`. |
| `org.openmodelica.crml.compiler.util` | The report wrappers shared with the other compiler tests, including `OutputsWrapper`, and `CrmlFrontEnd`. |
