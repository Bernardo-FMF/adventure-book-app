CREATE TABLE game_session
(
    id                  UUID        NOT NULL,
    book_id             BIGINT      NOT NULL,
    section_id          BIGINT      NOT NULL,
    health              INTEGER     NOT NULL,
    status              VARCHAR(12) NOT NULL,
    last_consequence_id BIGINT,

    CONSTRAINT pk_game_session PRIMARY KEY (id),
    CONSTRAINT fk_game_book FOREIGN KEY (book_id) REFERENCES book (id) ON DELETE CASCADE,
    CONSTRAINT fk_game_section FOREIGN KEY (section_id) REFERENCES section (id) ON DELETE CASCADE,
    CONSTRAINT fk_game_last_consequence FOREIGN KEY (last_consequence_id) REFERENCES consequence (option) ON DELETE SET NULL,
    CONSTRAINT ck_game_status CHECK (status IN ('IN_PROGRESS', 'DEAD', 'FINISHED')),
    CONSTRAINT ck_game_health CHECK (health >= 0)
);
