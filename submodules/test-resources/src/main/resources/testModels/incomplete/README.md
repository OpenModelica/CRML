# Incomplete models

Models as an editor sees them while they are being written: most have a syntax
error. A file with syntax or build errors still gets a partial DOM, built from
the text the parser recovered; what cannot be built is left out. Each case
states in `expected.constraints` what of the partial DOM must be there (see
`CONSTRAINT_LANGUAGE.md` in `:util-test`).

Run by `IncompleteModelTest` (checks as for `library-loading`, see
`CaseFolderTest`) and `PartialDomTest` (no stage fails with an exception on
these files).

File names carry the usual expectation markers (`.syntax-error.crml`,
`.semantic-error.crml`).

| Case | What is incomplete | Kept |
|---|---|---|
| `declared_not_defined` | a variable without `is` (valid CRML) | the variable, without a definition |
| `missing_value` | `Boolean x is ;` | `x` without a definition, and the rest |
| `missing_semicolon` | a `;` is missing | both variables, `y` linked to `x` |
| `unclosed_model` | the model's `};` is missing | everything |
| `unclosed_class` | a class's `};` is missing | the class; what follows is read into it |
| `unfinished_word` | a word typed after a value | `x` |
| `missing_name` | `Boolean ;` | the other variable; the nameless one is left out |
| `unfinished_call` | `a 'implies' ;` (parses) | the call, as an unresolved Sequence |
| `operator_without_body` | `Operator … = ;` in a library | the operator, without a definition |
| `partial_library` | a library with one broken operator | the library's other operator, used by a model (which still gets a broken-dependency error) |

An unset single-valued feature cannot be checked directly (it makes a chain
unresolved), so "no definition" is written as `count [Variable::name == "x"] /
[Value] == 0`: the variable contains no value.
