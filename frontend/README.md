# Frontend

## Commands

```bash
npm ci
npm start     # dev server on http://localhost:4200
npm test
npm run build
```

`npm start` proxies `/api` to the backend through `proxy.conf.js`, which defaults to `http://localhost:8081`. Point it
elsewhere with `BACKEND_URL`:

```bash
BACKEND_URL=http://localhost:9000 npm start
```

## Directory structure

```
core/api      typed HTTP clients and response models
core/state    keeps state regarding identity and the games in progress
ui            standalone components
books         main page, displays books and allows searching and filtering; An identified user may start a game
game          game session page, displays the current game state, and allows the user to make choices to progress
adventurer    page to set the user identity
layout        layout that displays the user's identity
```

## Testing

```bash
npm test
```
