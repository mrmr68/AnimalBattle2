'use strict';

const test = require('node:test');
const assert = require('node:assert');

const store = require('../src/store');
const { StubPool } = require('./helpers');

test('upsertPlayer inserts and returns mapped row', async () => {
  const pool = new StubPool([
    {
      match: (t) => t.includes('INSERT INTO players'),
      result: {
        rows: [{ id: 1, device_id: 'dev1', name: 'Ali', level: 1, xp: 0, coins: 100, trophies: 0, selected_animal_id: 'lion', unlocked_animals: ['lion'], animal_upgrades: {} }],
        rowCount: 1,
      },
    },
  ]);

  const player = await store.upsertPlayer(pool, { deviceId: 'dev1', name: 'Ali' });
  assert.equal(player.id, 1);
  assert.equal(player.name, 'Ali');
  assert.equal(player.coins, 100);
});

test('getPlayerByDeviceId returns null when not found', async () => {
  const pool = new StubPool();
  const player = await store.getPlayerByDeviceId(pool, 'missing');
  assert.equal(player, null);
});

test('recordBattle applies rewards only on win', async () => {
  const pool = new StubPool([
    {
      match: (t) => t.includes('INSERT INTO battles'),
      result: { rows: [{ id: 10, created_at: new Date() }], rowCount: 1 },
    },
    {
      match: (t) => t.includes('UPDATE players SET'),
      result: (params) => ({ rows: [{ coins: 100 + params[1], trophies: 5 + params[2], level: Math.floor((5 + params[2]) / 10) + 1 }], rowCount: 1 }),
    },
  ]);

  const result = await store.recordBattle(pool, {
    playerId: 1,
    playerAnimalId: 'lion',
    opponentName: 'Shadow',
    opponentAnimalId: 'wolf',
    won: true,
    rewardCoins: 25,
    rewardTrophies: 1,
  });

  assert.equal(result.battleId, 10);
  assert.equal(result.player.coins, 125);
  assert.equal(result.player.trophies, 6);
  assert.equal(result.player.level, 1); // 6/10 + 1
});

test('recordBattle defeat grants zero rewards', async () => {
  const pool = new StubPool([
    { match: (t) => t.includes('INSERT INTO battles'), result: { rows: [{ id: 11, created_at: new Date() }], rowCount: 1 } },
    { match: (t) => t.includes('UPDATE players SET'), result: (params) => ({ rows: [{ coins: 100 + params[1], trophies: 5 + params[2], level: 1 }], rowCount: 1 }) },
  ]);

  const result = await store.recordBattle(pool, {
    playerId: 1,
    playerAnimalId: 'tiger',
    opponentName: 'Blaze',
    opponentAnimalId: 'bear',
    won: false,
    rewardCoins: 25,
    rewardTrophies: 1,
  });

  assert.equal(result.player.coins, 100);
  assert.equal(result.player.trophies, 5);
});

test('getRecentBattles caps limit at 50', async () => {
  const pool = new StubPool();
  await store.getRecentBattles(pool, 1, 500);
  const call = pool.queryCalls.find((c) => c.text.includes('FROM battles'));
  assert.equal(call.params[1], 50);
});

test('getWeeklyLeaderboard refreshes view and ranks entries', async () => {
  const pool = new StubPool([
    {
      match: (t) => t.includes('weekly_leaderboard'),
      result: {
        rows: [
          { rank: 1, player_id: 2, player_name: 'Blaze', weekly_trophies: 12, wins: 12, total_battles: 15 },
          { rank: 2, player_id: 1, player_name: 'Ali', weekly_trophies: 5, wins: 5, total_battles: 8 },
        ],
        rowCount: 2,
      },
    },
  ]);

  const entries = await store.getWeeklyLeaderboard(pool, 10);
  assert.equal(entries[0].player_name, 'Blaze');
  assert.equal(entries[0].rank, 1);
  assert.equal(entries.length, 2);
  // View refreshed before select
  assert.ok(pool.queryCalls[0].text.includes('REFRESH MATERIALIZED VIEW'));
});
