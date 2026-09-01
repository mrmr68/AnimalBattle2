'use strict';

/**
 * Data-access layer. Every function takes an injected `pool` so unit tests
 * can run against a stubbed pool without a live PostgreSQL instance.
 */

const LEADERBOARD_MAX_ENTRIES = 100;
const RECENT_BATTLES_MAX = 50;

async function upsertPlayer(pool, { deviceId, name }) {
  const { rows } = await pool.query(
    `INSERT INTO players (device_id, name)
     VALUES ($1, $2)
     ON CONFLICT (device_id)
     DO UPDATE SET name = EXCLUDED.name, updated_at = now()
     RETURNING id, device_id, name, level, xp, coins, trophies,
               selected_animal_id, unlocked_animals, animal_upgrades`,
    [deviceId, name || 'Player']
  );
  return rows[0];
}

async function getPlayerByDeviceId(pool, deviceId) {
  const { rows } = await pool.query(
    `SELECT id, device_id, name, level, xp, coins, trophies,
            selected_animal_id, unlocked_animals, animal_upgrades
     FROM players WHERE device_id = $1`,
    [deviceId]
  );
  return rows[0] || null;
}

async function updatePlayerProfile(pool, playerId, { name, selectedAnimalId }) {
  const { rows } = await pool.query(
    `UPDATE players SET
       name = COALESCE($2, name),
       selected_animal_id = COALESCE($3, selected_animal_id),
       updated_at = now()
     WHERE id = $1
     RETURNING id, device_id, name, level, xp, coins, trophies,
               selected_animal_id, unlocked_animals, animal_upgrades`,
    [playerId, name || null, selectedAnimalId || null]
  );
  return rows[0] || null;
}

/**
 * Records a finished battle and applies its rewards atomically.
 * Rewards follow the game rules: win = +25 coins, +1 trophy (passed in,
 * validated server-side to stay positive and bounded).
 */
async function recordBattle(pool, { playerId, playerAnimalId, opponentName, opponentAnimalId, won, rewardCoins, rewardTrophies }) {
  const client = await pool.connect();
  try {
    await client.query('BEGIN');

    const battleResult = await client.query(
      `INSERT INTO battles
         (player_id, player_animal_id, opponent_name, opponent_animal_id, won, reward_coins, reward_trophies)
       VALUES ($1, $2, $3, $4, $5, $6, $7)
       RETURNING id, created_at`,
      [playerId, playerAnimalId, opponentName, opponentAnimalId, won, rewardCoins, rewardTrophies]
    );

    const coinDelta = won ? rewardCoins : 0;
    const trophyDelta = won ? rewardTrophies : 0;

    const playerResult = await client.query(
      `UPDATE players SET
         coins = coins + $2,
         trophies = trophies + $3,
         level = GREATEST(1, (trophies + $3) / 10 + 1),
         updated_at = now()
       WHERE id = $1
       RETURNING coins, trophies, level`,
      [playerId, coinDelta, trophyDelta]
    );

    await client.query('COMMIT');
    return {
      battleId: battleResult.rows[0].id,
      createdAt: battleResult.rows[0].created_at,
      player: playerResult.rows[0],
    };
  } catch (err) {
    await client.query('ROLLBACK');
    throw err;
  } finally {
    client.release();
  }
}

async function getRecentBattles(pool, playerId, limit = RECENT_BATTLES_MAX) {
  const { rows } = await pool.query(
    `SELECT id, player_animal_id, opponent_name, opponent_animal_id,
            won, reward_coins, reward_trophies, created_at
     FROM battles
     WHERE player_id = $1
     ORDER BY created_at DESC
     LIMIT $2`,
    [playerId, Math.min(limit, RECENT_BATTLES_MAX)]
  );
  return rows;
}

/**
 * Weekly leaderboard from the materialized view, refreshed on read.
 * Returns ranked entries; ties broken by earlier join order (player_id).
 */
async function getWeeklyLeaderboard(pool, limit = LEADERBOARD_MAX_ENTRIES) {
  await pool.query('REFRESH MATERIALIZED VIEW CONCURRENTLY weekly_leaderboard');
  const { rows } = await pool.query(
    `WITH ranked AS (
       SELECT player_id, player_name, weekly_trophies, wins, total_battles,
              DENSE_RANK() OVER (ORDER BY weekly_trophies DESC, wins DESC) AS rank
       FROM weekly_leaderboard
       WHERE weekly_trophies > 0 OR wins > 0
     )
     SELECT * FROM ranked ORDER BY rank, player_id LIMIT $1`,
    [Math.min(limit, LEADERBOARD_MAX_ENTRIES)]
  );
  return rows;
}

module.exports = {
  LEADERBOARD_MAX_ENTRIES,
  RECENT_BATTLES_MAX,
  upsertPlayer,
  getPlayerByDeviceId,
  updatePlayerProfile,
  recordBattle,
  getRecentBattles,
  getWeeklyLeaderboard,
};
