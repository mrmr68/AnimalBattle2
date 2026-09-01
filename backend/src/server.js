'use strict';

const express = require('express');
const cors = require('cors');

const store = require('./store');
const { createPool } = require('./db/pool');

/**
 * Builds the Express app. `pool` is injectable for testing.
 *
 * Routes (all JSON, prefix /api/v1):
 *   POST /players            { deviceId, name? }        -> upsert + return player
 *   GET  /players/:deviceId                             -> fetch player
 *   PATCH /players/me        header X-Player-Id         -> update name / selectedAnimalId
 *   POST /battles            header X-Player-Id         -> record battle + rewards
 *   GET  /battles/recent     header X-Player-Id         -> recent battles
 *   GET  /leaderboard/weekly                            -> ranked entries
 *   GET  /health                                        -> liveness + db check
 */
function createApp({ pool } = {}) {
  const db = pool || createPool();

  const app = express();
  app.use(cors());
  app.use(express.json());

  // Wrap async handlers so rejections reach the error middleware.
  const wrap = (fn) => (req, res, next) => Promise.resolve(fn(req, res, next)).catch(next);

  function requirePlayerId(req, res, next) {
    const id = Number(req.header('X-Player-Id'));
    if (!Number.isInteger(id) || id <= 0) {
      return res.status(401).json({ error: 'Missing or invalid X-Player-Id header' });
    }
    req.playerId = id;
    return next();
  }

  app.get('/api/v1/health', wrap(async (_req, res) => {
    await db.query('SELECT 1');
    res.json({ status: 'ok', service: 'animal-battle-backend', time: new Date().toISOString() });
  }));

  app.post('/api/v1/players', wrap(async (req, res) => {
    const { deviceId, name } = req.body || {};
    if (typeof deviceId !== 'string' || deviceId.length === 0 || deviceId.length > 128) {
      return res.status(400).json({ error: 'deviceId is required (max 128 chars)' });
    }
    const player = await store.upsertPlayer(db, { deviceId, name });
    res.status(player ? 200 : 500).json(player);
  }));

  app.get('/api/v1/players/:deviceId', wrap(async (req, res) => {
    const player = await store.getPlayerByDeviceId(db, req.params.deviceId);
    if (!player) return res.status(404).json({ error: 'Player not found' });
    res.json(player);
  }));

  app.patch('/api/v1/players/me', requirePlayerId, wrap(async (req, res) => {
    const { name, selectedAnimalId } = req.body || {};
    if (name !== undefined && (typeof name !== 'string' || name.length === 0 || name.length > 64)) {
      return res.status(400).json({ error: 'name must be 1-64 chars' });
    }
    if (selectedAnimalId !== undefined && (typeof selectedAnimalId !== 'string' || selectedAnimalId.length === 0 || selectedAnimalId.length > 64)) {
      return res.status(400).json({ error: 'selectedAnimalId must be 1-64 chars' });
    }
    const player = await store.updatePlayerProfile(db, req.playerId, { name, selectedAnimalId });
    if (!player) return res.status(404).json({ error: 'Player not found' });
    res.json(player);
  }));

  app.post('/api/v1/battles', requirePlayerId, wrap(async (req, res) => {
    const b = req.body || {};
    const errors = [];
    for (const key of ['playerAnimalId', 'opponentName', 'opponentAnimalId']) {
      if (typeof b[key] !== 'string' || b[key].length === 0 || b[key].length > 64) {
        errors.push(`${key} is required (max 64 chars)`);
      }
    }
    if (typeof b.won !== 'boolean') errors.push('won must be a boolean');
    const rewardCoins = Number(b.rewardCoins ?? 0);
    const rewardTrophies = Number(b.rewardTrophies ?? 0);
    if (!Number.isInteger(rewardCoins) || rewardCoins < 0 || rewardCoins > 1000) {
      errors.push('rewardCoins must be an integer 0-1000');
    }
    if (!Number.isInteger(rewardTrophies) || rewardTrophies < 0 || rewardTrophies > 100) {
      errors.push('rewardTrophies must be an integer 0-100');
    }
    if (errors.length > 0) return res.status(400).json({ error: errors.join('; ') });

    const result = await store.recordBattle(db, {
      playerId: req.playerId,
      playerAnimalId: b.playerAnimalId,
      opponentName: b.opponentName,
      opponentAnimalId: b.opponentAnimalId,
      won: b.won,
      rewardCoins,
      rewardTrophies,
    });
    res.status(201).json(result);
  }));

  app.get('/api/v1/battles/recent', requirePlayerId, wrap(async (req, res) => {
    const limit = Number(req.query.limit ?? 20);
    const battles = await store.getRecentBattles(db, req.playerId, Number.isInteger(limit) ? limit : 20);
    res.json(battles);
  }));

  app.get('/api/v1/leaderboard/weekly', wrap(async (req, res) => {
    const limit = Number(req.query.limit ?? 100);
    const entries = await store.getWeeklyLeaderboard(db, Number.isInteger(limit) ? limit : 100);
    res.json(entries);
  }));

  // 404 for unknown API routes
  app.use('/api', (_req, res) => res.status(404).json({ error: 'Not found' }));

  // Central error handler
  // eslint-disable-next-line no-unused-vars
  app.use((err, _req, res, _next) => {
    console.error('[error]', err.message);
    res.status(500).json({ error: 'Internal server error' });
  });

  return app;
}

module.exports = { createApp };

if (require.main === module) {
  const pool = createPool();
  const app = createApp({ pool });
  const port = Number(process.env.PORT || 3000);
  app.listen(port, '0.0.0.0', () => {
    console.log(`animal-battle-backend listening on 0.0.0.0:${port}`);
  });
}
