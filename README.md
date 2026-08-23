# S5_Cloud_Carte

A road issue reporting system split across three clients. Citizens report potholes and damaged roads from a mobile app with GPS coordinates and a photo; managers see those reports appear on a web map, triage them, and assign them to contractors. A Spring Boot API owns the durable data, and Firebase carries the real-time propagation between surfaces.

Built as a cloud computing project, the goal was to architect a system across three separate clients — a Spring Boot API, a React web dashboard, and an Ionic mobile app — and keep them in sync without tight coupling.

## Features

### What it does

- Mobile app (Ionic/Capacitor): submit a report with GPS coordinates, description, and photo; follow your own reports and their status
- Web dashboard (React): every report on an interactive map, filterable by status (nouveau, en cours, terminé), assignable to contractors
- Real-time propagation: a report filed on mobile surfaces on the web without polling
- Role-based access: regular users see only their own reports, managers see everything
- Full CRUD for the problems attached to a report, plus user and contractor administration
- Self-hosted map tiles, no external map API dependency
- One `docker compose up` brings up the API, PostgreSQL, the web frontend, and the tile server together

### How the pieces fit together

- **Postgres is the system of record; Firebase is the transport.** Firestore carries changes between clients in real time, but the backend keeps its own relational copy rather than treating Firestore as the database. Reports are relational data with foreign keys and reporting queries behind them, which is not what a document store is good at, and it means an outage in the sync layer costs live updates rather than the data itself.
- **Synced entities share one `Syncable` contract instead of one sync routine each.** Any entity mirroring a Firestore collection exposes `firebaseId`, `lastSync`, and its collection name, so a single generic method pulls a collection, upserts each document, and stamps it. Adding a synced entity means implementing the interface rather than writing another near-identical loop.
- **Reference data is reconciled by business name, not by primary key.** A Firestore document and its Postgres row are matched on `nom`, with the Firebase document id stored alongside rather than adopted as the key. Each system keeps its own identity scheme, which is what avoids Postgres inheriting Firestore's ids or the reverse.
- **Sync checks reachability first and gives up quietly.** A one-second reachability probe runs before any sync work, so an unreachable Firebase means "no updates this pass" rather than a stack trace on every scheduled run.
- **Map tiles are served locally from MBTiles rather than pulled from a tile API.** It removes a hard external dependency and any per-request cost or key management, and it works offline. The tradeoff is real and paid up front: the tile pipeline has to be built and the tile data shipped with the deployment.
- **The whole stack is one compose file.** API, database, web frontend, and tile server come up together, because a system whose interesting behaviour is the interaction between services is not usefully demonstrated by starting them one at a time.

## Screenshots

### Dashboard

![Dashboard showing key metrics and road work statistics](docs/screenshots/dashboard.png)

Live counts of reports, total surface coverage, allocated budget, and overall progress across every road work project.

### Map View

![Map view](docs/screenshots/Carte.png)

Every report plotted over Antananarivo, with layers filterable by status. The sidebar carries area statistics, surface, budget, and progress.

![Signalement marker without any issues and popup details](docs/screenshots/CarteSignalementWithoutProb.png)
![Signalement markers with an issue and popup details](docs/screenshots/CarteSignalementWithProb.png)

Clicking a marker opens its details: date, status, surface area, budget, and the assigned contractor. A report can exist before anyone has attached a specific problem to it, which is why the two states look different.

### Signal Management

![Signal management table with creation dates, statuses, and quick actions](docs/screenshots/GestSignalement.png)

The triage view. Managers move reports between statuses, attach problems, and remove records, all against the same data the map reads.

### Problems Management

![Problems table with associated signals and status updates](docs/screenshots/GestProblems.png)

Problems are separate records linked back to a report, so one location can accumulate several distinct issues over time rather than having its description overwritten.

### Users Management

![Users table with email, role assignment, and permission controls](docs/screenshots/GestUser.png)

Role assignment, account blocking, and the sync controls that govern contractor access.

## Tech Stack

- Backend: Java + Spring Boot
- Web frontend: React
- Mobile frontend: Ionic + Capacitor (Vue)
- Real-time sync: Firebase (Firestore)
- Database: PostgreSQL
- Deployment: Docker + Docker Compose
- Map tiles: MapTiler tile server over MBTiles

## Project Structure

```
S5_Cloud_Carte/
├── backend/              # Spring Boot API
├── frontend-web/         # React dashboard
├── frontend-mobile/      # Ionic/Vue mobile app
├── tiles/                # MBTiles map data
└── docker-compose.yml
```

## Getting Started

### Prerequisites

- Docker Desktop
- Node.js (only needed for mobile development)
- A Firebase project with Firestore enabled

### Run with Docker

```bash
git clone https://github.com/Snowlydial/S5_Cloud_Carte.git
cd S5_Cloud_Carte
```

Configure the schema in `./backend/sql/script_pg.sql` before the first launch, then:

```bash
docker compose up --build -d
```

- API: http://localhost:8080
- Web dashboard: http://localhost:3000
- Tile server: http://localhost:8081

### If you change the database schema

```bash
docker compose down -v
docker compose up --build -d
```

`-v` drops the volumes, which is what forces the schema script to run again on the next start.

### Mobile setup (Ionic)

```bash
cd frontend-mobile
npm install

npm install @capacitor/geolocation @ionic/pwa-elements
npx cap sync
```

On Android, add `ACCESS_FINE_LOCATION` to `AndroidManifest.xml` — reports are useless without coordinates, so the app has nothing to submit if the permission is missing.

## Academic context

Built during Semester 5 at IT University as a cloud computing project.
