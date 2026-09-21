CREATE TABLE player
(
    id       BIGINT GENERATED ALWAYS AS IDENTITY,
    username VARCHAR(80) NOT NULL,

    CONSTRAINT pk_player PRIMARY KEY (id)
);

CREATE UNIQUE INDEX uq_player_username ON player (username);

ALTER TABLE game_session
    ADD COLUMN player_id BIGINT,
    ADD CONSTRAINT fk_game_player FOREIGN KEY (player_id) REFERENCES player (id) ON DELETE CASCADE;

CREATE UNIQUE INDEX uq_active_game_per_book ON game_session (player_id, book_id) WHERE status = 'IN_PROGRESS';
