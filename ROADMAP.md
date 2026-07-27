# Tax Platform Roadmap

## 1. Document Purpose

This document defines the current implementation plan for the Tax Platform.

Unlike `PROJECT.md`, which describes the stable vision and principles of the project, this file is expected to change as milestones are completed, deferred, split, or reordered.

## 2. Current Status

- **Current milestone:** Version 0.1 — Identity and Company Workspace
- **Status:** Planned
- **Implementation:** Not started
- **Last updated:** 2026-07-27

## 3. Milestone Statuses

The roadmap uses the following statuses:

- **Planned** — approved but implementation has not started.
- **In Progress** — active implementation is underway.
- **Completed** — all completion criteria have been satisfied.
- **Deferred** — intentionally postponed and not part of the current plan.

## 4. Version 0.1 — Identity and Company Workspace

**Status:** Planned

### 4.1 Goal

Create the first complete and demonstrable user workflow and establish the minimum technical foundation for future document-processing features.

At the end of this milestone, a user must be able to register, sign in, create a company, and view only the companies to which the user belongs.

### 4.2 User Workflow

1. A new user opens the application.
2. The user registers with an email address, display name, and password.
3. The user signs in.
4. The user creates a company.
5. The user becomes the owner of that company.
6. The user views the list of companies to which the user belongs.
7. The user opens a company workspace.
8. The user signs out.

### 4.3 Backend Scope

The backend will include:

- Java 21;
- Spring Boot;
- Maven and Maven Wrapper;
- Spring Web;
- Spring Data JPA;
- Spring Security;
- Bean Validation;
- PostgreSQL;
- Flyway;
- OpenAPI/Swagger for API inspection;
- automated unit and integration tests.

The initial business modules will be:

```text
identity
company
security
common
configuration
```

The exact package structure may be refined during implementation, but business capabilities must remain explicitly separated.

### 4.4 Frontend Scope

The frontend will include:

- React;
- TypeScript;
- Vite;
- a minimal application shell;
- registration page;
- login page;
- company list page;
- create-company page;
- authenticated navigation;
- error and validation feedback.

The first milestone prioritizes a clear working flow over advanced visual design.

### 4.5 Local Infrastructure

Local development will use:

- PostgreSQL in Docker;
- Docker Compose for local infrastructure;
- environment variables for database configuration;
- application profiles for local development and tests.

The Java application and React development server may initially run directly on the host machine. Containerizing them is optional for Version 0.1 unless it provides a concrete benefit during implementation.

### 4.6 Initial Data Model

#### User

Minimum planned fields:

- `id`;
- `email`;
- `password_hash`;
- `display_name`;
- `status`;
- `created_at`;
- `updated_at`.

#### Company

Minimum planned fields:

- `id`;
- `name`;
- `registration_number`;
- `created_at`;
- `updated_at`.

#### CompanyMember

Minimum planned fields:

- `id`;
- `user_id`;
- `company_id`;
- `role`;
- `created_at`.

Initial roles:

- `OWNER`;
- `MEMBER`.

The combination of `user_id` and `company_id` must be unique.

### 4.7 Authentication and Security Scope

Version 0.1 will use:

- email and password credentials;
- secure password hashing;
- Spring Security;
- a server-side HTTP session;
- an HTTP-only session cookie;
- CSRF protection appropriate for a browser-based frontend;
- authorization checks for company membership.

Version 0.1 will not introduce JWT access and refresh tokens.

### 4.8 Planned API Surface

The initial API is expected to include endpoints equivalent to:

```text
POST /api/auth/register
POST /api/auth/login
POST /api/auth/logout
GET  /api/auth/me

GET  /api/companies
POST /api/companies
GET  /api/companies/{companyId}
```

Exact request formats, response formats, and endpoint naming may be refined during implementation and then documented in the API specification.

### 4.9 Tenant Isolation Rule

A user must not gain access to a company merely by knowing its identifier.

For every company-specific request, the backend must verify that the authenticated user has a corresponding `CompanyMember` record.

This rule must be covered by automated tests.

### 4.10 Implementation Sequence

#### Step 1 — Repository and Application Bootstrap

- create the repository structure;
- generate the Spring Boot backend;
- generate the React frontend;
- add Maven Wrapper;
- add basic `.gitignore` and configuration conventions;
- verify that both applications build.

#### Step 2 — Local PostgreSQL and Flyway

- add Docker Compose with PostgreSQL;
- configure environment-based database properties;
- add the first Flyway migration;
- verify local connectivity;
- add persistence integration-test support.

#### Step 3 — Identity Domain

- implement the user model;
- implement registration;
- validate and normalize email addresses;
- hash passwords securely;
- prevent duplicate registration;
- add authentication and registration tests.

#### Step 4 — Session Authentication

- implement login and logout;
- expose the current-user endpoint;
- configure session-cookie behavior;
- configure CSRF protection;
- verify unauthorized and authenticated behavior.

#### Step 5 — Company Workspace

- implement company creation;
- create the owner membership automatically;
- list companies for the authenticated user;
- retrieve an accessible company;
- reject access to unrelated companies;
- add authorization and tenant-isolation tests.

#### Step 6 — Minimal Frontend Flow

- create registration and login screens;
- connect the browser session to the backend;
- create company list and creation screens;
- show validation and authorization errors;
- verify the complete user workflow manually.

#### Step 7 — Quality and Documentation

- add CI for backend tests and frontend build;
- add OpenAPI documentation;
- document real startup commands;
- update `README.md` and `ARCHITECTURE.md` to match the implementation;
- record any new architectural decisions.

### 4.11 Explicit Non-Scope

Version 0.1 does not include:

- financial document upload;
- document binary storage;
- Amazon S3;
- a Python service;
- OCR;
- AI processing;
- asynchronous jobs;
- Kafka;
- Redis;
- Kubernetes;
- production AWS deployment;
- password reset;
- email verification;
- user invitations;
- complete role-based access control;
- advanced audit logging;
- billing or subscriptions.

These exclusions are intentional and protect the milestone from uncontrolled scope growth.

### 4.12 Completion Criteria

Version 0.1 is complete only when all of the following are true:

1. PostgreSQL starts through Docker Compose.
2. Database schema creation and updates are controlled by Flyway.
3. A user can register with valid data.
4. Duplicate user registration is rejected safely.
5. Passwords are stored only as secure hashes.
6. A registered user can sign in and receive a server-side session.
7. The session is represented by an HTTP-only cookie.
8. A signed-in user can sign out.
9. Unauthenticated requests cannot access protected company endpoints.
10. A user can create a company.
11. The company creator receives the `OWNER` membership.
12. A user sees only companies to which the user belongs.
13. Direct access to another user's company is rejected.
14. The complete flow works through the React frontend.
15. Backend unit and integration tests pass.
16. The frontend production build passes.
17. CI runs the required checks automatically.
18. `README.md` contains verified local startup and test commands.
19. `ARCHITECTURE.md` describes the implemented system rather than only the plan.
20. All significant deviations from the approved design are documented in `DECISIONS.md`.

## 5. Version 0.2 — Document Registration and Upload

**Status:** Planned at a high level

### Goal

Allow a user to select a company, upload a financial document, and view the company's document list and processing status.

Likely capabilities:

- document metadata model;
- document upload endpoint;
- company-scoped document authorization;
- document status lifecycle;
- initial file-storage abstraction;
- document list and details UI.

The storage implementation will be selected when this milestone is planned in detail. Local storage, S3-compatible local storage, and Amazon S3 must be evaluated rather than assumed prematurely.

## 6. Version 0.3 — Python Document Processing Service

**Status:** Planned at a high level

### Goal

Introduce a separately deployable Python service for a concrete document-processing capability such as extraction, OCR, classification, validation, or AI-assisted analysis.

The first integration may use synchronous HTTP if the processing operation is sufficiently short and reliable.

## 7. Version 0.4 — Asynchronous Processing

**Status:** Planned at a high level

### Goal

Make document processing resilient to long execution times, retries, temporary failures, and independent service availability.

A queue, broker, or database-backed job mechanism will be selected based on actual requirements. Kafka is a candidate, not a predetermined answer.

## 8. Version 0.5 — Cloud Deployment

**Status:** Planned at a high level

### Goal

Deploy a stable working version to AWS using infrastructure appropriate to the system's actual shape at that time.

Potential capabilities include:

- managed PostgreSQL;
- object storage;
- application deployment;
- secrets management;
- logging and metrics;
- CI/CD deployment pipeline;
- infrastructure cost controls.

## 9. Later Evolution

Possible later capabilities include:

- Redis-backed caching or distributed sessions;
- richer RBAC;
- invitations and collaboration;
- audit history;
- reporting workflows;
- event-driven integrations;
- horizontal scaling;
- Kubernetes, only if operational requirements justify it;
- additional independently deployable services.

These are directions, not commitments. Every future milestone must be justified by a concrete business or engineering need.
