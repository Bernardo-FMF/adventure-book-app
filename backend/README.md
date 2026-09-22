# Backend

## Configuration

All of it comes from the environment, with no defaults for the database so a misconfiguration fails loudly:

| Variable        | Default                 | Purpose                                                                 |
|-----------------|-------------------------|-------------------------------------------------------------------------|
| `DB_URL`        | —                       | JDBC URL, for example `jdbc:postgresql://localhost:5432/adventureBooks` |
| `DB_USER`       | —                       | Database user                                                           |
| `DB_PASSWORD`   | —                       | Database password                                                       |
| `PORT`          | `8081`                  | HTTP port                                                               |
| `SEED_LOCATION` | `classpath:data/*.json` | Where books are imported from at startup                                |

The schema is created and migrated by Flyway from `src/main/resources/db/migration`. 
Hibernate never generates DDL.

## API

| Method | Path                                     | Identified | Description                                                                   |
|--------|------------------------------------------|------------|-------------------------------------------------------------------------------|
| `GET`  | `/api/books`                             | no         | Paginated list, with search and filters by genre and difficulty               |
| `GET`  | `/api/books/metadata`                    | no         | Book count and the genre/difficulty values actually in use, for the filter UI |
| `POST` | `/api/games`                             | yes        | Start a game for a book                                                       |
| `GET`  | `/api/games`                             | yes        | The caller's games in progress                                                |
| `GET`  | `/api/games/{gameId}`                    | yes        | Current state of one game                                                     |
| `POST` | `/api/games/{gameId}/choices/{optionId}` | yes        | Choose one of the options for the current section                             |

## Decisions

**Gameplay is server-driven.** The alternative was to send the entire book to the client, and let the client walk
through it.
Had it been client-driven, when a user tried to save, the server would have no way of knowing if the state is correct,
so it would have to trust client input. This could introduce corrupted saves, where values were modified without
following
the story.
A bonus aspect of this approach is that the optional objective 4 becomes rather simple to implement since the current
game state was already stored in a database, so the only necessary changes was the introduction of the player concept.

**Save button**. The design provided included a save button, which assumes that the application would be client-driven.
Given the above explanation as to why I went with server-driven, a save button wouldn't make much sense because every
choice
made is already persisted so trying to save would just rewrite the database row with the exact same data.

**The username is a stub for authentication.** The client sends the header `Authorization: Bearer <username>`, and the
`HandlerMethodArgumentResolver` turns it into a `PlayerEntity`. In this resolver, we create the user the first time
a name is seen.
There is no password and no verification, meaning anybody can claim any name. This is deliberate, since real
authentication
didn't seem to be within the scope of the assignment.

## Books and validation

Books are imported from JSON at startup and skipped if a book with the same slug already exists. A book is rejected
unless it has exactly one beginning, at least one ending, every option pointing at a declared section, and options on
every non-ending section. These rules were extracted from the description of the assignment.

`src/main/resources/data` contains the four books that were supplied. Given the constraints above, none of them are
valid,
therefore I added extra books, that fix the errors each of the supplied books have.

| Supplied file           | Rejected with                      | What is wrong                                                                                                                |
|-------------------------|------------------------------------|------------------------------------------------------------------------------------------------------------------------------|
| `crystal-caverns.json`  | `DEAD_END_NODE`                    | Section `666` is not an ending, yet offers no options, so a reader who reaches it is stuck                                   |
| `dragon-quest.json`     | `UNPARSEABLE`                      | The file is empty, so there is nothing to validate                                                                           |
| `pirates-jade-sea.json` | `UNRESOLVED_GOTO`, `DEAD_END_NODE` | Section `1` offers an option leading to section `999`, which doesn't exist; section `666` is node that leaves users stranded |
| `the-prisoner.json`     | `DEAD_END_NODE`                    | Section `666` is not an ending, yet offers no options                                                                        |
