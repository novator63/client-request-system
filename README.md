# Client Request System

## Run Everything With Docker Compose

This repository includes a single `docker-compose.yml` that starts:
- PostgreSQL database (with persistent volume)
- Spring Boot backend (Flyway migrations run automatically on startup)
- Vue frontend (served by Nginx)

### 1. Prepare environment variables

Create a local `.env` file from the template:

```sh
cp .env.example .env
```

You can keep default values for local/demo runs, or edit them in `.env`.

### 2. Build and run

```sh
docker compose up --build -d
```

### 3. Open the app

- Frontend: http://localhost
- Backend API: http://localhost:8080/api
- PostgreSQL: localhost:5432

### 4. Stop services

```sh
docker compose down
```

To also remove database data:

```sh
docker compose down -v
```

## Notes

- DB schema and seed data are managed by Flyway scripts located in `backend/app/src/main/resources/db/migration`.
- Frontend API URL is set at build time via `VITE_API_BASE_URL` from `.env`.
- For production use, change `APP_JWT_SECRET` in `.env`.
