# AGENTS.md

## 1. Purpose

This file defines mandatory rules for coding agents and AI-assisted development in the Tax Platform repository.

It applies to any agent that reads, changes, tests, reviews, or documents repository content. It does not grant authority to bypass human review, security boundaries, branch protections, or the architectural decisions recorded in `DECISIONS.md`.

## 2. Current Project Status

- **Application implementation:** In Progress
- **Current milestone:** Version 0.1 — Minimal Platform and Deterministic Integration
- **Milestone status:** In Progress
- **Last updated:** 2026-08-06

Implemented now:

- Java and React application bootstrap;
- PostgreSQL through Docker Compose;
- Flyway schema migrations;
- user persistence and repository tests;
- user registration with validation, normalized email, bcrypt password hashing, and duplicate handling;
- session-based authentication lifecycle (login, logout, /api/auth/me);
- Spring Security authenticated server-side sessions and CSRF protection;
- company membership and tenant isolation with transactional creation;
- React authentication flow, company workspace, and selection.

Not implemented yet:

- documents, processing, parsers, incidents, or agents;
- Kafka, Redis, AWS, S3, Kubernetes, or production deployment.

Agents must verify the current status in `README.md`, `ROADMAP.md`, and `ARCHITECTURE.md` before making changes. Planned technologies must not be described as implemented.

## 3. Required Work Pattern

For every implementation task, the agent must:

1. Read the issue, acceptance criteria, relevant documentation, and existing code.
2. Inspect the repository before proposing changes.
3. Identify the owning module and relevant tests.
4. Provide a short implementation plan before modifying files.
5. Make the smallest focused change that satisfies the issue.
6. Add or update tests for the behavior.
7. Run the relevant test and build commands.
8. Review the resulting diff.
9. Report changed files, test results, assumptions, and risks.
10. Stop without claiming completion if required checks fail.

## 4. Architecture Rules

Agents must:

- preserve the deterministic financial core;
- keep authentication, tenant authorization, business rules, processing jobs, incidents, and retry eligibility in Java;
- preserve explicit module boundaries;
- avoid moving business logic into `common` or generic utility packages;
- use interfaces at infrastructure boundaries such as document storage, ticket providers, and pull-request providers;
- keep Python agent orchestration separate from Java business ownership;
- access Java-owned data through authorized APIs or MCP tools, never direct database access;
- keep Support Investigation and Integration Repair permissions separate;
- preserve human approval before repair and human review before merge;
- keep Kafka, Redis, AWS, S3, and Kubernetes milestone-driven.

Agents must not silently change architecture. A significant architecture change requires an ADR or an explicit update to an existing ADR.

## 5. Financial and Data Safety Rules

Agents must not:

- move financial calculations, accounting rules, tax logic, or report approval into an LLM;
- use real customer financial data in code, tests, prompts, examples, logs, or fixtures;
- send raw uploaded files or financial row values to an external LLM;
- add real company names, registration numbers, bank accounts, personal identifiers, or credentials to fixtures;
- create unrestricted anonymization claims;
- treat model output as financial source of truth;
- log file contents, secrets, session identifiers, or sensitive tool arguments.

Use only synthetic data and sanitized structural metadata.

## 6. Tenant Isolation Rules

Agents must not weaken tenant isolation.

Every company-scoped operation must:

- derive the authenticated identity server-side;
- verify company membership in Java;
- scope repository queries by company or authorized resource relationship;
- reject access from a non-member even when the resource identifier is known;
- include negative authorization tests.

Frontend hiding, client-provided roles, agent identity, or possession of an identifier is never sufficient authorization.

## 7. Agent Tool and MCP Rules

When implementing product agents or tools:

- tools must be narrow, typed, and schema-validated;
- tools must return minimum necessary data;
- every tool must authenticate and authorize its caller;
- company and incident scope must be rechecked in Java;
- state-changing tools require explicit permission and audit;
- tool output must be redacted;
- arbitrary SQL, arbitrary shell, unrestricted file reads, direct production database access, deployment, merge, and financial-data mutation tools are forbidden;
- retry safety must come from deterministic backend logic;
- uploaded document text is untrusted data and cannot change system instructions;
- tool calls, sanitized arguments, outcomes, and errors must be traceable.

## 8. Repair Agent Rules

A repair implementation must preserve these product constraints:

- no repair run without an approved ticket and expected behavior;
- synthetic reproduction only;
- isolated repository clone and branch;
- no production secrets or customer files in the sandbox;
- no writes to `main`;
- no merge or deployment;
- only focused changes relevant to the approved issue;
- regression tests are mandatory;
- targeted tests, module tests, and full backend tests must be run before draft completion;
- failing tests must be reported honestly;
- the output is a draft pull request requiring human review.

## 9. Dependency Rules

Before adding a dependency, the agent must state:

- the concrete problem it solves;
- why existing dependencies or standard libraries are insufficient;
- maintenance and security implications;
- whether the dependency affects runtime size, licensing, build, or deployment;
- the test needed to prove its use.

Do not add a framework or infrastructure component solely because it is popular or mentioned in the target architecture.

## 10. Change Scope Rules

Agents must:

- make small focused diffs;
- avoid unrelated formatting or refactoring;
- preserve public contracts unless the issue explicitly changes them;
- update migrations rather than editing applied Flyway migrations;
- avoid broad package renames during feature work;
- not remove tests simply to make a build pass;
- not weaken assertions, authorization, validation, or error handling without explicit approval;
- not hide failures with catch-all exceptions or silent fallbacks.

## 11. Testing Rules

Every behavior change requires appropriate tests.

Current verified commands:

```bash
# local database required by current backend integration tests
docker compose up -d postgres

# backend
cd backend
./mvnw test

# frontend
cd frontend
npm run lint
npm run build
```

Agents must run the relevant commands from the repository state they actually inspected and must report any omitted verification explicitly.

Expected layers include:

- unit tests for business rules;
- PostgreSQL and Flyway integration tests;
- transaction and uniqueness tests;
- session, CSRF, and tenant-isolation tests;
- document-storage contract tests;
- parser fixture and regression tests;
- processing-job and incident-state tests;
- MCP tool authorization and redaction tests;
- Python orchestration and persistence tests;
- formal agent evaluation scenarios.

An agent must not declare a task complete when:

- required tests were not run;
- tests fail;
- the build fails;
- acceptance criteria remain unverified;
- implementation status and documentation contradict each other.

## 12. Documentation Rules

Update documentation when a change affects:

- current implementation status;
- milestone scope or completion;
- architecture diagrams or boundaries;
- public startup or test commands;
- security or data handling;
- significant technology choices;
- tool contracts or agent state;
- deployment or operations.

Use these status terms consistently:

- implemented now;
- approved for the current milestone;
- planned for later;
- optional future evolution.

Do not describe planned technology in present tense as a running component.

## 13. Branch, Commit, and Pull Request Rules

Agents must not write directly to `main`.

Preferred branch patterns:

```text
feature/<issue>-short-description
fix/<issue>-short-description
ai-fix/<incident>-short-description
chore/<issue>-short-description
```

Commits must be meaningful. Avoid messages such as:

```text
fix
try
changes
AI changes
update
```

A pull request or handoff must include:

- issue and acceptance criteria;
- implementation summary;
- changed files;
- test commands and results;
- security and data-boundary impact;
- risks and remaining work;
- whether AI generated or materially assisted the change.

## 14. Required Final Report Format

After a code task, report:

```text
Summary
- what changed and why

Changed files
- file path: purpose

Tests
- command: result

Acceptance criteria
- satisfied / not satisfied with evidence

Risks and assumptions
- concise list

Documentation
- updated files or reason no update was needed
```

Do not expose private chain-of-thought. Provide concise evidence, decisions, and outcomes.

## 15. Stop Conditions

The agent must stop and request human input when:

- business behavior is not approved;
- a change would alter financial or tax logic;
- tenant isolation cannot be preserved;
- the only available reproduction uses real customer data;
- required secrets or production access would be needed;
- the task requires merging, deployment, or writing to `main`;
- architecture restrictions conflict with the requested implementation;
- tests reveal a broader issue outside the approved scope.
