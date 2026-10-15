# CRML translator for web browsers

The CRML to Modelica translator of the compiler, compiled with [TeaVM](https://teavm.org) to JavaScript and
WebAssembly, so that a web page can translate CRML without Java, e.g. in the OpenModelica web editor.

It is built from the sources of the `language` and `compiler` projects: the parser, generated from
`language/src/main/antlr/crml/language/grammar/crml.g4`, `crml.language` and `crml.compiler.translation`. Only
`crml.web.CrmlWeb`, the entry point, and a stand-in for the log4j API, which writes errors to the console, are here.
The command line, the file handling and running OMC are not part of it. The Java sources are compiled for Java 8,
as in the other projects. The parser uses ANTLR 4.13.2 here instead of 4.9.2, whose runtime needs `java.util.UUID`,
which TeaVM does not provide.

## Build

```bash
./gradlew :web:web
```

writes

- `submodules/web/build/generated/teavm/js/crml.js`, an ES module (~590 KB, ~175 KB compressed), and
- `submodules/web/build/generated/teavm/wasm-gc/crml.wasm` with its loader `crml.wasm-runtime.js`
  (~1 MB, ~310 KB compressed), for browsers with WebAssembly GC.

The CI uploads both as the artifact `crml-compiler-web`.

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

`translate` returns the text of the Modelica file that `java -jar compiler-all.jar` writes for the same CRML file,
or a text starting with `ERROR ` when the translation fails.

## Test

```bash
find submodules/test-resources/src/main/resources/testModels -name '*.crml' | LC_ALL=C sort > files.txt
java -jar submodules/compiler/build/libs/compiler-all.jar -o build/web-expected $(cat files.txt)
node submodules/web/test/compare.mjs submodules/web/build/generated/teavm/js/crml.js \
  submodules/test-resources/src/main/resources/testModels build/web-expected
```

translates every test model with `crml.js` and checks that the result is the same as the one of the jar.
