'use strict';

const test = require('node:test');
const assert = require('node:assert');

const { runMigrations } = require('../src/db/migrate');
const { StubPool } = require('./helpers');

test('runMigrations applies new files and records them', async () => {
  const pool = new StubPool();
  await runMigrations(pool);
  // INSERT goes through pool.connect() client, not pool.query — inspect the client.
  const inserts = pool.client.queries.filter((c) => c.text.includes('INSERT INTO schema_migrations'));
  assert.equal(inserts.length, 1);
  assert.equal(inserts[0].params[0], '001_init.sql');
});

test('runMigrations skips already-applied files', async () => {
  const pool = new StubPool([
    { match: (t) => t.includes('WHERE filename = $1'), result: { rows: [{ 1: 1 }], rowCount: 1 } },
  ]);
  await runMigrations(pool);
  const inserts = pool.queryCalls.filter((c) => c.text.includes('INSERT INTO schema_migrations'));
  assert.equal(inserts.length, 0);
});
