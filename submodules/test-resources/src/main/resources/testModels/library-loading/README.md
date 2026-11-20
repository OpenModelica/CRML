# Library loading test cases

Small test cases for loading CRML libraries (and models) that a model depends
on through `L union` / `flatten { A, B } union`. Each case tests one thing.

## Conventions

- **One folder per case.** All `.crml` files of a folder are loaded into one
  `ResourceSet`, one EMF `Resource` per file. The folder is the library path
  of the case: a dependency is found by its **declared** name (`library Logic`),
  not by its file name (see `file_name_differs`).
- **Expected outcome is in the file name**, following `ModelExpectation`
  (`:util-test`):
  - no marker or `.correct`: parses, loads, links, resolves all operator calls
    and passes validation;
  - `.syntax-error`: rejected by the parser. Loading goes on with the other
    files ("load as much as possible");
  - `.semantic-error`: parses, but validation must report an error. Problems
    with dependencies (missing, cycle, library on a model, broken dependency)
    are validation errors, not exceptions.
- Each error file starts with an `// Expected:` comment that says which error
  is meant.
- **`expected.constraints`** in each case folder states what the linked DOM must
  look like: which library each call and type resolves to, what is loaded and
  how often, and what must stay unresolved. The language is described in
  `CONSTRAINT_LANGUAGE.md` in `:util-test`. `LibraryLoadingTest` evaluates the
  file and shows the results as a table in the report; any line that is not
  `pass` fails the case. For error cases, the constraints only describe the DOM;
  which validation error is reported cannot be stated yet.
- **`translation.constraints`**, in every case with a valid model, states what
  the generated Modelica must look like. `LibraryLoading` in `:compiler`
  translates all valid models of the case in one run, adds each result as
  `<Model>.mo` and `<Model>.trace`, and evaluates the file over the CRML, Modelica
  and trace metamodels; see "Checking a translation" in `CONSTRAINT_LANGUAGE.md`.
  Per model it checks:
  - the class: its name, and `partial` exactly when a variable it has, own or
    inherited, is unbound;
  - nothing dropped at any depth (no `UNSUPPORTED` link, no `Placeholder`), no
    unresolved name, no reference into another model's class;
  - every top-level element of the model and of each library it reaches, once,
    of the right kind (function or block for an operator), directly in the
    model's class, with the CRML name for classes and variables; exact counts,
    so nothing is emitted twice or extra;
  - nothing of a root it does not reach (scope flows one way);
  - call sites and structure: each call reaches the function or block of the
    right library, operators call their dependencies, extends clauses and
    component types point at the generated classes.

  Every expected value follows from the CRML sources. The cases with only error
  files have none.
- A recurring line is `count [Model::name == "M"] / [Sequence] == 0`: no keyword
  call in `M` is left as an unparsed Sequence, i.e. every operator call was
  resolved.
- A correct model must not reach the generator with an unresolved `Sequence`.
  Today such a variable is silently dropped and the CLI still reports
  "translated", so a test runner must check the diagnostics and not just the
  exit status.

## Semantics assumed

- `union` acts like set union for the user, but the DOM stays separate: each
  library keeps its own Resource, and the model only refers into it.
- Dependencies are transitive. A library reached along several paths is loaded
  once, and listing the same dependency twice is harmless.
- A model may depend on a model. A library may not depend on a model.
- Scope flows one way only: a library does not see what its dependents declare.
- Libraries are loaded once per run (CLI instance), even when several models
  use them.
- `flatten { A, B } union` is only checked for bringing both A and B into
  scope; what else `flatten` means is not covered yet.
- Not covered yet (later features): name clashes and overriding between
  libraries, operator precedence across libraries, several definitions in one
  file, named (non-mixfix) operator calls (not in the grammar), members of
  library classes accessed from operators (`p.cav` is not supported).

## Cases

| Case | Tests | Expected |
|---|---|---|
| `template` | Template from a library | correct |
| `operator` | keyword Operator from a library | correct |
| `class_type` | library class as variable type, constructor binding into it | correct |
| `class_extends` | model class extends a library class | correct |
| `library_internal_call` | library operator calls another operator of the same library | correct |
| `library_internal_class_link` | class extension linked inside a library root | correct |
| `lifting_in_library` | `P.element` lifting inside a library operator | correct |
| `two_libraries` | `A union B union` | correct |
| `flatten_set` | `flatten { A, B } union` | correct |
| `duplicate_dependency` | `L union L union` (no ambiguity) | correct |
| `transitive` | operator reached through an intermediate library | correct |
| `transitive_library_call` | library operator calls an operator of its dependency | correct |
| `transitive_chain` | three levels of dependency, empty intermediate libraries | correct |
| `diamond` | one library reached through two paths, loaded once | correct |
| `model_union_model` | model depends on a model (variables and operators) | correct |
| `shared_library` | two models of one run share a library | correct |
| `local_and_library` | model and library operators mixed; local operator calls library one | correct |
| `unused_dependency` | dependency never used | correct |
| `empty_library` | empty library | correct |
| `file_name_differs` | lookup by declared name, not file name | correct |
| `unrelated_broken_file` | a file with a syntax error that nothing depends on; it is loaded partially | model correct |
| `missing_library` | dependency not found | semantic error |
| `not_in_scope_without_union` | a library loaded in the run but not unioned is not in scope | semantic error |
| `dependency_is_one_way` | a library cannot use what its dependent declares | semantic error |
| `library_depends_on_model` | library depends on a model | semantic error |
| `self_dependency` | library depends on itself | semantic error |
| `cycle` | A depends on B depends on A | semantic error (both) |
| `broken_dependency` | dependency has a syntax error; what of it builds is still used | syntax error + semantic error |
| `unresolved_name_in_library` | unknown name inside a library (NPE today) | library semantic error, model correct |
| `etl` | Category-free ETL with four models (see below) | correct |

### `etl`

`etl/ETL.crml` is `testModels/libraries/ETL.crml` without the `Category`
declarations (the grammar cannot parse them yet), with the operator bodies of
the `testModels/libraries/ETL_test` models. `Implies`, `BecomesTrueInside`,
`CountInside` and `CheckOver` declare the same variables as the ETL_test models
of the same name. So once libraries are emitted, the existing harnesses in
`verificationModels/libraries/ETL_test/<Name>/` and the references in
`refResults/libraries/ETL_test/` should apply to them unchanged.

## How the correct cases were checked

Every file parses exactly when its marker says it should. For every correct
model, a copy with its libraries' contents pasted into the model body (in
dependency order) translates with crmlcv2 with no diagnostics. So once
library loading works, a failure in a correct case comes from loading or
linking, not from an unsupported construct.
