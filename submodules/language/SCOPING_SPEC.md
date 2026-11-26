# Scoping and multi-library linking

This document is for contributors and maintainers of the `:language` module. It
describes how names are looked up in the CRML DOM, how a model is linked
against the libraries and models it depends on, and where this departs from
the CRML specification v1.2.

Status (2026-11-24): implemented. Scoping follows Modelica's lookup rules
(section 3.3) and works across files: a root sees what its `union`
dependencies declare, wherever those are loaded from (section 6).

The code is in:
- `org.openmodelica.crml.language.scope`: the scoping functions (`Scopes`) and
  lookup strategies;
- `org.openmodelica.crml.language.dom.util.ScopeResolver` and `DOMVisitor`:
  the linker;
- `org.openmodelica.crml.language.workspace`: loading, binding and ordering
  roots across files (`CrmlWorkspace`, `RootIndex`, `RootHeader`);
- `org.openmodelica.crml.language.validation.rules.NameConflicts`: names
  that must be unique;
- `:model`, `org.openmodelica.crml.model.base`: the namespace metamodel that
  everything above is built on (`CrmlObject.getNamespace()`, `Namespace` with
  owned, inherited and all members, `MemberKind`, `NamespaceSupport`).

Not covered (see also section 10):
- positions in the source text (content assist at a cursor, the LSP);
- a library path on the command line (section 6.5);
- member access (`tank.level`) and dotted paths (`L.X`).

## 1. Overview

The scoping utility has two functions:
- **list**: the declarations available at a point of the DOM, of a given
  kind. Its use case is content assist.
- **resolve**: the declaration a name denotes at a point. It is built on
  the same lookup as *list*.

Both are used by the linker (`ScopeResolver`) and by the operator-call parser
(`MixfixParser`, via `Scopes.operators`).

The structure of the lookup comes from the metamodel: every element knows its
enclosing `Namespace` (`getNamespace()`, by containment), and a namespace
knows its members. The rules on top of that structure (nearest level first,
visibility, hiding, ambiguity, self-reference, built-ins) live in
`Scopes` and `UpwardScopeStrategy`.

Design choices, in short:

| Topic | Choice |
|---|---|
| Query | A DOM element plus an expected kind. A convenience form takes the host and the `EReference` being set, and takes the kind from the reference's type. |
| Kind | An `EClass`. A candidate matches if `kind.isInstance(candidate)`. |
| Result of *list* | A flat list, nearest declarations first. Hidden and inaccessible declarations are left out. |
| Same name twice at one level | Both are returned; *resolve* reports the ambiguity. |
| Built-ins | Returned by *list* in content assist mode only; never resolved by name. |
| Result of *resolve* | Found, ambiguous (with the candidates), self-reference, not accessible (with the reason), or not found. The linker reports each differently. |
| Lookup | Upwards through the enclosing namespaces, with Modelica's visibility: a namespace sees its own and inherited members, and of the namespaces around it only classes, operators and constants. |
| Declaration order | Does not matter. A declaration cannot refer to itself. |
| Where lookup starts | Pluggable per reference (`ScopeStrategy`). Constructor bindings use their own strategy; member access will be another. |
| State | None. Nothing is cached: the linker changes the DOM, so every call computes its result from the current DOM. |

## 2. The namespace metamodel (`:model`)

`Namespace` is mixed into the metamodel in `crml.xcore`. The namespaces are:

| Namespace | Owned members | Base namespaces (inherited from) |
|---|---|---|
| `Class` | its variables | its superclasses (unset ones left out) |
| `Library` | its classes, then its operators | its bound `LibraryDependency`s (unbound ones left out) |
| `Model` (a `Library`) | the members of a library, then its variables, objects and sets | as for a library |
| `CustomOperator` | its parameters (the variables of its header) | none |

- `getOwnedMembers()`: declared here, in declaration order.
- `getInheritedMembers()`: what the base namespaces pass on, transitively:
  per base, its owned members and the inherited ones it does not hide.
  Inherited members hidden by an owned member are left out. A member reached
  along several paths (a diamond, or `L union L union`) is listed once.
  Cycles are cut.
- `getMembers()`: owned, then inherited.

`getAllVariables()`, `getAllClasses()` and `getAllOperators()` are typed
views of `getMembers()`. For a model they include what its dependencies
declare; the compiler uses them (section 6.4).

`MemberKind` groups members into `VALUE` (variables, objects and sets: the
`Element`s), `TYPE` (classes) and `OPERATOR` (operators and templates).

### 2.1 Hiding

`NamespaceSupport.hides(nearer, farther)` is true if they are different
members with the same `MemberKind` and the same *hiding key*
(`NamespaceSupport.hidingKey`):
- the member's name;
- for an operator without a name (one written in natural syntax, such as
  `'neg'`), its keyword signature (`CustomOperator.getSignature()`): the
  header with each keyword as written and each parameter as `_`, e.g.
  `_ 'and' _`. Two unnamed operators therefore hide each other only if their
  keywords are written the same way.

The same function decides hiding within a namespace (`:model`) and across
levels (`UpwardScopeStrategy`), so the two always agree.

> **Not supported: overloading.** The CRML specification (§4.13, p.95)
> identifies an operator by its *typed* signature (`T$w1$T1$w2$T2`), so
> operators with the same keywords and different parameter types are
> overloads. The signature above leaves the types out: such operators hide
> each other and are reported as name conflicts (section 7). Overloading
> needs type inference to choose an overload at a call. TODO in
> `OperatorSupport.signature`, `NamespaceSupport.hidingKey` and
> `NameConflicts`.

## 3. The `scope` package

### 3.1 Types

```java
/** One declaration available at a point. */
public final class ScopeEntry {
    String name();                   // null for an operator written in natural syntax
    NamedEntity element();           // the declaration; null for a built-in
    Namespace namespace();           // where it is a member, owned or inherited; null for a built-in
    MemberKind kind();               // VALUE, TYPE or OPERATOR
    boolean builtIn();
    AccessRestriction restriction(); // why it cannot be used here; null if it can
    boolean accessible();            // restriction() == null

    static ScopeEntry of(NamedEntity member, Namespace namespace);
    static ScopeEntry restricted(NamedEntity member, Namespace namespace, AccessRestriction why);
    static ScopeEntry builtIn(String name, MemberKind kind);
}

public enum ScopeMode {
    RESOLUTION,     // declarations only
    CONTENT_ASSIST  // declarations, then built-ins
}

/** Why a declaration found by a lookup cannot be used where the name is written. */
public enum AccessRestriction {
    ENCLOSING_NON_CONSTANT; // a non-constant value of a namespace around this one
    String explanation();   // a phrase for the diagnostic
}

/** The result of resolving a name. */
public final class Resolution {
    enum Status { FOUND, AMBIGUOUS, SELF_REFERENCE, NOT_ACCESSIBLE, NOT_FOUND }
    String name();
    Status status();
    boolean found();
    NamedEntity target();          // FOUND only, else null
    List<ScopeEntry> candidates(); // one if FOUND, several if AMBIGUOUS,
                                   // the restricted ones if NOT_ACCESSIBLE, none otherwise
}

/** Where a lookup starts, and what it sees. Chosen per reference. */
public interface ScopeStrategy {
    /** Nearest first; hidden ones and the enclosing declaration left out;
     *  not filtered by kind; no built-ins; inaccessible ones as restricted entries. */
    List<ScopeEntry> entries(EObject context);

    /** The declaration context is written in, which a name there must not
     *  refer to; null if self-reference does not apply. */
    default NamedEntity declaration(EObject context) { return null; }
}
```

`AccessRestriction` is an enum so that later rules (e.g. `protected` members,
access through a component) can add their own reasons.

### 3.2 `Scopes`, the entry point

```java
public class Scopes {
    Scopes();                              // default strategy: new UpwardScopeStrategy()
    Scopes(UpwardScopeStrategy upward);
    UpwardScopeStrategy upward();          // the default strategy

    // (1) list: accessible entries of the kind, nearest first
    List<ScopeEntry> list(EObject context, EClass kind, ScopeMode mode);
    List<ScopeEntry> list(EObject context, EClass kind, ScopeMode mode, ScopeStrategy strategy);
    List<ScopeEntry> list(Namespace start, EClass kind, ScopeMode mode); // start is included

    // (2) resolve
    Resolution resolve(EObject context, EClass kind, String name);
    Resolution resolve(EObject context, EClass kind, String name, ScopeStrategy strategy);
    Resolution resolve(EObject host, EReference reference, String name); // kind = reference.getEReferenceType()
    Resolution resolve(EObject host, EReference reference, String name, ScopeStrategy strategy);

    // For MixfixParser (section 5)
    List<CustomOperator> operators(Namespace start);
}
```

`Scopes` is an instance class, so the rules can be changed in a subclass or
with another default strategy.

### 3.3 Default strategy: the upward walk (`UpwardScopeStrategy`)

For a context element, the walk starts at `context.getNamespace()`. With the
`list(Namespace start, …)` form it starts at `start` itself, and nothing is
left out as a self-reference. The walk itself is
`UpwardScopeStrategy.entriesFrom(Namespace start, NamedEntity self)`, which
other strategies can reuse.

1. **Namespaces, nearest first.** These are `start`, `start.getNamespace()`,
   and so on until `null`. Each gives its `getMembers()`: its owned members,
   then its inherited members (section 2).

   The walk follows *containment*. The namespaces around a declaration are
   those of its own file, so scope flows one way: a library never sees what
   the models depending on it declare, and the members of a dependency are
   seen only as inherited members of the root that lists it.

2. **Visibility (Modelica).** The compiler turns a class into a nested
   Modelica model and an operator into a nested function or block, and
   Modelica lets a nested class use only the constants of the classes around
   it. So:
   - the starting namespace gives all its members: the variables of a class
     and of its superclasses, the parameters of an operator, everything a root
     and its dependencies declare;
   - every namespace around it gives its classes, its operators and its
     `constant` variables. Its other variables, objects and sets are returned
     as *restricted* entries (`ScopeEntry.restricted`, reason
     `AccessRestriction.ENCLOSING_NON_CONSTANT`), decided by the overridable
     `UpwardScopeStrategy.restriction(NamedEntity)`.

   Consequences:
   - an operator body sees its parameters, and of the model only classes,
     operators and constants, not the model's variables;
   - a class body sees its own and inherited variables, and of the model
     only classes, operators and constants;
   - the variables of a model are visible in that model's own declarations,
     including those it inherits from models it depends on
     (`model M is N union`).

   Restricted entries take part in hiding (step 3), so a name that denotes
   one is reported as not accessible (3.4) and is never resolved to something
   farther away. `list` and content assist leave them out.

3. **Hiding across levels.** A member of an outer namespace is left out if a
   nearer level already gave a member that `NamespaceSupport.hides` it (same
   hiding key and `MemberKind`, section 2.1). Within one namespace, `:model`
   has already applied hiding, and two members of the same level with the
   same key are both kept.

4. **Filtering by kind** (in `Scopes`). After hiding, only the entries with
   `kind.isInstance(element)` are kept. Hiding is decided before filtering: a
   variable `X` hides an outer variable `X` even when the query asks for
   classes only. Because hiding is per `MemberKind`, it does not affect the
   classes.

5. **Self-reference.** The *enclosing declaration* of a context
   (`UpwardScopeStrategy.declaration`) is its nearest container, or the
   context itself, that is an `Element` (a variable, object or set) or a
   `CustomOperator`. It takes part in hiding (step 3), but it is never
   returned. As a result:
   - `Boolean x is x;` does not resolve to an outer `x`. It is reported as a
     self-reference.
   - An operator is not visible in its own body, so it cannot call itself
     there. The mixfix parser applies the same rule to calls in natural
     syntax (section 5).
   - A class is not an enclosing declaration, so `class Node { Node next; }`
     works.

6. **Built-ins** (in `Scopes`, `CONTENT_ASSIST` mode only). They are added
   after the declarations:
   - the built-in types when `kind` accepts a `Class`
     (`kind.isSuperTypeOf(Class)`, e.g. `Domain`), i.e. at a type position;
   - the built-in operators when `kind` accepts an `Operator`.

   Both come from one class, `BuiltIns`: `TYPES` (`String`, `Boolean`,
   `Integer`, `Real`, `Event`, `Clock`, `Period`, `Periods`, `Requirement`),
   `BINARY_OPERATORS` and `UNARY_OPERATORS`, keyed by their CRML spelling.
   The expression and type builders read the same tables, so the builders
   and content assist cannot disagree. Built-ins are never resolved by name:
   the grammar recognises them, and the builders create
   `BuiltinTypeReference`s and operator nodes directly.

### 3.4 Resolution

`resolve(context, kind, name, strategy)`:

1. Takes `strategy.entries(context)` and keeps those named `name` with
   `kind.isInstance(element)`, split into accessible matches and restricted
   ones.
2. The result:

   | Accessible matches | Status |
   |---|---|
   | 1 | `FOUND` |
   | 2 or more | `AMBIGUOUS`, with all of them |
   | 0, and `strategy.declaration(context)` has this name and a matching kind | `SELF_REFERENCE` |
   | 0, but restricted entries of this name and kind | `NOT_ACCESSIBLE`, with them |
   | 0 otherwise | `NOT_FOUND` |

   Restricted entries never make a name resolve; they only explain a failure.

Because of hiding, the candidates of an `AMBIGUOUS` result always come from
the same level. The typical case is two dependencies of a root that both
provide `X` (which `NameConflicts` also reports, section 7).

### 3.5 Constructor-binding strategy (`ConstructorBindingScopeStrategy`)

Used for `Binding.element` inside a `ConstructorValue`
(`Pump { id = 1 }`):
- if the nearest enclosing constructor's type is a `UserTypereference` to a
  `Class`, the entries are `cls.getMembers()`, inherited variables included;
- otherwise the entries are empty and *resolve* reports `NOT_FOUND`. While
  the class is not linked yet this is also the case, and the linker retries
  later (section 4).

There is no upward walk and no self-reference: a binding names a member of
the class.

## 4. Linker: `ScopeResolver` and `DOMVisitor`

- While building, `DOMVisitor.link(host, feature, id, options)` queues a
  `LinkerTask`. `ScopeResolutionOptions.strategy` selects the strategy:
  `DEFAULT` (the upward walk) or `CONSTRUCTOR_BINDING`.
- `ScopeResolver.link`:
  1. picks the strategy from the options;
  2. calls `Scopes.resolve(host, feature, id, strategy)`;
  3. on `FOUND`, sets the reference (or adds to it, for a many-valued one);
  4. returns the `Resolution`.
- `DOMVisitor.link()` runs the queued tasks until no more of them resolves.
  A task is retried while it is not `FOUND`, because some references depend
  on others being linked first (e.g. a binding needs the constructor's
  class). The tasks left are returned as `UnresolvedLink`s, each carrying the
  last `Resolution`.
- `CrmlWorkspace.link` turns each `UnresolvedLink` into a diagnostic at the
  position of the host:

  | Status | Diagnostic kind | Message |
  |---|---|---|
  | `NOT_FOUND` | `UNRESOLVED_REFERENCE` | `Unresolved reference …` |
  | `AMBIGUOUS` | `AMBIGUOUS_REFERENCE` | `Ambiguous reference …, it can denote: Class X of A (a.crml), Class X of B (b.crml)` |
  | `SELF_REFERENCE` | `SELF_REFERENCE` | `'x' cannot refer to itself (Variable.…)` |
  | `NOT_ACCESSIBLE` | `INACCESSIBLE_REFERENCE` | `'x' cannot be used here: it is the Variable x of M, and of the namespaces around this one, only constants can be used` |

  An ambiguity candidate is named by the namespace that *declares* it, not
  by the namespace where it is visible (the root that inherits it), followed
  by its file name.

## 5. Operator calls: `MixfixParser`

- `Scopes.operators(start)` returns the visible `CustomOperator`s that can be
  written in natural syntax (`OperatorUtil.isMixfix`), using the same walk:
  own operators first, then inherited ones (from dependencies), then those of
  outer namespaces. Unnamed operators with the same signature hide each other
  (section 2.1), so an own `'neg'` hides a library `'neg'` instead of making
  calls ambiguous.
- Constructors: `MixfixParser(List<CustomOperator>, BuildContext[, TypeCheck])`,
  and `MixfixParser(Library root, BuildContext[, TypeCheck])`, which uses
  `Scopes.operators(root)` (a `Model` is a `Library`).
- `DOMVisitor.operatorParser()` builds one parser per root. Operators
  declared in classes would need one parser per class; that is not done.
- **Self-calls.** The parser works per root, not per reference, so
  `MixfixParser.parse` leaves out the operator whose body contains the
  sequence being parsed.
- **Effect on precedence.** Each operator is its own precedence level, in
  list order, and **an operator earlier in the list binds more loosely**.
  "Own operators first" therefore means that a library's operators bind
  tighter than the model's own operators. See `MIXFIX_PARSER.md`.
- Listing a library twice (`L union L union`) gives each operator once,
  because `:model` keeps a member reached along several paths once.

## 6. Multiple files and libraries: the workspace

All files of a run (one test case, one editor workspace, one compiler job)
live in one `CrmlWorkspace`, which wraps one EMF `ResourceSet`, one
`CrmlResource` per file. References between files are plain cross-resource
references: a model refers into the library resources, nothing is copied.
A library is loaded, bound and linked once per workspace, however many models
use it.

### 6.1 Loading and the root index

- `new CrmlWorkspace(List<Path> libraryPath)` and `loadLibraryPath()`: every
  `.crml` file directly in each folder of the library path is loaded, in
  file-name order, whether a model uses it or not. Libraries are found by
  their *declared* name, which is only known once a file is parsed, not by
  their file name.
- `load(Path)` loads one file (once: loading it again gives the same
  resource). `load(URI, String)` and `load(String)` load text, e.g. an editor
  buffer; loading the same URI again replaces the resource.
- Loading parses the file, reads its `RootHeader` from the parse tree (kind,
  name, dependency names with their positions) and builds the DOM, without
  linking. A file with syntax errors is loaded too, with a partial DOM.
- `RootIndex` maps declared names to resources. It is built from the headers,
  so a file with syntax errors is indexed as well, and is attached to the
  resource set as an adapter (`RootIndex.of(EObject)`). `lookup(name)` gives
  `FOUND` (one file, DOM complete), `BROKEN` (one file with syntax or build
  errors; `root()` gives its partial DOM, if any), `MISSING` or `DUPLICATE`.

### 6.2 Building dependencies

`RootBuilder.parse` creates one `LibraryDependency` per name in the header,
with `name` set and `library` unset: `flatten { A, B } union` gives two of
them. The position of the name is recorded with
`BuildContext.position(EObject, ParseTree)`, so dependency diagnostics point
at the name.

### 6.3 Stages (`CrmlWorkspace.resolve()`)

| Stage | Does |
|---|---|
| BUILD | Parse and build every file (at load time). |
| BIND | For every resource not resolved yet, before any is linked: each `LibraryDependency` gets `library` set to `RootIndex.lookup(name).root()` when that is a `Library`. This includes the partial DOM of a `BROKEN` file, so a dependent gets what of it could be built. It stays unset when the name is missing or declared by several files. Binding reports nothing; validation does (6.4). |
| ORDER | The bound roots are ordered dependencies first: depth first over the bound dependencies, each root after the ones it depends on, a cycle cut where it closes, load order breaking ties. |
| LINK, OPERATOR_CALLS, LIFT | Per resource, in that order. Libraries are therefore linked before the models that use them. |

A stage that fails reports its problems as diagnostics of the resource and
the next stages still run. A resolved resource is skipped by later calls of
`resolve()`. In tests, `InspectingWorkspace` can stop after a given stage
(`BUILD`, `BIND`, `LINK`, `OPERATOR_CALLS`, `LIFT`) and records a
`StageResult` per stage, e.g. a `BindResult` with each dependency and its
lookup status.

### 6.4 Dependency validation (`CrmlWorkspace.validate`)

`validate` reports each problem on the dependent, at the dependency's name.
It reads the headers, not the DOM, so a file whose DOM was not built still
gets its dependency problems reported.

| Kind | When |
|---|---|
| `MISSING_DEPENDENCY` | no file declares the name |
| `BROKEN_DEPENDENCY` | the declaring file has syntax or build errors; its own errors are not repeated |
| `AMBIGUOUS_DEPENDENCY` | several files declare the name |
| `DEPENDENCY_CYCLE` | the root reaches itself through its dependencies, itself included (`Lib is Lib union`); reported on every root in the cycle |
| `LIBRARY_DEPENDS_ON_MODEL` | a `library` depends on a `model` |

The same dependency listed twice (`L union L union`) is not an error.

### 6.5 Compiler

The compiler emits the contents of the libraries a model reaches directly
into the model's Modelica class: `ModelTransformer` iterates
`model.getAllClasses()`, `getAllOperators()` and `getAllVariables()`, which
include inherited members (section 2). Each top-level element of the model
and of every library it reaches is emitted once.

The command line (`Job`) still compiles each file in a workspace of its own,
without a library path, so files compiled from the command line do not see
each other. Multi-file translation is exercised through `CrmlWorkspace` with
a library path (the `LibraryLoading` tests of `:compiler`).

## 7. Name conflicts

The validation rule `NameConflicts` (`validation/rules`, registered in
`CRMLValidator.defaultRules()`, code 2) reports names that are not unique. It
is a validation error, reported eagerly: a conflict between two dependencies
is reported whether the name is used or not.

**Which members conflict.**
1. One namespace for all kinds: a variable, an object, a set, a class and a
   named operator with the same name conflict. Operators are values of the
   type `Operator` in CRML (§4.13), so an operator `f` and a variable `f`
   conflict too.
2. Operators that can be called in natural syntax also conflict when their
   keyword signatures (`_ 'and' _`) are the same. An unnamed operator
   conflicts only with other operators: a variable `implies` does not clash
   with `Template b1 'implies' b2`.

**Where.**

| Pair | Result | Reported on |
|---|---|---|
| two own members of a namespace (root, class, operator parameters) | error | the later member |
| an own member of a root and a member of a `union` dependency | error | the own member |
| members of two different dependencies of a root | error, even if unused | the later `LibraryDependency` |
| one declaration reached through two dependencies (diamond), or a dependency listed twice | no conflict | |
| an own member of a class and an inherited one | not checked (a redeclaration) | |
| members of two superclasses of a class | error | the class (`superClasses`) |
| an operator parameter and a declaration around the operator | no conflict (the parameter hides it) | |

**Lookup does not depend on this rule.** `:model` still lets an own member
hide an inherited one, and `Scopes` still reports a name provided by two
dependencies as `AMBIGUOUS` where it is used. On a model without conflicts
both give the same result as unique names would; on a model with conflicts
the user gets the validation error, and the linker keeps resolving as much as
it can.

**Files with syntax errors.** A member built from text in which the parser
recovered from an error (`SourceMap.recovered`: a recovery point within its
source or on the token just before it) is not checked. Such members are often
fragments: after a missing `;`, `Real d is time from e;` also gives a
variable `e` of type `from`, which would conflict with the real `e`.

## 8. Tests

- `:model`, `NamespaceTest`: owned, inherited and all members, hiding and the
  hiding key (two unnamed operators with different keywords do not hide each
  other).
- `:language`, `ScopesTest`: one test per rule of section 3 (order, hiding
  per kind, kind filter, ambiguity between dependencies, an own declaration
  hiding the dependencies, self-reference of variables and operators,
  visibility from operator and class bodies, constants of enclosing
  namespaces, a model seeing the variables of the models it depends on, the
  constructor strategy, built-ins only in content assist, `operators()`
  order and hiding).
- `:language`, `NameConflictsTest`: the table of section 7.
- `:language`, `CrmlWorkspaceTest`: the BIND trace, libraries linked before
  their dependents, and each dependency diagnostic of 6.4.
- `test-resources/.../testModels/library-loading`: 30 cases, one folder each,
  described in that folder's `README.md`. Every case has an
  `expected.constraints` checked on the linked DOM (`LibraryLoadingTest` in
  `:language`), and every case with a valid model has a
  `translation.constraints` checked on the generated Modelica
  (`LibraryLoading` in `:compiler`).

## 9. Deviations from the CRML specification v1.2

Page numbers are those of `documentation/crml_specification/CRML_specification_v1.2.pdf`.

| # | Specification | This implementation | Why / status |
|---|---|---|---|
| D1 | §4.3 (p.81), §3.20.2 (p.64), §3.22 (p.68): an expression refers to one outside its scope by a relative path; an attribute may depend on attributes of other objects. Taken literally, no name is found by plain upward lookup. | Plain names are looked up upwards, as in Modelica: classes, operators and constants of the namespaces around; only own and inherited variables (3.3). Paths are not supported. | The examples rely on upward lookup (`during`, `abs` and `Pump` used inside class bodies, p.120). Out of scope: paths, and reaching non-constant elements outside the current class. |
| D2 | p.120 and p.121: class bodies write `CoolingSystem coolingSystem is CoolingSystem.coolingSystem;` and `HeatExchanger {} heatExchangers is CoolingSystem.heatExchangers;`, reaching a model-level instance from inside a class. | Rejected: outward paths are not supported, and a non-constant of the model is not visible in a class (`INACCESSIBLE_REFERENCE` by plain name). | Not supported yet. A possible resolution is Modelica's `inner`/`outer`: the model-level instance declared `inner`, the class attribute `outer`. Until then: an `external` attribute bound later, or a value passed when the object is constructed. |
| D3 | p.101: the names of a merged set (`union`) must be unique. | Reported by `NameConflicts`. Lookup still hides dependency members behind own ones and reports ambiguity at use (section 7). | Same result on correct models; lookup keeps working on wrong ones. |
| D4 | p.95: operators are overloaded by typed signature (`T$w$T1` vs `T$w$T2`). | Not supported: the signature leaves types out, so overloads hide each other and are name conflicts. | Needs type inference at the call. TODO in `OperatorSupport.signature`, `NamespaceSupport.hidingKey` and `NameConflicts`. |
| D5 | p.116, p.122: a library declares constants (`constant Real pi is 3.141592;`). | Rejected while building: `Library` has no variables in the metamodel. | TODO in `RootBuilder.processLibrary` and `UpwardScopeStrategy.restriction`; the visibility rule already accepts them once kept. |
| D6 | p.118, p.122 and p.123: other roots are reached by path without a dependency (`filter CoolingSystem (...)`, `CoolingSystemLib.CoolingSystem`). | Only dependencies (`L union`) are visible. A root loaded in the same workspace but not listed is not in scope (`not_in_scope_without_union`). | Needs dotted names, which the grammar does not have. A future strategy would start at the `RootIndex`. |
| D7 | §4.21–4.23 (p.115, p.123): models, libraries and packages can be nested (`model M'` inside `model M`, `M'.ai'`). | Not supported: roots are not nested. | More restrictive than the specification; kept as is. |
| D8 | §3.23.2 and p.115: redeclaration of an inherited attribute (`redeclare a1`, `a → a'`). | Not checked: an own class member with the name of an inherited one simply hides it. | Out of scope. |
| D9 | §4.18: `parameter` is a variable constant over one simulation run. | Not counted as a constant for visibility: like Modelica, only `constant` is visible from the namespaces inside. | `parameter` is only in `uninstantiated_def` in the grammar today. |
| D10 | p.120: `eps` is used in class `CoolingSystem` but never declared. | Reported as `UNRESOLVED_REFERENCE`. | An error in the specification's example. |
| D11 | §3.20.2 (p.64): names are unique in a set, across all kinds. | Followed for conflicts (section 7). Hiding across levels is still per `MemberKind`: a class with a variable `Pump` can still use the class `Pump` of the model. | Making it independent of the kind was left out for now. In the generated Modelica, `Pump` inside such a class would denote the component. |

## 10. Not supported yet

- Member access (`o.a`, including `o.a is v` as a target) and dotted paths;
  a `ScopeStrategy` for them would plug in like the constructor-binding one.
- Reaching a model-level instance from inside a class (D2), e.g. through
  Modelica's `inner`/`outer`.
- Operator overloading (D4) and library constants (D5).
- Operators declared in classes as candidates for natural-syntax calls (one
  mixfix parser per root, section 5).
- Partial classes, modifications and redeclarations.
- A library path on the command line (6.5).
- Content assist at a source position (the LSP); `Scopes.list` in
  `CONTENT_ASSIST` mode is ready for it.
