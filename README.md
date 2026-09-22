# Adventure book application

## Documentation

This file has the instructions on how to run the project.
The backend and frontend then each have its own README.md with some information regarding each project.

- [`backend/README.md`](backend/README.md)
- [`frontend/README.md`](frontend/README.md)

## How to run

There are 2 ways:

- Docker compose. The docker compose file starts everything automatically.
- Manually. Run the projects manually, though a PostgreSQL instance is always needed.

### Docker compose

Requires Docker with Compose installed on the machine.

```bash
docker compose up --build
```

Then open **http://localhost:8080**.

This will build the frontend and backend docker images, and start them alongside an instance of PostgreSQL.

Ports 5432 and 8080 will be used by default, so they need to be free before running the compose command.

To stop the services:

```bash
docker compose down
```

### Run manually

In terms of prerequisites, we need:

- JDK 26;
- Maven 3.9*;
- Node.js 24 with npm;
- PostgreSQL instance.

For the database you can either use your own instance, or start the database instance from the compose file:

```bash
docker compose up -d db
```

If you use your own instance, create a new database called "adventureBooks".

#### Backend

Then to start the backend:

- Navigate to `backend/`;
- Set the env variables, and run the specified maven command:

1. In bash.

```bash
export DB_URL=jdbc:postgresql://localhost:5432/adventureBooks
export DB_USER=user
export DB_PASSWORD=password
mvn spring-boot:run
```

2. In powershell.

```powershell
$env:DB_URL = "jdbc:postgresql://localhost:5432/adventureBooks"
$env:DB_USER = "user"
$env:DB_PASSWORD = "password"
mvn spring-boot:run
```

This will start the API on http://localhost:8081.

#### Frontend

Then to start the frontend:

- Navigate to `frontend/`;
- Run the commands:

```bash
npm ci
npm start
```

This will start the frontend on http://localhost:4200.