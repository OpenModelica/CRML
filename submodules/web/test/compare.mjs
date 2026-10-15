#!/usr/bin/env node
// Translates each .crml file under <models> with the web build and compares
// the result with the .mo file the compiler jar wrote for it into <expected>,
// as `java -jar compiler-all.jar -o <expected> <files>` does.
// usage: node test/compare.mjs <crml.js> <models> <expected>

import fs from 'fs';
import path from 'path';

const [crmlJs, models, expected] = process.argv.slice(2);
const { translate } = await import(path.resolve(crmlJs));

// In the order of `find <models> -name '*.crml' | LC_ALL=C sort`, the order
// to give the files to the jar in.
const files = [];
(function walk(dir) {
  for (const e of fs.readdirSync(dir, { withFileTypes: true })) {
    const f = path.join(dir, e.name);
    if (e.isDirectory()) walk(f);
    else if (f.endsWith('.crml')) files.push(f);
  }
})(models);
files.sort((a, b) => (a < b ? -1 : a > b ? 1 : 0));

// The jar writes <expected>/<name>/<name>.mo, so of the files with the same
// name only the one translated last has its result there.
const last = new Map();
for (const f of files) last.set(path.basename(f, '.crml'), f);

let same = 0, failBoth = 0;
const problems = [];
for (const [name, f] of last) {
  const mo = translate(fs.readFileSync(f, 'utf-8'), '');
  const moFile = path.join(expected, name, name + '.mo');
  const want = fs.existsSync(moFile) ? fs.readFileSync(moFile, 'utf-8') : null;
  const failed = mo.startsWith('ERROR ');
  if (want === null && failed) failBoth++;
  else if (want === null) problems.push(`${f}: translated, but the jar failed`);
  else if (failed) problems.push(`${f}: ${mo}`);
  else if (mo === want) same++;
  else problems.push(`${f}: differs from ${moFile}`);
}
problems.forEach((p) => console.log(p));
console.log(`${same} identical to the jar, ${failBoth} failing in both, ${problems.length} different`);
process.exit(problems.length ? 1 : 0);
