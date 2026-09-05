-- Cloud save: extend players table with full game state columns
-- so player progress survives app reinstalls and works across devices.

ALTER TABLE players
    ADD COLUMN IF NOT EXISTS daily_login_streak   INT  NOT NULL DEFAULT 0 CHECK (daily_login_streak >= 0),
    ADD COLUMN IF NOT EXISTS last_login_date      BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS lucky_wheel_spins_today INT NOT NULL DEFAULT 0 CHECK (lucky_wheel_spins_today >= 0),
    ADD COLUMN IF NOT EXISTS last_spin_date       BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS current_map_level    INT  NOT NULL DEFAULT 1 CHECK (current_map_level >= 1),
    ADD COLUMN IF NOT EXISTS completed_levels     INT[] NOT NULL DEFAULT ARRAY[]::INT[],
    ADD COLUMN IF NOT EXISTS language             TEXT NOT NULL DEFAULT 'en',
    ADD COLUMN IF NOT EXISTS sound_enabled        BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS music_enabled        BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS notifications_enabled BOOLEAN NOT NULL DEFAULT TRUE;
