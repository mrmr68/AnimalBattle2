# Animal Battle — Backend (Node.js + PostgreSQL)

Game services for the Animal Battle Android client: player profiles, battle
records, and the weekly leaderboard.

## Stack

- Node.js (>= 18), Express
- PostgreSQL (single `DATABASE_URL`)
- No ORM — plain parameterized SQL in `src/store.js`

## Setup

```bash
cd backend
npm install
export DATABASE_URL=postgresql://user:pass@host:5432/animalbattle
npm run migrate   # creates tables + weekly_leaderboard materialized view
npm start         # listens on 0.0.0.0:${PORT:-3000}
```

Required environment variable:
- `DATABASE_URL` — PostgreSQL connection string
  (e.g. `postgresql://user:pass@host:5432/animalbattle`)

Never commit real secrets.

## Schema (`migrations/001_init.sql`)

- `players` — one row per device (`device_id` unique): name, level, xp,
  coins, trophies, selected/unlocked animals, upgrades.
- `battles` — one row per finished battle; win rewards are applied in the
  same transaction that inserts the record.
- `weekly_leaderboard` — materialized view aggregating this week's trophies
  and wins; refreshed (concurrently) on leaderboard reads.

## API (all JSON, prefix `/api/v1`)

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/players` | — | `{deviceId, name?}` → upsert player, returns profile |
| GET | `/players/:deviceId` | — | fetch profile |
| PATCH | `/players/me` | `X-Player-Id` | update `name` / `selectedAnimalId` |
| POST | `/battles` | `X-Player-Id` | record battle `{playerAnimalId, opponentName, opponentAnimalId, won, rewardCoins, rewardTrophies}` → applies rewards atomically |
| GET | `/battles/recent?limit=` | `X-Player-Id` | recent battles (max 50) |
| GET | `/leaderboard/weekly?limit=` | — | ranked weekly entries (max 100) |
| GET | `/health` | — | liveness + DB check |

Reward bounds are validated server-side (`rewardCoins ≤ 1000`,
`rewardTrophies ≤ 100`); the client's canonical win reward is
`25 coins + 1 trophy`.

## Tests

```bash
npm test
```

Unit tests run against an in-memory stub pool (no PostgreSQL needed):
data-access semantics (reward application, limits, ranking), every HTTP
endpoint (validation, auth, happy paths), and the migration runner.
