# CrystalCage Server

requires a PostgrSQL database to be running on port `5432`. it's recommended to use a Docker container:
```bash
docker run --name crystalcage \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -e POSTGRES_DB=crystalcage \
  -v ~/crystalcage-db/:/var/lib/postgresql/data \
  -p 5432:5432 \
  -d postgres:16
```