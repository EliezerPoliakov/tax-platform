# Contributing to Tax Platform

## 1. Purpose

Tax Platform uses an issue-driven workflow designed to keep changes understandable, testable, secure, and suitable for human review. These rules apply to manual and AI-assisted contributions.

## 2. Current Status

- **Current milestone:** Version 0.1 — Minimal Platform and Deterministic Integration
- **Application implementation:** Not started
- **Last updated:** 2026-08-05

Before contributing, read:

- `README.md`;
- `PROJECT.md`;
- `ROADMAP.md`;
- `ARCHITECTURE.md`;
- `DECISIONS.md`;
- `AGENTS.md` when AI assistance is used.

## 3. Standard Workflow

```text
Issue
-> acceptance criteria
-> implementation plan
-> small branch
-> code and tests
-> developer review
-> meaningful commit
-> pull request
```

### 3.1 Create or Select an Issue

Every non-trivial change should have an issue containing:

- problem statement;
- user or engineering value;
- scope;
- non-scope;
- acceptance criteria;
- security and data considerations;
- relevant documentation links;
- expected test level.

### 3.2 Inspect Before Changing

Before implementation:

- identify the owning module;
- search for existing behavior and tests;
- verify current architecture and milestone status;
- identify schema, API, security, and compatibility impact;
- write a short implementation plan.

### 3.3 Use a Focused Branch

Preferred names:

```text
feature/123-document-upload
fix/217-tenant-check
ai-fix/INC-1042-priority-multi-year
chore/45-ci-bootstrap
```

Do not work directly on `main`.

### 3.4 Implement the Smallest Complete Change

A contribution should:

- satisfy the issue without unrelated refactoring;
- preserve module and service boundaries;
- include validation and structured errors;
- protect tenant scope;
- use synthetic test data;
- add or update tests;
- update documentation when behavior or architecture changes.

### 3.5 Run Verification

The exact commands will be added after application bootstrap. A completed change will eventually require the relevant subset of:

- backend unit tests;
- backend integration tests;
- parser regression tests;
- security and tenant-isolation tests;
- frontend type checking and production build;
- Python tests;
- agent evaluation scenarios;
- full backend test suite for repair-agent output.

Do not state that a change is complete if required checks were not run or failed.

### 3.6 Review the Diff

Before committing:

- remove unrelated changes;
- confirm no secret, real financial data, or local path was added;
- verify migrations are append-only;
- verify error messages do not leak internals;
- verify documentation uses honest implementation status;
- verify the change does not weaken tenant or tool authorization.

## 4. Commit Guidelines

Commits should be coherent and meaningful.

Good examples:

```text
bootstrap Spring Boot backend with PostgreSQL configuration
add company membership authorization checks
persist structured parser incidents
add regression fixture for ten-year integration export
```

Avoid:

```text
fix
try again
AI changes
misc
update stuff
```

A commit should not mix an unrelated refactor, feature, schema change, and documentation rewrite unless they are inseparable parts of one issue.

## 5. Pull Request Requirements

A pull request should include:

- linked issue;
- problem and approved behavior;
- implementation summary;
- changed modules and files;
- database migration impact;
- API or event contract impact;
- security and tenant-isolation impact;
- test commands and results;
- screenshots or demo steps when UI behavior changes;
- known risks and follow-up work;
- documentation changes;
- disclosure of material AI assistance.

A repair-agent draft pull request must also include:

- incident identifier;
- root cause;
- synthetic reproduction description;
- regression coverage;
- baseline and final test results;
- architecture restriction checks;
- confirmation that no production data or secrets were used;
- explicit statement that the patch is AI-generated and requires human review.

## 6. Review Expectations

Reviewers should verify:

- acceptance criteria are met;
- architecture remains consistent with ADRs;
- company isolation is enforced server-side;
- deterministic financial logic remains outside LLMs;
- raw financial data is not exposed;
- tests cover success, failure, and unauthorized cases;
- migrations and contracts are safe;
- logs and traces are redacted;
- documentation accurately reflects implementation;
- the diff is focused and maintainable.

Passing tests are necessary but not sufficient for approval.

## 7. Database Changes

- Flyway is authoritative.
- Do not edit a migration already applied to a shared environment.
- Add a new migration for every schema change.
- Include database constraints for consistency-critical rules.
- Test migrations against PostgreSQL.
- Document destructive or irreversible changes.

## 8. API and Error Changes

- use stable structured error codes and localizable message codes;
- preserve correlation identifiers;
- do not expose stack traces, SQL, storage paths, secrets, or internal exception text to users;
- document intentional breaking changes;
- add authorization tests for company-scoped endpoints;
- validate all externally supplied data.

## 9. Synthetic Data Policy

Only synthetic financial data may be committed or used in portfolio demos.

Synthetic fixtures must not contain:

- copied customer rows;
- real company names or identifiers;
- real bank details;
- personal identifiers;
- production logs;
- credentials;
- proprietary source code or confidential integration specifications.

## 10. AI-Assisted Contributions

AI assistance is allowed when the developer understands and reviews the result.

The contributor remains responsible for:

- architecture;
- correctness;
- security;
- tests;
- licensing;
- documentation;
- commit quality.

Coding agents must follow `AGENTS.md`. AI-generated code is not exempt from normal review or test requirements.

## 11. Documentation Updates

Update documentation when a contribution changes:

- current implementation status;
- milestone completion or ordering;
- architecture or data ownership;
- security and privacy boundaries;
- public startup and test commands;
- agent tools, states, approvals, or evaluations;
- infrastructure and deployment.

Milestone names must remain consistent across `README.md`, `ROADMAP.md`, and `ARCHITECTURE.md`.

## 12. Definition of Done

A contribution is done when:

1. the issue and acceptance criteria are satisfied;
2. the diff is focused;
3. required tests and builds pass;
4. security and tenant boundaries are preserved;
5. no real financial data or secrets were introduced;
6. documentation is accurate;
7. review feedback is resolved;
8. the commit history is meaningful;
9. the change is merged by an authorized human or normal repository process.
