CREATE TABLE album_entities (
    code VARCHAR(3) PRIMARY KEY,
    name TEXT NOT NULL,
    type TEXT NOT NULL,

    CONSTRAINT chk_album_entity_type
        CHECK (type IN ('FIFA_WORLD_CUP', 'NATIONAL_TEAM', 'COCA_COLA')),

    CONSTRAINT chk_album_entity_code
        CHECK (
            CASE
                WHEN type = 'COCA_COLA' THEN code = 'CC'
                WHEN type = 'FIFA_WORLD_CUP' THEN code = 'FWC'
                WHEN type = 'NATIONAL_TEAM' THEN code ~ '^[A-Z]{3}$'
            END
        )
);

CREATE TABLE world_cup_national_teams(
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    national_team_code VARCHAR(3) NOT NULL UNIQUE,
    group_letter CHAR(1) NOT NULL,
    group_slot INTEGER NOT NULL,

    CONSTRAINT fk_national_team_code
        FOREIGN KEY (national_team_code)
        REFERENCES album_entities (code),

    CONSTRAINT chk_national_teams_code
        CHECK (national_team_code <> 'FWC' AND national_team_code <> 'CC'),

    CONSTRAINT chk_national_teams_group_letter
        CHECK (group_letter ~ '^[A-L]{1}$'),

    CONSTRAINT chk_national_teams_group_slot
        CHECK (group_slot BETWEEN 1 AND 4),

    CONSTRAINT uq_world_cup_group_slot
        UNIQUE (group_letter, group_slot)
);

CREATE TABLE regional_teams (
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name TEXT NOT NULL,
    code VARCHAR(3),

    CONSTRAINT chk_regional_teams_code
        CHECK (code IS NULL OR code ~ '^[A-Z]{2,3}$')
);

CREATE TABLE players (
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    full_name TEXT NOT NULL,
    birth_date DATE,
    height_cm INTEGER,
    weight_kg INTEGER,

    regional_team_id INTEGER,
    national_team_code VARCHAR(3) NOT NULL,

    CONSTRAINT fk_players_regional_teams
        FOREIGN KEY (regional_team_id)
        REFERENCES regional_teams (id)
        ON DELETE SET NULL,

    CONSTRAINT fk_players_national_teams
        FOREIGN KEY (national_team_code)
        REFERENCES world_cup_national_teams (national_team_code),

    CONSTRAINT chk_players_height_cm
        CHECK (height_cm IS NULL OR height_cm > 0),

    CONSTRAINT chk_players_weight_kg
        CHECK (weight_kg IS NULL OR weight_kg > 0)
);

CREATE TABLE stickers (
    code VARCHAR(7) PRIMARY KEY,
    sticker_prefix_code VARCHAR(3) NOT NULL,
    sticker_number INTEGER NOT NULL,
    sticker_type TEXT NOT NULL,
    player_id INTEGER,
    player_type TEXT,
    sticker_rarity TEXT NOT NULL,
    stock_available INTEGER NOT NULL DEFAULT 0,

    CONSTRAINT fk_stickers_category
        FOREIGN KEY (sticker_prefix_code)
        REFERENCES album_entities (code),

    CONSTRAINT fk_stickers_players
        FOREIGN KEY (player_id)
        REFERENCES players (id),

    CONSTRAINT chk_stickers_numbers
        CHECK (sticker_number > 0),

    CONSTRAINT chk_stickers_codes
        CHECK (
            CASE
                WHEN sticker_rarity = 'BASE'
                    THEN code = sticker_prefix_code || '-' || sticker_number::TEXT
                ELSE
                    code = sticker_prefix_code || '-' || sticker_number::TEXT || left(sticker_rarity, 1)
            END
        ),

    CONSTRAINT uq_stickers_team_number_rarity
        UNIQUE (sticker_prefix_code, sticker_number, sticker_rarity),

    CONSTRAINT chk_stickers_players_type
        CHECK(player_type IS NULL OR player_type IN ('STANDARD', 'LEGEND')),

    CONSTRAINT chk_stickers_rarity
        CHECK (sticker_rarity IN ('BASE', 'PURPLE', 'BRONZE', 'SILVER', 'GOLD')),

    CONSTRAINT chk_stickers_type
        CHECK (sticker_type IN ('PLAYER', 'BADGE', 'TEAM_PHOTO')),

    CONSTRAINT chk_stock_available_not_negative
        CHECK (stock_available >= 0),

    CONSTRAINT chk_players_type_for_sticker_rarity
        CHECK (player_type IS NULL OR player_type <> 'STANDARD' OR sticker_rarity = 'BASE'),

    CONSTRAINT chk_players_fields_by_sticker_type
        CHECK (
            (sticker_type = 'PLAYER' AND player_id IS NOT NULL AND player_type IS NOT NULL)
            OR
            (sticker_type <> 'PLAYER' AND player_id IS NULL AND player_type IS NULL AND sticker_rarity = 'BASE')
        )
);