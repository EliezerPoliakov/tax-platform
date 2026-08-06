# Tax Platform

A small but realistic financial integration platform built to demonstrate production-oriented Java engineering, deterministic document processing, multi-tenant security, and bounded agentic automation for integration incidents and repair work.

Tax Platform is inspired by a limited subset of the problems found in systems such as Rubixtax. It is not a tutorial CRUD application and is not intended to reproduce a complete commercial tax product.

## Current Status

- **Project phase:** Active local implementation
- **Current milestone:** Version 0.1 — Minimal Platform and Deterministic Integration
- **Milestone status:** In Progress
- **Application implementation:** In Progress
- **Last verified:** 2026-08-06

The repository now contains a working Java backend, React frontend, PostgreSQL local environment, Flyway migrations, user persistence, a complete registration flow, a fully functional session-based authentication lifecycle (login, logout, current-user retrieval), company persistence, explicit membership, and tenant-isolated company API with a corresponding frontend selection flow. All changes are protected by CSRF and verified by integration tests.

Version 0.1 is not complete. Document processing, deterministic parsing, structured incidents, CI, and the remaining frontend workflow are still planned for this milestone.

## Implemented Now

The following capabilities exist and have been verified locally:

- monorepo with independently buildable `backend/` and `frontend/` applications;
- Java 21 Spring Boot backend with Maven Wrapper;
- React, TypeScript, and Vite frontend;
- PostgreSQL 17 through Docker Compose;
- environment-based local database configuration;
- Flyway as the authoritative schema-migration mechanism;
- `V1__baseline.sql`, `V2__create_users.sql`, and `V3__create_companies.sql` migrations;
- `users`, `companies`, and `company_members` persistence models;
- Spring Data JPA repository integration against PostgreSQL;
- public registration API with validation and duplicate-email conflict handling;
- password hashing through Spring Security using the `{bcrypt}` format;
- session-based authentication with `POST /api/auth/login`, `POST /api/auth/logout`, and `GET /api/auth/me`;
- authenticated company API with `POST /api/companies`, `GET /api/companies`, and `GET /api/companies/{id}`;
- tenant isolation enforced in Java through membership-scoped repository queries;
- Spring Security authenticated server-side sessions with HTTP-only cookies;
- CSRF protection for all state-changing requests, including authentication and company creation;
- React registration, login, and company management forms with session restoration on application start;
- authenticated-user display, company selection, and logout capability in the frontend;
- integration tests for persistence, uniqueness, registration, login/logout lifecycle, company creation, transactional rollback, and tenant isolation;
- frontend lint and production-build verification;
- manual verification of browser registration, login, and session persistence.

The following are not implemented yet:

- document upload and storage abstraction;
- processing jobs, deterministic parser, canonical output, incidents, or structural profiles;
- Python agents, MCP, Kafka, Redis, AWS, S3, or Kubernetes;
- repository CI workflow.

## Product Direction

The platform has two deliberately separate concerns.

### Deterministic Financial Core

The ordinary financial workflow does not depend on an LLM:

1. A user registers and signs in.
2. The user selects or creates a company.
3. The user uploads a synthetic financial integration file.
4. The platform stores the file and creates a persistent processing job.
5. The Java backend selects an integration type and parser version.
6. A deterministic parser produces a canonical internal representation.
7. Technical validation rules run.
8. The user receives either a successful result or a structured error and incident.

Financial calculations, accounting rules, tax logic, report approval, and persistence of financial values remain deterministic and human-governed.

### Agentic Incident and Repair Flows

LLM agents are introduced only after the deterministic platform can produce a structured integration failure.

```text
Integration processing failure
        |
        v
Support Investigation Agent
        |
        v
Classification, evidence, guidance, retry decision, or escalation
        |
        v
Engineering ticket when code change is required
        |
        v
Human approval
        |
        v
Integration Repair Agent
        |
        v
Synthetic reproduction, patch, regression tests, draft pull request
        |
        v
Human code review
```

The agents do not receive raw customer financial files, do not access the production database directly, do not merge code, and do not deploy applications.

## Current Milestone

Version 0.1 delivers one complete local vertical scenario:

- Java 21 and Spring Boot backend;
- React and TypeScript frontend;
- PostgreSQL through Docker Compose;
- Flyway migrations;
- Spring Security with server-side sessions and CSRF protection;
- user registration and login;
- company membership and tenant isolation;
- one synthetic integration format;
- document upload through a storage abstraction;
- persistent processing jobs;
- one deterministic parser;
- canonical normalized output;
- success and failure results;
- a structured integration incident and structural file profile;
- automated tests and CI checks.

Registration, PostgreSQL, Flyway, password hashing, CSRF integration, and the initial React form are implemented. The remaining items are still milestone scope rather than current capabilities.

The Python agent service is not part of Version 0.1. It is introduced in Version 0.2 immediately after the Version 0.1 structured failure path exists.

## Milestone Overview

| Version | Goal | Status |
|---|---|---|
| 0.1 | Minimal platform and deterministic integration | In Progress |
| 0.2 | Support Investigation Agent | Planned later |
| 0.3 | Integration Repair Agent | Planned later |
| 0.4 | Asynchronous and event-driven processing with Kafka | Planned later |
| 0.5 | Cloud document storage and AWS deployment | Planned later |
| 0.6 | Reliability and operational maturity, including Redis where justified | Planned later |
| 0.7 | Additional synthetic integrations and format variants | Planned later |

See [ROADMAP.md](ROADMAP.md) for completion criteria and dependencies.

## Technology Status

| Technology or capability | Status |
|---|---|
| Java 21, Spring Boot, React, TypeScript | Implemented now |
| PostgreSQL through Docker Compose | Implemented now |
| Flyway migrations | Implemented now |
| User persistence and browser registration | Implemented now |
| Spring Security password hashing and CSRF protection | Implemented now |
| Login, logout, current user, authenticated server session | Implemented now |
| Company membership and tenant isolation | Implemented now |
| Deterministic parser, persistent processing job, structured incident | Approved for Version 0.1 |
| Repository CI workflow | Approved for Version 0.1; not implemented yet |
| Python agent service and LLM tool calling | Planned for Version 0.2 |
| Spring AI MCP tools exposed by Java | Planned for Version 0.2 |
| Persistent agent runs, tracing, approvals, and evaluations | Planned for Version 0.2 and expanded in Version 0.3 |
| Integration Repair Agent and draft pull requests | Planned for Version 0.3 |
| Kafka, outbox, retries, DLQ, event versioning | Planned for Version 0.4 after asynchronous boundaries exist |
| Amazon S3 and AWS deployment | Planned for Version 0.5 after the local flow is stable |
| Redis | Planned only when a demonstrated session, cache, rate-limit, or lock need exists |
| Kubernetes | Optional future evolution after multiple independently deployable services exist |

## Repository Structure

Current and planned top-level structure:

```text
tax-platform/
├── backend/                         # implemented Java/Spring Boot application
├── frontend/                        # implemented React/TypeScript application
├── agent-service/                   # added in Version 0.2
├── integration-samples/             # added with deterministic parser work
├── docs/                            # detailed architecture and security documents
├── docker-compose.yml               # implemented PostgreSQL local infrastructure
├── .env.example                     # implemented safe local configuration example
├── AGENTS.md
├── CONTRIBUTING.md
├── README.md
├── PROJECT.md
├── ROADMAP.md
├── ARCHITECTURE.md
└── DECISIONS.md
```

The repository is a monorepo, but each deployable application must remain independently buildable and must not bypass service boundaries through shared database access.

## Security Position

- Java is the authorization boundary and business source of truth.
- Every future company-scoped request and every Java/MCP tool call must recheck tenant membership.
- Passwords are persisted only as adaptive hashes, never plaintext.
- State-changing browser requests remain protected by CSRF.
- Python will not receive direct access to Java-owned business tables.
- Raw financial files and row values will not be sent to an external LLM.
- Only synthetic financial data is used in the portfolio demonstration.
- Tool schemas will be narrow, typed, authorized, and auditable.
- State-changing or repair operations require explicit permissions and human approval.
- Model output is a proposal, not a financial or engineering source of truth.

See [docs/SECURITY_AND_DATA_BOUNDARIES.md](docs/SECURITY_AND_DATA_BOUNDARIES.md).

## Running Locally

### Prerequisites

- Java 21;
- Node.js and npm;
- Docker with Docker Compose;
- Git.

### Start PostgreSQL

From the repository root:

```bash
cp .env.example .env   # first setup only; keep .env out of Git
docker compose up -d postgres
docker compose ps
```

### Start the backend

```bash
cd backend
./mvnw spring-boot:run
```

The backend listens on `http://localhost:8080` by default.

### Start the frontend

In a second terminal:

```bash
cd frontend
npm install            # first setup or after dependency changes
npm run dev
```

Open the URL printed by Vite. During local development, requests under `/api` are proxied to the Spring Boot backend.

### Stop local infrastructure

```bash
docker compose down
```

The named PostgreSQL volume is retained. Use `docker compose down -v` only when local database data should be deleted intentionally.

## Testing

Start PostgreSQL before backend integration tests:

```bash
docker compose up -d postgres
```

Backend tests:

```bash
cd backend
./mvnw test
```

Frontend verification:

```bash
cd frontend
npm run lint
npm run build
```

Current tests cover user persistence, the PostgreSQL email uniqueness constraint, registration, password hashing, duplicate registration, CSRF rejection, company creation, transactional rollback, and tenant isolation. Parser, agent, and evaluation tests will be added with their corresponding milestones.

## Documentation

- [PROJECT.md](PROJECT.md) — stable vision, goals, boundaries, and definition of success.
- [ROADMAP.md](ROADMAP.md) — ordered milestones, dependencies, completion criteria, and current implementation progress.
- [ARCHITECTURE.md](ARCHITECTURE.md) — implemented architecture and explicit future evolution.
- [DECISIONS.md](DECISIONS.md) — architecture decision records and supersession history.
- [AGENTS.md](AGENTS.md) — rules for coding agents working in this repository.
- [CONTRIBUTING.md](CONTRIBUTING.md) — issue-driven development and review workflow.
- [docs/AGENTIC_INCIDENT_FLOW.md](docs/AGENTIC_INCIDENT_FLOW.md) — detailed support and repair lifecycle.
- [docs/SECURITY_AND_DATA_BOUNDARIES.md](docs/SECURITY_AND_DATA_BOUNDARIES.md) — trust boundaries and sensitive-data controls.
- [docs/EVALUATION_STRATEGY.md](docs/EVALUATION_STRATEGY.md) — formal agent evaluation plan.

## Core Principles

- Keep the financial core deterministic.
- Use agents for variable operational investigation and bounded repair assistance.
- Deliver a demonstrable vertical scenario after every milestone.
- Introduce technology only when it solves a demonstrated problem.
- Keep implemented, approved, planned, and optional capabilities clearly separated.
- Preserve tenant isolation and human control even when AI is involved.
- Prefer small, reviewable changes with tests and meaningful history.
