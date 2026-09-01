-- Animal Battle schema (PostgreSQL)
-- Players: one row per registered device/player
-- Battles: one row per finished battle (used for Recent Battles + stats)

CREATE TABLE IF NOT EXISTS players (
    id            BIGSERIAL PRIMARY KEY,
    device_id     TEXT        NOT NULL UNIQUE,
    name          TEXT        NOT NULL DEFAULT 'Player',
    level         INT         NOT NULL DEFAULT 1 CHECK (level >= 1),
    xp            INT         NOT NULL DEFAULT 0 CHECK (xp >= 0),
    coins         INT         NOT NULL DEFAULT 100 CHECK (coins >= 0),
    trophies      INT         NOT NULL DEFAULT 0 CHECK (trophies >= 0),
    selected_animal_id TEXT   NOT NULL DEFAULT 'lion',
    unlocked_animals   TEXT[] NOT NULL DEFAULT ARRAY['lion'],
    animal_upgrades    JSONB  NOT NULL DEFAULT '{}'::jsonb,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS battles (
    id                 BIGSERIAL PRIMARY KEY,
    player_id          BIGINT  NOT NULL REFERENCES players(id) ON DELETE CASCADE,
    player_animal_id   TEXT    NOT NULL,
    opponent_name      TEXT    NOT NULL,
    opponent_animal_id TEXT    NOT NULL,
    won                BOOLEAN NOT NULL,
    reward_coins       INT     NOT NULL DEFAULT 0,
    reward_trophies    INT     NOT NULL DEFAULT 0,
    created_at         TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_battles_player ON battles (player_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_players_trophies ON players (trophies DESC);

-- Leaderboard: weekly window over battle wins/trophies
CREATE MATERIALIZED VIEW IF NOT EXISTS weekly_leaderboard AS
SELECT
    p.id                    AS player_id,
    p.name                  AS player_name,
    COALESCE(SUM(CASE WHEN b.won THEN b.reward_trophies ELSE 0 END), 0) AS weekly_trophies,
    COUNT(*) FILTER (WHERE b.won) AS wins,
    COUNT(*)                     AS total_battles
FROM players p
LEFT JOIN battles b
    ON b.player_id = p.id
   AND b.created_at >= date_trunc('week', now())
GROUP BY p.id, p.name;

CREATE UNIQUE INDEX IF NOT EXISTS idx_weekly_leaderboard_player
    ON weekly_leaderboard (player_id);
