#!/usr/bin/env node
// Translates each .crml file under <models> with the web build and compares
// the result with the .mo file that
// `./gradlew :compiler:translateModels -Pmodels=<models> -Pout=<expected>`
// wrote for it: models/a/B.crml with expected/a/B.mo.
// <build> is crml.js or crml.wasm (with crml.wasm-runtime.js next to it).
// usage: node test/compare.mjs <build> <models> <expected>

import fs from 'fs';
import path from 'path';

const [build, models, expected] = process.argv.slice(2);

async function load(file) {
  if (file.endsWith('.wasm')) {
    // The runtime is a classic script that defines the global TeaVM.
    const runtime = path.join(path.dirname(file), 'crml.wasm-runtime.js');
    (0, eval)(fs.readFileSync(runtime, 'utf-8') + '\n;globalThis.TeaVM = TeaVM;');
    const { exports } = await TeaVM.wasmGC.load(path.resolve(file));
    return exports.translate;
  }
  return (await import(path.resolve(file))).translate;
}
const translate = await load(build);

// In the order of `find <models> -name '*.crml' | LC_ALL=C sort`.
const files = [];
(function walk(dir) {
  for (const e of fs.readdirSync(dir, { withFileTypes: true })) {
    const f = path.join(dir, e.name);
    if (e.isDirectory()) walk(f);
    else if (f.endsWith('.crml')) files.push(f);
  }
})(models);
files.sort((a, b) => (a < b ? -1 : a > b ? 1 : 0));

let same = 0, failBoth = 0;
const problems = [];
for (const f of files) {
  const mo = translate(fs.readFileSync(f, 'utf-8'), '');
  const moFile = path.join(expected, path.relative(models, f).replace(/\.crml$/, '.mo'));
  if (!fs.existsSync(moFile)) {
    problems.push(`${f}: no ${moFile}`);
    continue;
  }
  const want = fs.readFileSync(moFile, 'utf-8');
  const failed = mo.startsWith('ERROR ');
  const wantFailed = want.startsWith('ERROR ');
  // The messages of failures name objects by their hash codes, so only the
  // failing is compared.
  if (failed && wantFailed) failBoth++;
  else if (wantFailed) problems.push(`${f}: translated, but the JVM failed: ${want.trim()}`);
  else if (failed) problems.push(`${f}: ${mo}`);
  else if (mo === want) same++;
  else problems.push(`${f}: differs from ${moFile}`);
}
problems.forEach((p) => console.log(p));
console.log(`${same} identical to the JVM, ${failBoth} failing in both, ${problems.length} different`);
process.exit(problems.length ? 1 : 0);
