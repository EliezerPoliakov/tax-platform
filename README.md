# Tax Platform

A small but realistic financial integration platform built to demonstrate production-oriented Java engineering, deterministic document processing, multi-tenant security, and bounded agentic automation for integration incidents and repair work.

Tax Platform is inspired by a limited subset of the problems found in systems such as Rubixtax. It is not a tutorial CRUD application and is not intended to reproduce a complete commercial tax product.

## Current Status

- **Project phase:** Documentation approved before application development
- **Current milestone:** Version 0.1 — Minimal Platform and Deterministic Integration
- **Milestone status:** Planned
- **Application implementation:** Not started
- **Documentation status:** Updated and approved on 2026-08-05

No backend, frontend, parser, agent service, cloud deployment, Kafka integration, Redis integration, or production infrastructure is claimed as implemented yet.

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

The Python agent service is not part of Version 0.1. It is introduced in Version 0.2 immediately after the Version 0.1 structured failure path exists.

## Milestone Overview

| Version | Goal | Status |
|---|---|---|
| 0.1 | Minimal platform and deterministic integration | Planned |
| 0.2 | Support Investigation Agent | Planned later |
| 0.3 | Integration Repair Agent | Planned later |
| 0.4 | Asynchronous and event-driven processing with Kafka | Planned later |
| 0.5 | Cloud document storage and AWS deployment | Planned later |
| 0.6 | Reliability and operational maturity, including Redis where justified | Planned later |
| 0.7 | Additional synthetic integrations and format variants | Planned later |

See [ROADMAP.md](ROADMAP.md) for completion criteria and dependencies.

## Target Technology Status

| Technology or capability | Status |
|---|---|
| Java 21, Spring Boot, React, TypeScript, PostgreSQL, Flyway, Spring Security | Approved for Version 0.1 |
| Docker Compose for PostgreSQL | Approved for Version 0.1 |
| Deterministic parser, persistent processing job, structured incident | Approved for Version 0.1 |
| Python agent service and LLM tool calling | Planned for Version 0.2 |
| Spring AI MCP tools exposed by Java | Planned for Version 0.2 |
| Persistent agent runs, tracing, approvals, and evaluations | Planned for Version 0.2 and expanded in Version 0.3 |
| Integration Repair Agent and draft pull requests | Planned for Version 0.3 |
| Kafka, outbox, retries, DLQ, event versioning | Planned for Version 0.4 after asynchronous boundaries exist |
| Amazon S3 and AWS deployment | Planned for Version 0.5 after the local flow is stable |
| Redis | Planned only when a demonstrated session, cache, rate-limit, or lock need exists |
| Kubernetes | Optional future evolution after multiple independently deployable services exist |

## Planned Repository Structure

```text
tax-platform/
├── backend/                         # Java/Spring Boot source of truth
├── frontend/                        # React/TypeScript user interface
├── agent-service/                   # Python service, added in Version 0.2
├── integration-samples/             # Synthetic files and structural fixtures
├── docs/
│   ├── AGENTIC_INCIDENT_FLOW.md
│   ├── SECURITY_AND_DATA_BOUNDARIES.md
│   └── EVALUATION_STRATEGY.md
├── docker-compose.yml
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
- Every company-scoped request and every Java/MCP tool call rechecks tenant membership.
- Python does not receive direct access to Java-owned business tables.
- Raw financial files and row values are not sent to an external LLM.
- Only synthetic financial data is used in the portfolio demonstration.
- Tool schemas are narrow, typed, authorized, and auditable.
- State-changing or repair operations require explicit permissions and human approval.
- Model output is a proposal, not a financial or engineering source of truth.

See [docs/SECURITY_AND_DATA_BOUNDARIES.md](docs/SECURITY_AND_DATA_BOUNDARIES.md).

## Running Locally

Application startup commands do not exist yet because application code has not been generated. Verified commands will be added only after the relevant components are implemented.

The first local infrastructure dependency will be PostgreSQL started with Docker Compose.

## Testing and Evaluation Direction

The project will use:

- backend unit, integration, persistence, security, and API tests;
- frontend type checking and production builds;
- parser fixtures and regression tests;
- tenant-isolation and structured-error tests;
- Support Agent and Repair Agent evaluation suites;
- assertions for expected and forbidden tool calls;
- trace, latency, token, and estimated-cost measurements;
- CI checks before a change can be considered complete.

See [docs/EVALUATION_STRATEGY.md](docs/EVALUATION_STRATEGY.md).

## Documentation

- [PROJECT.md](PROJECT.md) — stable vision, goals, boundaries, and definition of success.
- [ROADMAP.md](ROADMAP.md) — ordered milestones, dependencies, completion criteria, and demonstrable outcomes.
- [ARCHITECTURE.md](ARCHITECTURE.md) — current approved architecture and explicit future evolution.
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
