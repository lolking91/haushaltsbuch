# Haushaltsbuch

A personal budgeting application built with Spring Boot and SvelteKit.

## Stack

- **Backend:** Spring Boot 4 / Java 26 — includes the built SvelteKit frontend as static resources
- **Frontend:** SvelteKit, TypeScript, Tailwind CSS v4
- **Database:** PostgreSQL 17 (Docker)

## Running in production

```bash
docker compose up -d
```

The backend listens on `127.0.0.1:8080`. Routing to the outside is handled by
the central **nginx reverse proxy in the `infra` repository** (`../infra`).
This repo no longer contains an nginx service or configuration — both are managed
centrally there.

## Local development build

To test the production Docker image without a release:

```bash
docker compose -f docker-compose.yml -f docker-compose.local.yml up --build
```

## Environment variables

| Variable      | Description              |
|---------------|--------------------------|
| `DB_USER`     | PostgreSQL username       |
| `DB_PASSWORD` | PostgreSQL password       |
