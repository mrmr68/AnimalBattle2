'use strict';

const test = require('node:test');
const assert = require('node:assert');

const { createApp } = require('../src/server');
const { StubPool } = require('./helpers');

/** Minimal JSON fetch against the express app without extra deps. */
async function call(app, method, path, { headers = {}, body } = {}) {
  const server = app.listen(0);
  await new Promise((resolve) => server.once('listening', resolve));
  const port = server.address().port;
  try {
    const res = await fetch(`http://127.0.0.1:${port}${path}`, {
      method,
      headers: { 'content-type': 'application/json', ...headers },
      body: body === undefined ? undefined : JSON.stringify(body),
    });
    const json = await res.json().catch(() => null);
    return { status: res.status, json };
  } finally {
    await new Promise((resolve) => server.close(resolve));
  }
}

const PLAYER_ROW = {
  id: 1, device_id: 'dev1', name: 'Ali', level: 1, xp: 0, coins: 100, trophies: 0,
  selected_animal_id: 'lion', unlocked_animals: ['lion'], animal_upgrades: {},
};

test('POST /players validates deviceId', async () => {
  const app = createApp({ pool: new StubPool() });
  const res = await call(app, 'POST', '/api/v1/players', { body: { name: 'x' } });
  assert.equal(res.status, 400);
});

test('POST /players upserts and returns player', async () => {
  const pool = new StubPool([
    { match: (t) => t.includes('INSERT INTO players'), result: { rows: [PLAYER_ROW], rowCount: 1 } },
  ]);
  const app = createApp({ pool });
  const res = await call(app, 'POST', '/api/v1/players', { body: { deviceId: 'dev1', name: 'Ali' } });
  assert.equal(res.status, 200);
  assert.equal(res.json.id, 1);
});

test('GET /players/:deviceId returns 404 when unknown', async () => {
  const app = createApp({ pool: new StubPool() });
  const res = await call(app, 'GET', '/api/v1/players/ghost');
  assert.equal(res.status, 404);
});

test('PATCH /players/me requires X-Player-Id', async () => {
  const app = createApp({ pool: new StubPool() });
  const res = await call(app, 'PATCH', '/api/v1/players/me', { body: { name: 'New' } });
  assert.equal(res.status, 401);
});

test('PATCH /players/me updates profile', async () => {
  const pool = new StubPool([
    { match: (t) => t.includes('UPDATE players SET'), result: { rows: [{ ...PLAYER_ROW, name: 'NewName' }], rowCount: 1 } },
  ]);
  const app = createApp({ pool });
  const res = await call(app, 'PATCH', '/api/v1/players/me', {
    headers: { 'X-Player-Id': '1' },
    body: { name: 'NewName' },
  });
  assert.equal(res.status, 200);
  assert.equal(res.json.name, 'NewName');
});

test('POST /battles validates payload', async () => {
  const app = createApp({ pool: new StubPool() });
  const res = await call(app, 'POST', '/api/v1/battles', {
    headers: { 'X-Player-Id': '1' },
    body: { playerAnimalId: 'lion', won: 'yes' },
  });
  assert.equal(res.status, 400);
});

test('POST /battles rejects out-of-range rewards', async () => {
  const app = createApp({ pool: new StubPool() });
  const res = await call(app, 'POST', '/api/v1/battles', {
    headers: { 'X-Player-Id': '1' },
    body: { playerAnimalId: 'lion', opponentName: 'x', opponentAnimalId: 'wolf', won: true, rewardCoins: 5000, rewardTrophies: 1 },
  });
  assert.equal(res.status, 400);
});

test('POST /battles records and returns rewards', async () => {
  const pool = new StubPool([
    { match: (t) => t.includes('INSERT INTO battles'), result: { rows: [{ id: 9, created_at: new Date() }], rowCount: 1 } },
    { match: (t) => t.includes('UPDATE players SET'), result: { rows: [{ coins: 125, trophies: 1, level: 1 }], rowCount: 1 } },
  ]);
  const app = createApp({ pool });
  const res = await call(app, 'POST', '/api/v1/battles', {
    headers: { 'X-Player-Id': '1' },
    body: {
      playerAnimalId: 'lion', opponentName: 'Shadow', opponentAnimalId: 'wolf',
      won: true, rewardCoins: 25, rewardTrophies: 1,
    },
  });
  assert.equal(res.status, 201);
  assert.equal(res.json.battleId, 9);
  assert.equal(res.json.player.coins, 125);
});

test('GET /battles/recent requires auth and returns rows', async () => {
  const pool = new StubPool([
    { match: (t) => t.includes('FROM battles'), result: { rows: [{ id: 1, won: true }], rowCount: 1 } },
  ]);
  const app = createApp({ pool });

  const noAuth = await call(app, 'GET', '/api/v1/battles/recent');
  assert.equal(noAuth.status, 401);

  const ok = await call(app, 'GET', '/api/v1/battles/recent', { headers: { 'X-Player-Id': '1' } });
  assert.equal(ok.status, 200);
  assert.equal(ok.json.length, 1);
});

test('GET /leaderboard/weekly returns ranked entries', async () => {
  const pool = new StubPool([
    {
      match: (t) => t.includes('weekly_leaderboard'),
      result: { rows: [{ rank: 1, player_id: 1, player_name: 'Ali', weekly_trophies: 3, wins: 3, total_battles: 4 }], rowCount: 1 },
    },
  ]);
  const app = createApp({ pool });
  const res = await call(app, 'GET', '/api/v1/leaderboard/weekly');
  assert.equal(res.status, 200);
  assert.equal(res.json[0].player_name, 'Ali');
});

test('GET /health reports ok', async () => {
  const app = createApp({ pool: new StubPool() });
  const res = await call(app, 'GET', '/api/v1/health');
  assert.equal(res.status, 200);
  assert.equal(res.json.status, 'ok');
});
