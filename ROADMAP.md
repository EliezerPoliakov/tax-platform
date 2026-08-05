# Tax Platform Roadmap

## 1. Document Status

- **Current milestone:** Version 0.1 — Minimal Platform and Deterministic Integration
- **Current milestone status:** In Progress
- **Application implementation:** In Progress
- **Documentation baseline:** Approved on 2026-08-05
- **Last updated:** 2026-08-05

This roadmap is the authoritative source for implementation order, dependencies, milestone completion criteria, and what becomes demonstrable after each version.

## 2. Status Definitions

- **Implemented now** — exists in the repository, runs, and has been verified.
- **Approved for current milestone** — committed scope for active implementation.
- **Planned later** — approved direction assigned to a later milestone.
- **Optional future evolution** — not committed and requires future justification.
- **Deferred** — intentionally removed from the current sequence.

At the date above, repository bootstrap, PostgreSQL, Flyway, user persistence, and browser registration are implemented. Version 0.1 remains in progress.

## 3. Roadmap Principles

1. Every milestone must end with a working vertical scenario.
2. The financial core remains deterministic.
3. The first agent appears as soon as one real structured integration failure exists.
4. A technology is added only when the milestone demonstrates the problem it solves.
5. Security, tenant isolation, tests, and honest documentation are completion criteria, not optional cleanup.
6. Synthetic data is used for all portfolio integrations and agent repair scenarios.
7. Later infrastructure must not be described as implemented before its milestone is complete.

## 4. Version 0.1 — Minimal Platform and Deterministic Integration

**Status:** In Progress

### 4.1 Current Implementation Progress

Implemented and verified:

- GitHub monorepo with buildable Spring Boot and React applications;
- Maven Wrapper and Vite project bootstrap;
- PostgreSQL 17 through Docker Compose;
- environment-based database configuration;
- Flyway baseline and `users` schema migrations;
- JPA `User` model and repository;
- normalized unique email persistence;
- Spring Security password hashing with `{bcrypt}` storage;
- public registration API with validation and duplicate-email conflict response;
- CSRF-token endpoint and CSRF enforcement for registration;
- React registration form connected to the backend through the Vite proxy;
- PostgreSQL repository and registration API integration tests;
- frontend lint and production-build verification;
- manual browser-to-database registration verification.

Still required for Version 0.1:

- login, logout, current-user endpoint, and authenticated server-side session;
- HTTP-only authenticated-session cookie behavior;
- company membership and tenant isolation;
- document metadata and storage abstraction;
- persistent processing job and deterministic parser;
- canonical success output;
- structured failure, structural profile, and incident;
- complete frontend workflow;
- repository CI and final milestone documentation.

### 4.2 Goal

Deliver the smallest local platform that proves the deterministic business flow and creates the structured failure boundary required by the first agent milestone.

### 4.3 Demonstrable Scenario

At the end of Version 0.1:

1. A user registers and signs in.
2. The user creates or selects a company.
3. The user uploads a normal synthetic integration file.
4. The backend stores the file, creates a persistent processing job, selects a parser version, and processes it deterministically.
5. The UI displays a successful normalized result.
6. The user uploads a problematic synthetic variant.
7. The backend records a structured processing failure, structural file profile, and integration incident.
8. The UI displays a stable incident identifier and structured error.

No LLM is required for this milestone.

### 4.4 Scope

#### Repository and Build

- monorepo structure;
- Java 21 and Spring Boot backend;
- Maven Wrapper;
- React, TypeScript, and Vite frontend;
- Docker Compose for PostgreSQL;
- environment-based configuration;
- backend tests and frontend production build in CI;
- project documentation maintained with code.

#### Identity and Company Access

- user registration;
- secure password hashing;
- login, logout, and current-user endpoint;
- server-side session authentication;
- HTTP-only cookie configuration;
- CSRF protection suitable for a browser SPA;
- company creation and listing;
- explicit `CompanyMember` relationship;
- company-scoped authorization and negative tenant-isolation tests.

#### Documents and Processing

- one synthetic integration type;
- one small synthetic file format;
- company-scoped document metadata;
- local document-storage abstraction and implementation;
- file upload validation;
- persistent processing job;
- deterministic parser with explicit parser version;
- canonical normalized model;
- technical validation rules;
- success and failure states;
- duplicate or repeated-request strategy documented and minimally enforced;
- correlation identifier and audit-relevant timestamps.

#### Structured Failure Boundary

- normalized error code;
- processing stage;
- redacted technical error detail;
- structural file profile without financial row values;
- integration incident record;
- parser metadata and retry history fields needed by Version 0.2;
- one deliberately reproducible failure scenario, preferably a multi-year or header-layout variant.

#### User Interface

- registration and login screens;
- company selection and creation;
- document upload;
- processing status;
- successful normalized result summary;
- structured failure and incident display;
- basic validation and authorization feedback.

### 4.5 Explicit Non-Scope

Version 0.1 does not include:

- Python agent service;
- external LLM calls;
- MCP tools;
- automated incident classification;
- repair automation;
- Kafka;
- Redis;
- S3 or AWS deployment;
- production-scale large-file processing;
- real financial data;
- real tax calculations or official tax forms;
- Jira or GitHub integration.

The architecture must still leave a clear path toward these capabilities.

### 4.6 Implementation Order

#### Step 1 — Repository Bootstrap

**Status:** In Progress

Implemented:

- created `backend/` and `frontend/`;
- generated buildable applications;
- added Maven Wrapper, `.gitignore`, and environment example;
- verified backend tests and frontend lint/build.

Remaining:

- add baseline repository CI.

#### Step 2 — PostgreSQL and Flyway

**Status:** Implemented

- added Docker Compose PostgreSQL;
- configured environment-based local database settings;
- configured Spring Boot through `application.yml`;
- made Flyway authoritative for schema changes;
- applied the baseline and `users` migrations;
- verified application connectivity, schema validation, and migration history.

#### Step 3 — Identity and Session Security

**Status:** In Progress

Implemented:

- user persistence and unique normalized email;
- registration request validation;
- adaptive password hashing;
- duplicate-email conflict handling;
- CSRF-token endpoint and CSRF-protected registration;
- registration API integration tests;
- React registration screen and backend connection.

Remaining:

- implement login and logout;
- expose the current authenticated user;
- create and verify the server-side authenticated session;
- configure and verify authenticated-session cookie behavior;
- add session lifecycle and unauthorized-access tests.

#### Step 4 — Company Membership and Tenant Isolation

- implement company and membership entities;
- create owner membership transactionally;
- list accessible companies;
- enforce company authorization in backend services;
- add cross-company access tests.

#### Step 5 — Document Metadata and Storage Abstraction

- define company-scoped document model;
- define `DocumentStorage` interface;
- add local filesystem implementation for synthetic files;
- upload and retrieve metadata without exposing arbitrary filesystem paths.

#### Step 6 — Persistent Processing Job and Parser

- create processing-job lifecycle;
- select integration type and parser version;
- implement one deterministic parser and canonical model;
- persist success output or structured failure.

#### Step 7 — Structural Profile and Incident

- extract header/schema metadata without row values;
- create a reproducible failure fixture;
- persist integration incident and normalized error;
- expose incident details through company-authorized APIs.

#### Step 8 — Complete Frontend Vertical Flow

- connect registration, company selection, upload, status, success, and failure views;
- verify browser session and CSRF behavior;
- demonstrate both normal and failing files.

#### Step 9 — Quality and Documentation

- run all automated checks;
- add verified startup commands;
- update architecture from approved plan to implemented reality;
- document deviations with ADRs;
- record demo steps.

### 4.7 Completion Criteria

Version 0.1 is complete only when:

1. PostgreSQL starts through Docker Compose.
2. Flyway is authoritative for the schema.
3. Backend and frontend builds pass in CI.
4. A user can register, sign in, remain authenticated through a server-side session, and sign out.
5. CSRF protection is enabled and verified for state-changing browser requests.
6. A user can create a company and receives an owner membership in the same transaction.
7. Company-scoped operations reject a non-member even when the identifier is known.
8. A user can upload a supported synthetic file to an authorized company.
9. The binary is stored through an abstraction rather than an arbitrary path supplied by the client.
10. A persistent processing job survives request completion and records timestamps and status transitions.
11. The parser and parser version are recorded.
12. A valid file produces deterministic canonical output.
13. A problematic file produces a normalized error, structural profile, and persistent incident.
14. The structural profile excludes raw financial row values.
15. The complete normal and failure flows work through the React UI.
16. Unit, integration, security, parser, and API tests pass.
17. Documentation accurately states what is implemented and what remains planned.

### 4.8 Dependencies

None beyond repository setup and local development tooling.

### 4.9 What Becomes Demonstrable

A secure, multi-tenant Java platform can accept a synthetic integration file, process it deterministically, and produce an incident boundary suitable for an agent without exposing raw financial data.

## 5. Version 0.2 — Support Investigation Agent

**Status:** Planned later

### 5.1 Goal

Introduce the first genuine agentic milestone immediately after Version 0.1 provides a structured incident and structural file profile.

### 5.2 Scope

- separate Python agent service;
- selected LLM provider integration;
- Support Investigation Agent;
- multi-step tool-calling loop;
- narrow typed tools exposed by Java;
- Spring AI MCP server capabilities in Java;
- Python MCP client integration;
- incident classification;
- evidence collection;
- known issue and similar incident search;
- retry-eligibility lookup;
- support recommendation;
- internal engineering-ticket provider;
- human escalation;
- persistent `agent_run`, `agent_step`, `tool_execution`, `agent_approval`, and `agent_evaluation` records;
- sanitized traces, timing, token usage, and estimated cost;
- first formal Support Agent evaluation suite;
- React incident investigation summary and trace summary.

### 5.3 Required Tools

Initial tools should include equivalents of:

```text
get_incident_summary
get_processing_job_status
get_structural_file_profile
get_parser_metadata
get_redacted_error_trace
search_known_issues
search_similar_incidents
get_recent_deployments
get_integration_documentation
check_retry_eligibility
create_support_recommendation
create_engineering_ticket
escalate_to_human
```

State-changing tools require explicit service permissions and audit records.

### 5.4 Completion Criteria

1. A structured incident can trigger or manually start a Support Agent run.
2. The agent performs multiple tool calls based on evidence.
3. Java rechecks authorization and returns only minimal sanitized data.
4. Python has no direct access to Java-owned business tables.
5. The agent produces one of the approved classifications.
6. Known issue, user guidance, safe retry recommendation, ticket creation, and human escalation paths are represented.
7. Retry safety comes only from deterministic backend logic.
8. Agent state and tool history survive service restart.
9. Traces contain no raw financial data, credentials, or complete production logs.
10. The first fifteen Support Agent evaluation scenarios run with recorded results.
11. The UI displays classification, evidence summary, proposed action, state, and trace summary without private chain-of-thought content.

### 5.5 Dependencies

- Version 0.1 incident and structural profile;
- stable Java authorization boundary;
- at least one known issue and several synthetic incident fixtures.

### 5.6 What Becomes Demonstrable

A persistent Python agent investigates a Java-owned integration incident through narrow MCP tools, gathers evidence, classifies the case, and safely chooses guidance, ticket creation, retry recommendation, or escalation.

## 6. Version 0.3 — Integration Repair Agent

**Status:** Planned later

### 6.1 Goal

Demonstrate human-approved AI-assisted repair of a deterministic integration parser using only synthetic reproduction data and an isolated repository sandbox.

### 6.2 Scope

- repair approval workflow;
- approved expected behavior;
- synthetic reproduction generator or curated fixture;
- ephemeral container or sandbox per repair run;
- repository clone and isolated branch;
- restricted repository tools;
- reproduction command;
- code search and focused file reads;
- patch generation;
- regression-test generation;
- targeted, module, and full test execution;
- architecture restriction checks;
- diff and risk summary;
- `PullRequestProvider` abstraction;
- local draft provider first, GitHub adapter later if justified;
- draft pull request only;
- Repair Agent evaluation suite;
- human review outcome recording.

### 6.3 Completion Criteria

1. A repair run cannot start without an approved ticket and expected behavior.
2. A synthetic file reproduces the failure.
3. The agent works in an isolated branch and cannot write to `main`.
4. The agent reads only repository content required by the issue.
5. The patch fixes the targeted regression scenario.
6. Existing parser tests and module tests remain green.
7. Full backend tests are run before a draft is considered ready.
8. Unrelated refactoring is rejected by policy or evaluation.
9. The draft includes root cause, implementation, changed files, test results, risks, AI-generated status, and data-boundary confirmation.
10. The agent cannot merge or deploy.
11. Human approve, request-changes, reject, tests-only, and manual-rewrite outcomes can be recorded.
12. Repair evaluation scenarios produce persisted results.

### 6.4 Dependencies

- Version 0.2 Support Agent and ticket flow;
- approved repository conventions in `AGENTS.md` and `CONTRIBUTING.md`;
- reproducible deterministic parser failure;
- stable test commands.

### 6.5 What Becomes Demonstrable

An incident can move from automated investigation to human-approved synthetic reproduction and AI-assisted code repair, ending in a tested draft pull request that still requires human review.

## 7. Version 0.4 — Asynchronous and Event-Driven Processing

**Status:** Planned later

### 7.1 Goal

Introduce Kafka only after real asynchronous boundaries exist among document processing, incident creation, agent investigation, repair approval, and reprocessing.

### 7.2 Scope

- Java outbox pattern;
- Kafka topics and versioned event contracts;
- events such as `DocumentUploaded`, `ProcessingStarted`, `ProcessingFailed`, `IntegrationIncidentCreated`, `InvestigationRequested`, `RepairApproved`, `FixDeployed`, and `ReprocessingRequested`;
- idempotent consumers;
- bounded concurrency;
- retries and dead-letter handling;
- correlation and causation identifiers;
- observable consumer lag;
- background workers;
- safe replay and duplicate handling;
- migration from direct trigger paths only where justified.

### 7.3 Completion Criteria

- at least one end-to-end flow uses the outbox and Kafka;
- duplicate delivery does not duplicate business effects;
- failed messages reach a controlled retry or dead-letter path;
- event versions are documented;
- correlation is visible across Java and Python services;
- queue lag and consumer failures are observable;
- the synchronous local alternative is removed only where the event path is stable.

### 7.4 Dependencies

- stable processing and agent state machines from Versions 0.1–0.3.

### 7.5 What Becomes Demonstrable

The platform handles long-running and cross-service work with durable event delivery, idempotency, retries, dead-letter handling, and observable operational state.

## 8. Version 0.5 — Cloud Document Storage and Deployment

**Status:** Planned later

### 8.1 Goal

Move a stable local system to bounded-cost AWS infrastructure and durable object storage.

### 8.2 Scope

- Amazon S3 for uploaded and synthetic reproduction objects;
- document metadata retained in PostgreSQL;
- streamed or pre-signed upload strategy where file size justifies it;
- independent Java and Python deployment;
- managed secrets;
- centralized logs and metrics;
- health checks;
- CI/CD deployment pipeline;
- bounded cloud spending and cleanup policy;
- managed PostgreSQL if justified by the deployment design.

### 8.3 Completion Criteria

- no secrets are committed to Git;
- objects are tenant-scoped and not publicly readable;
- local and cloud storage implementations share the same application abstraction;
- services deploy independently;
- deployment is repeatable through CI/CD or documented infrastructure automation;
- logs and health are centrally inspectable;
- cloud costs and resource teardown are documented.

### 8.4 Dependencies

- stable local end-to-end flow;
- defined object lifecycle and storage abstraction;
- sufficient tests to detect deployment regressions.

### 8.5 What Becomes Demonstrable

The local architecture evolves into a deployable cloud platform with durable object storage, independent services, secrets management, health checks, and CI/CD.

## 9. Version 0.6 — Reliability and Operational Maturity

**Status:** Planned later

### 9.1 Goal

Improve reliability using measured operational needs rather than adding infrastructure by default.

### 9.2 Scope

Possible approved work includes:

- Redis-backed Spring sessions when horizontal Java scaling requires them;
- caching known issues or operational metadata when measured read patterns justify it;
- rate limiting;
- short-lived distributed locks when a concrete contention scenario exists;
- dashboards and alerts;
- processing and agent cost tracking;
- incident grouping and recurrence detection;
- controlled reprocessing;
- failure recovery after service restart;
- load and soak tests;
- tenant-isolation and tool-authorization security tests;
- retention and cleanup policies.

### 9.3 Completion Criteria

Defined during milestone planning from measured bottlenecks and failure modes. Redis is not a completion requirement unless a demonstrated problem needs it.

### 9.4 Dependencies

- operational data from the deployed or realistically exercised system.

### 9.5 What Becomes Demonstrable

The platform can explain and measure its reliability, cost, failure recovery, security, and performance behavior.

## 10. Version 0.7 — Additional Integrations

**Status:** Planned later

### 10.1 Goal

Prove that the canonical model, parser versioning, incident system, and agent workflows generalize beyond a single fixture.

### 10.2 Candidate Synthetic Integrations

- Priority-like multi-year export;
- simplified CPA open-format file;
- exchange-rate API adapter;
- changed external API response contract;
- CSV encoding and delimiter variation;
- Excel sheet and header-layout variation;
- enum or date-format changes.

### 10.3 Completion Criteria

- at least two materially different integration adapters exist;
- each has versioned parser metadata and regression fixtures;
- failures map to common incident concepts without losing integration-specific evidence;
- no proprietary code or real customer data is used;
- agent evaluations include cross-integration cases.

### 10.4 What Becomes Demonstrable

The system is an extensible integration platform rather than a single hard-coded parser demonstration.

## 11. Optional Future Evolution

The following remain optional and are not assigned to an early milestone:

- Kubernetes for orchestration and scaling practice;
- richer user invitations and RBAC;
- external identity provider;
- mobile or third-party API authentication;
- real Jira and GitHub adapters beyond the provider interfaces;
- advanced incident clustering;
- open-document summarization for domain experts;
- additional Java service extraction;
- multi-region or high-availability design.

Each requires a separate decision and a concrete problem statement.

## 12. Deferred Work

The following are deliberately deferred from Version 0.1:

- identity-page polish beyond a usable flow;
- production-scale spreadsheet anonymization;
- real tax rules and official form interpretation;
- autonomous reprocessing without deterministic idempotency confirmation;
- agents with broad shell, SQL, database, or production-file access;
- autonomous merge and deployment;
- Kafka, Redis, AWS, and Kubernetes before their triggering problems exist.

## 13. Immediate Next Implementation Task

The next implementation issue is:

> Complete the authentication lifecycle with login, logout, current-user retrieval, and a verified server-side session, while preserving CSRF protection and the existing registration flow.

Expected scope:

- authenticate by normalized email and password;
- reject invalid credentials without leaking account details;
- create an authenticated server-side session;
- expose `GET /api/auth/me`;
- implement logout and session invalidation;
- add API integration tests for success, failure, unauthenticated access, session reuse, and logout;
- extend the React UI only enough to demonstrate login, current user, and logout.

Company membership remains the following task after session authentication is complete.

