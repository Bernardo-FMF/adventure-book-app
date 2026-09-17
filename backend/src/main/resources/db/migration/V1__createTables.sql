CREATE TABLE book (
    id              BIGINT          GENERATED ALWAYS AS IDENTITY,
    slug            VARCHAR(120)    NOT NULL,
    title           VARCHAR(200)    NOT NULL,
    author          VARCHAR(120),
    difficulty      VARCHAR(10),
    genre           VARCHAR(40),
    description     TEXT,

    CONSTRAINT pk_book PRIMARY KEY (id),
    CONSTRAINT uq_slug UNIQUE (slug),
    CONSTRAINT ck_difficulty CHECK (difficulty IS NULL OR difficulty in ('EASY', 'MEDIUM', 'HARD')),
    CONSTRAINT ck_genre CHECK (genre IS NULL OR genre IN ('FANTASY', 'HIGH_FANTASY', 'ADVENTURE', 'STEAMPUNK_MYSTERY', 'MYSTERY'))
);

CREATE TABLE book_tag (
    book_id         BIGINT          NOT NULL,
    tag             VARCHAR(40)     NOT NULL,

    CONSTRAINT pk_tag PRIMARY KEY (book_id, tag),
    CONSTRAINT fk_book FOREIGN KEY (book_id) REFERENCES book(id) ON DELETE CASCADE
);

CREATE TABLE section (
    id              BIGINT          GENERATED ALWAYS AS IDENTITY,
    book_id         BIGINT          NOT NULL,
    section_ref     VARCHAR(40)     NOT NULL,
    type            VARCHAR(6)      NOT NULL,
    text            TEXT            NOT NULL,
    pos             INTEGER         NOT NULL,

    CONSTRAINT pk_section PRIMARY KEY (id),
    CONSTRAINT fk_book FOREIGN KEY (book_id) REFERENCES book(id) ON DELETE CASCADE,
    CONSTRAINT uq_book_section UNIQUE (book_id, section_ref),
    CONSTRAINT ck_section_type CHECK (type IN ('BEGIN', 'NODE', 'END'))
);

CREATE TABLE section_option (
    id              BIGINT          GENERATED ALWAYS AS IDENTITY,
    section_id      BIGINT          NOT NULL,
    pos             INTEGER         NOT NULL,
    description     TEXT            NOT NULL,
    goto            VARCHAR(40)     NOT NULL,

    CONSTRAINT pk_section_option PRIMARY KEY (id),
    CONSTRAINT fk_section FOREIGN KEY (section_id) REFERENCES section(id) ON DELETE CASCADE,
    CONSTRAINT uq_section_pos UNIQUE (section_id, pos)
);

CREATE TABLE consequence (
    option          BIGINT,
    type            VARCHAR(12)     NOT NULL,
    amount          INTEGER         NOT NULL,
    text            TEXT,

    CONSTRAINT pk_consequence PRIMARY KEY (option),
    CONSTRAINT fk_consequence FOREIGN KEY (option) REFERENCES section_option(id) ON DELETE CASCADE,
    CONSTRAINT ck_type CHECK (type IN ('LOSE_HEALTH', 'GAIN_HEALTH')),
    CONSTRAINT ck_amount CHECK (amount >= 0)
);