-- Idempotent battle recording: clients send a unique battleId (UUID) so
-- network retries cannot double-apply rewards.

ALTER TABLE battles ADD COLUMN IF NOT EXISTS client_battle_id TEXT;
CREATE UNIQUE INDEX IF NOT EXISTS idx_battles_client_id
    ON battles (client_battle_id) WHERE client_battle_id IS NOT NULL;
