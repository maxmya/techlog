# maxmya // bench

A personal build log and project host. One place for open source work, release
history, landing pages and wiki links. Java backend, Angular frontend, one
Docker image.

## What's here

| Path | Purpose |
| --- | --- |
| `src/main/java/io/maxmya/bench` | Spring Boot API, content store, SPA fallback |
| `src/main/resources/content/site.json` | Seed content: profile, projects, versions, log |
| `frontend/` | Angular 22 application |
| `Dockerfile` | Multi-stage image: Node build → Maven build → small JRE runtime |
| `.github/workflows/deploy.yml` | Build, push to GHCR, deploy on push to `main` |
| `deploy.ps1` | Local Docker build + SSH deploy |
| `BRAND.md` | Visual identity rules |

## Run locally

```powershell
# frontend bundle first (Maven picks it up automatically)
npm --prefix frontend ci
npm --prefix frontend run build

# API + static site on :8080
.\mvnw.cmd spring-boot:run
```

Or the whole thing in Docker:

```powershell
docker build -t maxmya/bench:local .
docker run --rm -p 8080:8080 -v bench-data:/app/data maxmya/bench:local
```

Dev server for the frontend alone: `npm --prefix frontend start` (expects the
API on the same origin; API calls will 404 unless the backend is running).

## Content model

`site.json` contains:

- `profile` — handle, bio, links
- `projects[]` — name, tagline, status, language, repo/wiki/demo URLs, tags,
  `landing.blocks` (text / features / quote / install / links),
  `versions[]` and `updates[]`
- `updates[]` — site-wide log entries

At startup the seed is copied to `BENCH_DATA_FILE` (default
`/app/data/site.json`) if that file doesn't exist. Edits made through the admin
API live on the mounted volume and survive redeploys. To reset, delete the file
in the volume and restart the container.

The projects page also lists every **public GitHub repository** for the configured
user, fetched live from `/users/{user}/repos` and cached for 30 minutes. Repos with
a matching project file are marked `FILE`. Private repos are never listed. Optional
environment variables: `GITHUB_USER` (default `maxmya`), `GITHUB_TOKEN` (raises the
API rate limit), `GITHUB_CACHE_MINUTES` (default `30`).

### Public API

| Method | Path |
| --- | --- |
| GET | `/api/health` |
| GET | `/api/site` |
| GET | `/api/projects` |
| GET | `/api/projects/{slug}` |
| GET | `/api/updates` |
| GET | `/api/github/repos` |

### Admin API

Set `ADMIN_TOKEN` on the container, then send `X-Admin-Token`.

| Method | Path | Body |
| --- | --- | --- |
| PUT | `/api/admin/content` | whole `site.json` |
| PUT | `/api/admin/projects/{slug}` | one project |
| POST | `/api/admin/projects/{slug}/versions` | one version |
| DELETE | `/api/admin/projects/{slug}` | — |

Example — add a release without rebuilding:

```powershell
$body = @{
  tag = 'v0.8.0'; date = '2026-10-01'; title = 'New shelf layout'
  notes = 'Reworked the update list.'; changelogUrl = 'https://github.com/maxmya/appshelf/releases'
  downloadUrl = 'https://github.com/maxmya/appshelf/releases/tag/v0.8.0'; current = $true
} | ConvertTo-Json

Invoke-RestMethod -Method Post -Uri 'http://maxmya.com/api/admin/projects/appshelf/versions' `
  -Headers @{ 'X-Admin-Token' = $env:ADMIN_TOKEN } -ContentType 'application/json' -Body $body
```

## Deployment

### GitHub Actions (automatic)

On push to `main`: build Angular → verify Maven build → build image → push to
`ghcr.io/maxmya/techlog:latest` → SSH to the droplet → pull and restart the
container on port 80 with the `bench-data` volume.

Repository secrets:

| Secret | Value |
| --- | --- |
| `SERVER_HOST` | `maxmya.com` (or the droplet IP) |
| `SERVER_USER` | `root` |
| `SERVER_SSH_KEY` | private key authorized on the server |
| `ADMIN_TOKEN` | long random string for the admin API |

Workflow permissions must allow `packages: write` (already declared in the
workflow; the repo may need "Read and write permissions" under Actions → General).

### PowerShell (manual)

Requires Docker Desktop, OpenSSH and `scp` on PATH.

```powershell
.\deploy.ps1
.\deploy.ps1 -Server 164.92.190.246 -SshKey "$env:USERPROFILE\.ssh\id_ed25519" -AdminToken "..."
.\deploy.ps1 -SkipBuild   # reuse the existing local image
```

The script builds locally, saves the image to a tar, copies it over SSH, loads
it on the server, replaces the `bench` container and waits for the health check.

## Server prerequisites

Docker Engine installed, port 80 free, and the SSH key from GitHub Actions
authorized for the deploy user. The container owns port 80; anything else bound
there must be stopped first.
