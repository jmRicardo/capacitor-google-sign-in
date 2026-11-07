// The TS interface, both native implementations and the web stub must expose the same
// methods, plugin name and error codes. 0.x shipped `checkScopes` in TS and `checkScope`
// in Swift: every call failed and nothing noticed.
import assert from 'node:assert/strict';
import { readFileSync, readdirSync } from 'node:fs';
import { join } from 'node:path';
import { test } from 'node:test';

import ts from 'typescript';

const root = new URL('..', import.meta.url).pathname;
const read = (path) => readFileSync(join(root, path), 'utf8');
const sorted = (values) => [...new Set(values)].sort();

const swiftDir = 'ios/Sources/GoogleSignInPlugin';
const swift = readdirSync(join(root, swiftDir))
  .filter((file) => file.endsWith('.swift'))
  .map((file) => read(join(swiftDir, file)))
  .join('\n');
const javaDir = 'android/src/main/java/com/jmricardo/google/auth';
const javaPlugin = read(join(javaDir, 'GoogleSignInPlugin.java'));
const javaErrors = read(join(javaDir, 'GoogleSignInErrors.java'));

function definitions() {
  const source = ts.createSourceFile('definitions.ts', read('src/definitions.ts'), ts.ScriptTarget.Latest, true);
  let methods = [];
  let errorCodes = [];
  source.forEachChild((node) => {
    if (ts.isInterfaceDeclaration(node) && node.name.text === 'GoogleSignInPlugin') {
      methods = node.members.filter(ts.isMethodSignature).map((member) => member.name.getText(source));
    }
    if (ts.isTypeAliasDeclaration(node) && node.name.text === 'GoogleSignInErrorCode') {
      errorCodes = node.type.types.map((type) => type.literal.text);
    }
  });
  return { methods: sorted(methods), errorCodes: sorted(errorCodes) };
}

const ts_ = definitions();

test('the TS interface declares methods and error codes', () => {
  assert.deepEqual(ts_.methods, ['signIn', 'signOut']);
  assert.ok(ts_.errorCodes.length > 0);
});

test('iOS registers exactly the TS methods, and implements each one', () => {
  const registered = [...swift.matchAll(/CAPPluginMethod\(name:\s*"(\w+)"/g)].map((m) => m[1]);
  assert.deepEqual(sorted(registered), ts_.methods);
  for (const method of ts_.methods) {
    assert.match(swift, new RegExp(`@objc func ${method}\\(_ call: CAPPluginCall\\)`), `Swift lacks ${method}`);
  }
});

test('Android exposes exactly the TS methods', () => {
  const methods = [...javaPlugin.matchAll(/@PluginMethod[^\n]*\n\s*public void (\w+)\(PluginCall/g)].map((m) => m[1]);
  assert.deepEqual(sorted(methods), ts_.methods);
});

test('the web stub implements exactly the TS methods', () => {
  const methods = [...read('src/web.ts').matchAll(/^\s+async (\w+)\(/gm)].map((m) => m[1]);
  assert.deepEqual(sorted(methods), ts_.methods);
});

test('every platform uses the same plugin name', () => {
  const name = read('src/index.ts').match(/registerPlugin<\w+>\('(\w+)'/)[1];
  assert.equal(swift.match(/jsName = "(\w+)"/)[1], name);
  assert.equal(javaPlugin.match(/@CapacitorPlugin\(name = "(\w+)"\)/)[1], name);
});

test('the native error codes are exactly the TS ones', () => {
  const swiftEnum = swift.match(/enum GoogleSignInErrorCode: String \{([\s\S]*?)\n\}/)[1];
  const swiftCodes = [...swiftEnum.matchAll(/case (\w+)(?:\s*=\s*"(\w+)")?/g)].map((m) => m[2] ?? m[1]);
  assert.deepEqual(sorted(swiftCodes), ts_.errorCodes);

  const javaCodes = [...javaErrors.matchAll(/static final String \w+ = "(\w+)";/g)].map((m) => m[1]);
  assert.deepEqual(sorted(javaCodes), ts_.errorCodes);
});

test('only the plugin class carries @objc(Name), which `cap sync` reads as plugin classes', () => {
  const files = (dir) =>
    readdirSync(join(root, dir), { recursive: true })
      .filter((file) => file.endsWith('.swift'))
      .map((file) => read(join(dir, file)));
  const classes = files('ios').flatMap((text) => [...text.matchAll(/@objc\(([A-Za-z0-9_-]+)\)/g)].map((m) => m[1]));
  assert.deepEqual(classes, ['GoogleSignInPlugin']);
});
