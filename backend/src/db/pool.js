'use strict';

const { Pool } = require('pg');

/**
 * Shared PostgreSQL pool.
 *
 * DATABASE_URL is the only required configuration:
 *   postgresql://user:pass@host:5432/animalbattle
 *
 * In tests the pool can be injected directly (see test/helpers.js).
 */
function createPool(connectionString = process.env.DATABASE_URL) {
  if (!connectionString) {
    throw new Error(
      'DATABASE_URL is not set. Point it at a PostgreSQL instance, e.g. ' +
        'postgresql://animalbattle:secret@localhost:5432/animalbattle'
    );
  }
  return new Pool({
    connectionString,
    max: 10,
    idleTimeoutMillis: 30_000,
    connectionTimeoutMillis: 5_000,
  });
}

module.exports = { createPool };
