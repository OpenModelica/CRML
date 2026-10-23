# CRML translator for web browsers

The CRML to Modelica translator of the compiler, compiled with [TeaVM](https://teavm.org) to JavaScript and
WebAssembly, so that a web page can translate CRML without Java, e.g. in the OpenModelica web editor.

It is built from the sources of the `language` and `compiler` projects and the jars of the `model` and `modelica`
projects: the parser, generated from `language/src/main/antlr/crml/language/grammar/crml.g4`, `crml.language`,
which builds the object model, and `crml.compiler.crmlcv2`, which translates it to Modelica. The command line, the
file handling and running OMC are not part of it. The Java sources are compiled for Java 8, as in the other projects.
The parser uses ANTLR 4.13.2 here instead of 4.9.2, whose runtime needs `java.util.UUID`, which TeaVM does not
provide.

Only `crml.web.CrmlWeb`, the entry point, and what the object model, which is built with EMF, needs to run in a
browser are here:

- `crml.web.teavm.EmfPlugin`, a TeaVM plug-in that makes EMF's package registry the plain one, without the
  system properties, security manager, class loaders and Eclipse plug-ins it looks for on a JVM;
- `crml.web.teavm.JavaxXmlSubstitution`, which takes the few `javax.xml` classes EMF refers to, which TeaVM does
  not provide, from `crml.web.emul.javax.xml`;
- `org.teavm.classlib.java.*`, the locks and object streams EMF refers to, which TeaVM does not provide either;
- stand-ins for the log4j API, which writes errors to the console, and for the Eclipse platform and OSGi, which
  never run in a browser.

The wasm-gc runtime of TeaVM 0.16 cannot make a `WeakReference` to `null`, which EMF does, so `copyWasmGCRuntime`
patches it. Both targets are built unoptimized: optimized, TeaVM 0.16 gives different files for the same sources.

## Build

```bash
./gradlew :web:web
```

writes

- `submodules/web/build/generated/teavm/js/crml.js`, an ES module (~1.5 MB, ~460 KB compressed), and
- `submodules/web/build/generated/teavm/wasm-gc/crml.wasm` with its loader `crml.wasm-runtime.js`
  (~2.5 MB, ~770 KB compressed), for browsers with WebAssembly GC.

The CI uploads both as the artifact `crml-compiler-web`.

## Release

Pushing a tag `web-<version>`, e.g. `web-1.0.0`, publishes the release of that name with `crml.js`, `crml.wasm`,
`crml.wasm-runtime.js`, this README and `SHA256SUMS` (`.github/workflows/web-release.yml`). The OpenModelica web
OMEdit downloads `crml.js` from such a release, pinned by its version and SHA-256 sum, to translate CRML files.
`crml.js` and `crml.wasm` are reproducible: building the same sources gives the same files.

## Usage

```js
import { translate } from './crml.js';
const modelica = translate(crmlText, '');   // the second argument is the `within` of the Modelica file
```

or

```html
<script src="crml.wasm-runtime.js"></script>
<script type="module">
  const { exports } = await TeaVM.wasmGC.load('crml.wasm');
  const modelica = exports.translate(crmlText, '');
</script>
```

`translate` returns the Modelica text that the omcv2 pipeline gives on the JVM for the same CRML file, or a text
starting with `ERROR ` when the translation fails.

## Demo

```bash
./gradlew :web:serveDemo            # or -Pport=<port>, the default is 8000
```

builds both translators and serves `demo/index.html` with them on `http://localhost:8000/` until stopped with
Ctrl+C. The page translates the CRML typed into it as you type, with `crml.js` or, chosen in the page or with
`?build=wasm`, with `crml.wasm`. `./gradlew :web:demo` only assembles the page in `build/demo`, for another server:
browsers load ES modules and WebAssembly only over HTTP.

## Test

```bash
MODELS=submodules/test-resources/src/main/resources/testModels
./gradlew :web:web :compiler:translateModels -Pmodels=$MODELS -Pout=build/web-expected
node submodules/web/test/compare.mjs submodules/web/build/generated/teavm/js/crml.js $MODELS build/web-expected
node submodules/web/test/compare.mjs submodules/web/build/generated/teavm/wasm-gc/crml.wasm $MODELS build/web-expected
```

translates every test model on the JVM, with `crml.compiler.util.TranslateModels` from the compiler's tests, and
with `crml.js` and `crml.wasm` (Node.js 22 or later), and checks that the results are the same. Models that fail
count as the same when they fail in both.
