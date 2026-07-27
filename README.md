# Tax Platform

A small but realistic financial platform built to demonstrate production-oriented software engineering, modern architecture, and a complete end-to-end business workflow.

The project is intentionally developed in small, working iterations. Technologies are introduced only when they solve a concrete business or engineering problem.

## Current Status

- **Project phase:** Initial planning and setup
- **Current milestone:** Version 0.1 — Identity and Company Workspace
- **Milestone status:** Planned
- **Implementation status:** Not started

The first milestone has been approved and documented. Application code, local startup commands, and deployment instructions do not exist yet and will be added as implementation progresses.

## Product Direction

The target business workflow is:

1. A user signs in.
2. The user selects a company.
3. The user uploads a financial document.
4. The document is stored and processed.
5. The user reviews and edits the extracted data.
6. The system generates a final report.

Version 0.1 establishes the identity and company workspace required by that workflow.

## Version 0.1 Scope

Version 0.1 will allow a user to:

- register with an email address and password;
- sign in and sign out using a server-side session;
- create a company;
- view the companies to which the user belongs;
- access company data only when the user is a company member.

The initial implementation will include:

- a Java 21 and Spring Boot backend;
- a React and TypeScript frontend;
- PostgreSQL running locally through Docker Compose;
- Flyway database migrations;
- Spring Security with session-based authentication;
- automated backend tests;
- frontend build verification;
- continuous integration.

Document upload, Python processing, AWS, S3, Redis, Kafka, and Kubernetes are intentionally outside Version 0.1.

## Planned Repository Structure

```text
tax-platform/
├── backend/
├── frontend/
├── docs/
├── docker-compose.yml
├── README.md
├── PROJECT.md
├── ROADMAP.md
├── ARCHITECTURE.md
└── DECISIONS.md
```

A separately deployable Python document-processing service will be introduced later, when the document-processing business capability is implemented.

## Architecture Overview

The approved Version 0.1 architecture is:

```text
React Frontend
      |
      | HTTP / REST
      v
Java Spring Boot Application
      |
      | JPA / JDBC
      v
PostgreSQL
```

The Java application will begin as one deployable application with explicit internal business modules. Specialized services will be added incrementally rather than created as empty microservices in advance.

See [ARCHITECTURE.md](ARCHITECTURE.md) for details.

## Running Locally

Local startup instructions will be added after the backend, frontend, and Docker Compose configuration are created.

Until then, any startup command would be speculative and should not be treated as project documentation.

## Testing

Testing commands and the test strategy will be documented as soon as the initial applications are generated.

Version 0.1 is expected to include:

- unit tests for business rules;
- integration tests for persistence and security-sensitive flows;
- API tests for authentication and company access;
- frontend build verification;
- CI execution on every pull request or push to the main development branch.

## Documentation

- [PROJECT.md](PROJECT.md) — project vision, principles, boundaries, and definition of success.
- [ROADMAP.md](ROADMAP.md) — current milestone, scope, implementation sequence, and completion criteria.
- [ARCHITECTURE.md](ARCHITECTURE.md) — approved system structure and planned evolution.
- [DECISIONS.md](DECISIONS.md) — architectural decisions and the reasoning behind them.

## Project Principles

The project follows several core rules:

- keep every completed iteration demonstrable;
- prefer one complete workflow over many unfinished features;
- introduce technology only to solve a real problem;
- keep current implementation separate from future plans;
- document important architectural trade-offs;
- use AI as an engineering assistant, not as a substitute for understanding.

See [PROJECT.md](PROJECT.md) for the complete project charter.
