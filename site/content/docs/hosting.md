---
title: Hosting & Installation
description: Run your own Sanctum server with Docker or a direct build.
nav_label: Hosting
order: 3
---

## Overview

Sanctum has two parts: the **server**, which you host, and the **Android app**, which connects to it. Sanctum is :term[self-hosted]{key="self-hosted"}, so the server runs on a machine you control. It is made of three pieces:

- **the Sanctum server**: a Ktor (Kotlin) application that serves the API the app talks to
- **a PostgreSQL database**: books, authors, series, tags and read history
- **a storage folder**: the files you upload, ebook files and covers

Every user on a server has an :term[isolated library]{key="isolated-library"}.

## Requirements

| | Docker (recommended) | Direct build |
|---|---|---|
| Runtime | Docker with Compose | JDK 21 |
| Database | included (PostgreSQL 16) | PostgreSQL 16, installed separately |
| Ports | 7489/TCP (API) <br/> 7490/UDP (LAN discovery) | same |
| Storage | two :term[Docker volumes]{key="docker-volume"} (database, files) | a folder on disk |
| Disk space | depends on the size of your library (number of ebook files and covers) | same |

## Installation

::::tabs
:::tab{label="Docker"}
1. Clone the repository and open the server folder:

   ```bash
   git clone https://github.com/jovanni0/CrystalCage.git
   cd CrystalCage/server
   ```

2. Create your configuration file:

   ```bash
   cp .env.example .env
   ```

3. Edit `.env` and change at least these values (see [Configuration](#configuration)):

   - `JWT_SECRET`: generate one with `openssl rand -base64 48`
   - `SEED_ADMIN_PASSWORD`: the password of the first admin account
   - `POSTGRES_PASSWORD`: the database password

4. Start the server:

   ```bash
   docker compose up -d
   ```

5. Check that it is running:

   ```bash
   docker compose logs -f app
   ```

   You should see a line like `server trying to listen for API on port 7489, with discovery on port 7490`.
:::
:::tab{label="Direct build"}
1. Start a PostgreSQL 16 database. The simplest way is still a container:

   ```bash
   docker run --name sanctum-db \
     -e POSTGRES_USER=postgres \
     -e POSTGRES_PASSWORD=<database-password> \
     -e POSTGRES_DB=sanctum \
     -v ~/sanctum-db/:/var/lib/postgresql/data \
     -p 127.0.0.1:5432:5432 \
     -d postgres:16
   ```

2. Clone the repository and build the server:

   ```bash
   git clone https://github.com/jovanni0/CrystalCage.git
   cd CrystalCage/server
   ./gradlew shadowJar
   ```

3. Set the :term[environment variables]{key="environment-variable"} from [Configuration](#configuration). At minimum: `DB_JDBC_URL`, `POSTGRES_USER`, `POSTGRES_PASSWORD`, `JWT_SECRET`, `STORAGE_BASE_DIR` and `SEED_ADMIN_PASSWORD`.

4. Run it:

   ```bash
   java -jar build/libs/app-all.jar
   ```

To start it automatically after a reboot, run it as a systemd service.
:::
::::

## Configuration

All settings are environment variables. With Docker, they are read from `.env`.

| Variable | Default | Required | Description |
|---|---|---|---|
| **Server** | | | |
| `PORT` | `7489` | no | Port of the API. |
| `DISCOVERY_PORT` | `7490` | no | UDP port for LAN discovery. The app always looks on 7490, so changing it turns discovery off. |
| `SERVER_NAME` | `Sanctum Server` | no | Name shown in the app when it finds your server. |
| `SERVER_HTTPS` | `false` | no | Whether LAN discovery should point the app to `https://`. See [HTTPS](#https-and-reverse-proxy). |
| **Database** | | | |
| `DB_JDBC_URL` | `jdbc:postgresql://localhost:5432/sanctum` | yes | Connection URL. With Docker: `jdbc:postgresql://db:5432/sanctum`. |
| `POSTGRES_USER` | `postgres` | yes | Database user. |
| `POSTGRES_PASSWORD` | `postgres` | **change it** | Database password. |
| `POSTGRES_DB` | `sanctum` | Docker only | Name of the database the container creates. |
| **Authentication** | | | |
| `JWT_SECRET` | placeholder | **change it** | Secret used to sign login tokens. Anyone who knows it can log in as any user. |
| `JWT_ISSUER`, `JWT_AUDIENCE`, `JWT_REALM` | `sanctum-server`, `sanctum-client`, `sanctum` | no | Token metadata. Leave them as they are. |
| **Storage** | | | |
| `STORAGE_BASE_DIR` | `./sanctum-fs` | yes | Folder for uploaded ebook files and covers. With Docker it must be inside the mounted volume, `/app/sanctum-fs`. |
| **First run** | | | |
| `SEED_ADMIN_PASSWORD` | placeholder | **change it** | Password of the `admin` account, created on the first start. |

:::warning
Change `JWT_SECRET`, `POSTGRES_PASSWORD` and `SEED_ADMIN_PASSWORD` before the server is reachable from outside your network.
:::

## First run

On its first start, the server creates an **admin account**:

- username: `admin`
- password: the value of `SEED_ADMIN_PASSWORD`

The account is created only once. Changing `SEED_ADMIN_PASSWORD` later does not change the password of an existing admin, you will need to change it in the app.

### Adding users

Sanctum is invite-only. The admin generates an :term[invite code]{key="invite-code"} in the app and gives it to the new user, who enters it when creating an account. Each code works once and does not expire. Passwords must be at least 4 characters long.

## Connecting the app

When you open the app for the first time, it looks for a server.

**On the same network**, the app uses :term[LAN discovery]{key="lan-discovery"}: it sends a broadcast on UDP port 7490 and lists every Sanctum server that answers, by its `SERVER_NAME`. Pick yours.

**Anywhere else**, enter the address manually. Include the port unless a reverse proxy serves Sanctum on the standard ports:

- `http://192.168.1.20:7489` for a server on your network
- `https://books.example.com` for a server behind a reverse proxy

You can leave out `http://` or `https://`, the app tries them both.

:::note
LAN discovery relies on broadcast packets, which Docker's default network does not forward to containers. If the app does not find a server running in Docker, enter its address manually.
:::

## HTTPS and reverse proxy

To reach your server from the internet, put it behind a :term[reverse proxy]{key="reverse-proxy"} that handles HTTPS (Caddy, nginx, Traefik, or a platform such as Dokploy) and forward requests to port `7489`. In the app, enter your domain manually, for example `https://books.example.com`.

`SERVER_HTTPS` only affects LAN discovery. A discovered server is opened as `http://<server-ip>:7489`, or `https://<server-ip>:7489` when `SERVER_HTTPS=true`. Sanctum itself serves plain HTTP on that port, so leave `SERVER_HTTPS=false` unless HTTPS is served on that same address and port.

:::warning
Do not expose the database port (`5432`) to the internet. With Docker, the server reaches the database through Docker's internal network and the port does not need to be published.
:::

## Data, backups and updates

Your data lives in two places, and a backup needs both:

| What | Docker | Direct build |
|---|---|---|
| Database | volume `db_data` | your PostgreSQL data folder |
| Files (ebooks, covers) | volume `sanctum_fs` | the `STORAGE_BASE_DIR` folder |

Back up the database:

```bash
docker compose exec db pg_dump -U postgres sanctum > sanctum-backup.sql
```

Back up the files by copying the contents of `STORAGE_BASE_DIR`, or of the `sanctum_fs` volume with Docker.

To update a Docker install:

```bash
git pull
docker compose up -d --build
```

Volumes are kept between rebuilds, so your data stays.

## Troubleshooting

**Covers or ebook files disappear after a rebuild.**
`STORAGE_BASE_DIR` is not inside the mounted volume, so the files were written inside the container and were lost with it. With Docker, use `/app/sanctum-fs`.

**The app finds the server but cannot log in (`SSL_PROTOCOL_ERROR`).**
`SERVER_HTTPS=true`, but the server answers plain HTTP on its port. Set it to `false`, or enter the HTTPS address manually.

**The app does not find the server on the network.**
Check that the phone is on the same network, that UDP port 7490 is open and that `DISCOVERY_PORT` is 7490. With Docker, enter the address manually.

**The server stops right after starting.**
It cannot reach the database. Check `DB_JDBC_URL` and the credentials, and look at the database logs (`docker compose logs db`).
