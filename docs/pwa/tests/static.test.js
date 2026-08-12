"use strict";

const assert = require("node:assert/strict");
const fs = require("node:fs");
const path = require("node:path");
const test = require("node:test");
const vm = require("node:vm");

const root = path.resolve(__dirname, "..");

test("manifest is installable and declares iOS-sized icons", () => {
  const manifest = JSON.parse(fs.readFileSync(path.join(root, "manifest.webmanifest"), "utf8"));
  assert.equal(manifest.name, "懒羊羊当大厨~");
  assert.equal(manifest.display, "standalone");
  assert.ok(manifest.start_url.startsWith("./"));
  const sizes = new Set(manifest.icons.map((icon) => icon.sizes));
  assert.ok(sizes.has("180x180"));
  assert.ok(sizes.has("192x192"));
  assert.ok(sizes.has("512x512"));
});

test("service worker precaches every local app shell asset", () => {
  const source = fs.readFileSync(path.join(root, "service-worker.js"), "utf8");
  const expected = ["index.html", "styles.css", "core.js", "app.js", "manifest.webmanifest", "built-in-recipes.json", "yunfeng-special.json"];
  expected.forEach((asset) => assert.match(source, new RegExp(asset.replaceAll(".", "\\."))));
});

test("javascript parses without a browser runtime", () => {
  ["core.js", "app.js", "service-worker.js"].forEach((file) => {
    new vm.Script(fs.readFileSync(path.join(root, file), "utf8"), { filename: file });
  });
});

test("catalogs contain the retained Android content", () => {
  const builtIns = JSON.parse(fs.readFileSync(path.join(root, "data", "built-in-recipes.json"), "utf8"));
  const yunfeng = JSON.parse(fs.readFileSync(path.join(root, "data", "yunfeng-special.json"), "utf8"));
  assert.equal(builtIns.recipes.length, 15);
  assert.equal(yunfeng.recipes.length, 150);
  assert.equal(new Set(yunfeng.recipes.map((item) => item.id)).size, 150);
});

test("source includes safe areas, reduced motion, and compact icon tools", () => {
  const css = fs.readFileSync(path.join(root, "styles.css"), "utf8");
  const html = fs.readFileSync(path.join(root, "index.html"), "utf8");
  assert.match(css, /safe-area-inset-top/);
  assert.match(css, /safe-area-inset-bottom/);
  assert.match(css, /prefers-reduced-motion/);
  assert.match(html, /apple-mobile-web-app-capable/);
  assert.match(html, /apple-touch-icon/);
});
