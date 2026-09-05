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

test('GET /battles/recent clamps negative limit', async () => {
  const pool = new StubPool([
    { match: (t) => t.includes('FROM battles'), result: { rows: [], rowCount: 0 } },
  ]);
  const app = createApp({ pool });
  const res = await call(app, 'GET', '/api/v1/battles/recent?limit=-5', { headers: { 'X-Player-Id': '1' } });
  assert.equal(res.status, 200);
  const callRow = pool.queryCalls.find((c) => c.text.includes('FROM battles'));
  assert.equal(callRow.params[1], 1);
});

test('POST /battles returns 404 for unknown player (FK violation)', async () => {
  const pool = new StubPool();
  pool.client.query = async (text) => {
    if (text === 'BEGIN' || text === 'ROLLBACK') return { rows: [], rowCount: 0 };
    const err = new Error('foreign key violation');
    err.code = '23503';
    throw err;
  };
  const app = createApp({ pool });
  const res = await call(app, 'POST', '/api/v1/battles', {
    headers: { 'X-Player-Id': '999' },
    body: { playerAnimalId: 'lion', opponentName: 'S', opponentAnimalId: 'wolf', won: true, rewardCoins: 25, rewardTrophies: 1 },
  });
  assert.equal(res.status, 404);
});

test('POST /battles is idempotent on clientBattleId (200 duplicate, no double rewards)', async () => {
  const pool = new StubPool([
    { match: (t) => t.includes('WHERE client_battle_id = $1'), result: { rows: [{ id: 7, created_at: new Date() }], rowCount: 1 } },
    { match: (t) => t.includes('SELECT coins, trophies, level FROM players'), result: { rows: [{ coins: 125, trophies: 6, level: 1 }], rowCount: 1 } },
  ]);
  const app = createApp({ pool });
  const res = await call(app, 'POST', '/api/v1/battles', {
    headers: { 'X-Player-Id': '1' },
    body: {
      playerAnimalId: 'lion', opponentName: 'Shadow', opponentAnimalId: 'wolf',
      won: true, rewardCoins: 25, rewardTrophies: 1, clientBattleId: 'abc-123',
    },
  });
  assert.equal(res.status, 200);
  assert.equal(res.json.duplicate, true);
  assert.ok(!pool.queryCalls.some((c) => c.text.includes('INSERT INTO battles')));
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

test('PUT /players/me/sync requires X-Player-Id', async () => {
  const app = createApp({ pool: new StubPool() });
  const res = await call(app, 'PUT', '/api/v1/players/me/sync', { body: { name: 'Test' } });
  assert.equal(res.status, 401);
});

test('PUT /players/me/sync updates and returns player', async () => {
  const pool = new StubPool([
    {
      match: (t) => t.includes('UPDATE players SET') && t.includes('daily_login_streak'),
      result: {
        rows: [{ id: 1, device_id: 'dev1', name: 'Ali', level: 5, xp: 30, coins: 500, trophies: 45 }],
        rowCount: 1,
      },
    },
  ]);
  const app = createApp({ pool });
  const res = await call(app, 'PUT', '/api/v1/players/me/sync', {
    headers: { 'X-Player-Id': '1' },
    body: {
      name: 'Ali', level: 5, xp: 30, coins: 500, trophies: 45,
      selectedAnimalId: 'tiger', unlockedAnimals: ['lion', 'tiger'],
      animalUpgrades: {}, dailyLoginStreak: 3, lastLoginDate: 0,
      luckyWheelSpinsToday: 0, lastSpinDate: 0,
      currentMapLevel: 4, completedLevels: [1, 2, 3],
    },
  });
  assert.equal(res.status, 200);
  assert.equal(res.json.level, 5);
  assert.equal(res.json.coins, 500);
});

test('PUT /players/me/sync returns 404 when player not found', async () => {
  const pool = new StubPool([
    {
      match: (t) => t.includes('UPDATE players SET'),
      result: { rows: [], rowCount: 0 },
    },
  ]);
  const app = createApp({ pool });
  const res = await call(app, 'PUT', '/api/v1/players/me/sync', {
    headers: { 'X-Player-Id': '999' },
    body: { name: 'Ghost' },
  });
  assert.equal(res.status, 404);
});

test('POST /matches/result requires X-Player-Id', async () => {
  const app = createApp({ pool: new StubPool() });
  const res = await call(app, 'POST', '/api/v1/matches/result', {
    body: { matchId: 'ABC123', winnerPlayerId: '1', nonce: 'nonce-very-long-004' },
  });
  assert.equal(res.status, 401);
});

test('POST /matches/result validates matchId', async () => {
  const app = createApp({ pool: new StubPool() });
  const res = await call(app, 'POST', '/api/v1/matches/result', {
    headers: { 'X-Player-Id': '1' },
    body: { matchId: 'AB', winnerPlayerId: '1', nonce: 'nonce-very-long-005' },
  });
  assert.equal(res.status, 400);
});

test('POST /matches/result records server-authoritative result', async () => {
  const pool = new StubPool([
    {
      match: (t) => t.includes('UPDATE players SET'),
      result: { rows: [{ id: 1, name: 'Ali', coins: 125, xp: 20, trophies: 1, level: 1 }], rowCount: 1 },
    },
    {
      match: (t) => t.includes('INSERT INTO match_records'),
      result: { rows: [], rowCount: 1 },
    },
  ]);
  const app = createApp({ pool });
  const res = await call(app, 'POST', '/api/v1/matches/result', {
    headers: { 'X-Player-Id': '1' },
    body: {
      matchId: 'ABC123', opponentId: '2', winnerPlayerId: '1',
      stats: { perfectAnswers: 2 }, nonce: 'nonce-very-long-006',
    },
  });
  assert.equal(res.status, 201);
  assert.equal(res.json.result, 'win');
  assert.equal(res.json.rewards.coins, 25);
  assert.equal(res.json.rewards.xp, 20);
});
