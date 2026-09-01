'use strict';

const { readFileSync, readdirSync } = require('node:fs');
const path = require('node:path');

const { createPool } = require('./pool');

/**
 * Minimal, ordered SQL migration runner.
 * Applies every .sql file in backend/migrations in filename order,
 * tracking applied files in schema_migrations.
 */
async function runMigrations(pool) {
  await pool.query(`
    CREATE TABLE IF NOT EXISTS schema_migrations (
      filename   TEXT PRIMARY KEY,
      applied_at TIMESTAMPTZ NOT NULL DEFAULT now()
    )
  `);

  const dir = path.join(__dirname, '..', '..', 'migrations');
  const files = readdirSync(dir).filter((f) => f.endsWith('.sql')).sort();

  for (const file of files) {
    const already = await pool.query('SELECT 1 FROM schema_migrations WHERE filename = $1', [file]);
    if (already.rowCount > 0) continue;

    const sql = readFileSync(path.join(dir, file), 'utf8');
    const client = await pool.connect();
    try {
      await client.query('BEGIN');
      await client.query(sql);
      await client.query('INSERT INTO schema_migrations (filename) VALUES ($1)', [file]);
      await client.query('COMMIT');
      console.log(`[migrate] applied ${file}`);
    } catch (err) {
      await client.query('ROLLBACK');
      throw new Error(`Migration ${file} failed: ${err.message}`);
    } finally {
      client.release();
    }
  }
}

/** CLI entry: `node src/db/migrate.js` */
if (require.main === module) {
  const pool = createPool();
  runMigrations(pool)
    .then(() => {
      console.log('[migrate] done');
      return pool.end();
    })
    .catch((err) => {
      console.error('[migrate] failed:', err.message);
      process.exitCode = 1;
      return pool.end();
    });
}

module.exports = { runMigrations };
