-- Online 1v1: server-authoritative match results.
-- Each accepted result is recorded once per (match_id, nonce) so replay
-- attacks cannot double-award rewards.

CREATE TABLE IF NOT EXISTS match_records (
    id               BIGSERIAL PRIMARY KEY,
    match_id         TEXT        NOT NULL,
    player_id        BIGINT      NOT NULL REFERENCES players(id) ON DELETE CASCADE,
    opponent_id      BIGINT,
    winner_player_id BIGINT,
    nonce            TEXT        NOT NULL,
    stats_json       JSONB       NOT NULL DEFAULT '{}'::jsonb,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (match_id, nonce)
);

CREATE INDEX IF NOT EXISTS idx_match_records_player
    ON match_records (player_id, created_at DESC);