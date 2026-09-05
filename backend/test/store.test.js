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
  assert.equal(result.duplicate, false);
});

test('recordBattle is idempotent on clientBattleId (no double rewards)', async () => {
  const pool = new StubPool([
    // Duplicate check finds an existing row for this clientBattleId
    { match: (t) => t.includes('WHERE client_battle_id = $1'), result: { rows: [{ id: 7, created_at: new Date() }], rowCount: 1 } },
    // Current player state read for the echo-back
    { match: (t) => t.includes('SELECT coins, trophies, level FROM players'), result: { rows: [{ coins: 125, trophies: 6, level: 1 }], rowCount: 1 } },
  ]);

  const result = await store.recordBattle(pool, {
    playerId: 1,
    playerAnimalId: 'lion',
    opponentName: 'Shadow',
    opponentAnimalId: 'wolf',
    won: true,
    rewardCoins: 25,
    rewardTrophies: 1,
    clientBattleId: 'abc-123',
  });

  assert.equal(result.duplicate, true);
  assert.equal(result.battleId, 7);
  // No INSERT INTO battles / UPDATE players reward query ran
  assert.ok(!pool.queryCalls.some((c) => c.text.includes('INSERT INTO battles')));
  assert.ok(!pool.queryCalls.some((c) => c.text.includes('UPDATE players SET')));
});

test('recordBattle stores clientBattleId on insert', async () => {
  const pool = new StubPool([
    { match: (t) => t.includes('INSERT INTO battles'), result: { rows: [{ id: 12, created_at: new Date() }], rowCount: 1 } },
    { match: (t) => t.includes('UPDATE players SET'), result: { rows: [{ coins: 125, trophies: 6, level: 1 }], rowCount: 1 } },
  ]);

  await store.recordBattle(pool, {
    playerId: 1, playerAnimalId: 'lion', opponentName: 'S', opponentAnimalId: 'wolf',
    won: true, rewardCoins: 25, rewardTrophies: 1, clientBattleId: 'uuid-1',
  });

  const insert = pool.client.queries.find((c) => c.text.includes('INSERT INTO battles'));
  assert.equal(insert.params[7], 'uuid-1');
});

test('recordBattle maps FK violation to 404', async () => {
  const pool = new StubPool();
  // StubClient.query returns {rows:[]} for everything — simulate FK error by monkey-patching
  pool.client.query = async (text) => {
    if (text === 'BEGIN') return { rows: [], rowCount: 0 };
    if (text === 'ROLLBACK') return { rows: [], rowCount: 0 };
    const err = new Error('insert or update on table "battles" violates foreign key');
    err.code = '23503';
    throw err;
  };

  await assert.rejects(
    () => store.recordBattle(pool, { playerId: 999, playerAnimalId: 'lion', opponentName: 'S', opponentAnimalId: 'wolf', won: true, rewardCoins: 25, rewardTrophies: 1 }),
    (err) => err.statusCode === 404
  );
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

  store._resetLeaderboardRefreshForTests();
  const entries = await store.getWeeklyLeaderboard(pool, 10, { forceRefresh: true });
  assert.equal(entries[0].player_name, 'Blaze');
  assert.equal(entries[0].rank, 1);
  assert.equal(entries.length, 2);
  // View refreshed before select
  assert.ok(pool.queryCalls[0].text.includes('REFRESH MATERIALIZED VIEW'));
});

// ── syncPlayerState ────────────────────────────────────────

test('syncPlayerState updates player and returns updated row', async () => {
  const pool = new StubPool([
    {
      match: (t) => t.includes('UPDATE players SET') && t.includes('daily_login_streak'),
      result: {
        rows: [{
          id: 1, device_id: 'dev1', name: 'Ali', level: 5, xp: 30, coins: 500, trophies: 45,
          selected_animal_id: 'tiger', unlocked_animals: ['lion', 'tiger'],
          animal_upgrades: { tiger: 2 },
          daily_login_streak: 3, last_login_date: 1700000000000,
          lucky_wheel_spins_today: 1, last_spin_date: 1700000000000,
          current_map_level: 4, completed_levels: [1, 2, 3],
          language: 'en', sound_enabled: true, music_enabled: true, notifications_enabled: true,
        }],
        rowCount: 1,
      },
    },
  ]);

  const result = await store.syncPlayerState(pool, 1, {
    name: 'Ali', level: 5, xp: 30, coins: 500, trophies: 45,
    selectedAnimalId: 'tiger', unlockedAnimals: ['lion', 'tiger'],
    animalUpgrades: { tiger: 2 },
    dailyLoginStreak: 3, lastLoginDate: 1700000000000,
    luckyWheelSpinsToday: 1, lastSpinDate: 1700000000000,
    currentMapLevel: 4, completedLevels: [1, 2, 3],
    language: 'en', soundEnabled: true, musicEnabled: true, notificationsEnabled: true,
  });

  assert.ok(result);
  assert.equal(result.id, 1);
  assert.equal(result.name, 'Ali');
  assert.equal(result.level, 5);
  assert.equal(result.coins, 500);
  assert.equal(result.trophies, 45);
});
