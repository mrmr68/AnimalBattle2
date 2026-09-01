'use strict';

/**
 * In-memory stub pool for unit tests. Implements the small surface of
 * pg.Pool used by store.js: query(text, params) and connect() returning a
 * client with query() and release(). Each test seeds expected results.
 */
class StubClient {
  constructor(responses) {
    this.responses = responses; // Array of { match(text), result }
    this.queries = [];
  }
  async query(text, params) {
    this.queries.push({ text, params });
    for (const r of this.responses) {
      if (r.match(text)) return typeof r.result === 'function' ? r.result(params) : r.result;
    }
    return { rows: [], rowCount: 0 };
  }
  release() {}
}

class StubPool {
  constructor(responses = []) {
    this.client = new StubClient(responses);
    this.queryCalls = [];
  }
  async query(text, params) {
    this.queryCalls.push({ text, params });
    return this.client.query(text, params);
  }
  async connect() {
    return this.client;
  }
  async end() {}
}

module.exports = { StubPool, StubClient };
