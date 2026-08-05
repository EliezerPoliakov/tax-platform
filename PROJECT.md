# Tax Platform Project Charter

## 1. Document Status

- **Document role:** Stable project vision and boundaries
- **Application implementation:** Not started
- **Current approved milestone:** Version 0.1 — Minimal Platform and Deterministic Integration
- **Last updated:** 2026-08-05

This document distinguishes among:

- **implemented now** — application capabilities that exist and have been verified;
- **approved for the current milestone** — committed scope for Version 0.1;
- **planned for later** — approved direction with a later milestone;
- **optional future evolution** — a learning or architecture option that requires future justification.

At the date above, project documentation exists, but application code and runtime capabilities have not yet been implemented.

## 2. Vision

Tax Platform is a small but realistic financial integration platform inspired by a limited subset of the operational problems found in systems such as Rubixtax.

It is not a tutorial CRUD application, a complete rewrite of Rubixtax, or an autonomous tax system. The project combines a deterministic financial core with bounded LLM agents that investigate integration incidents and assist with human-approved code repair.

The project should be understandable as a product, defensible as an architecture, and demonstrable as an engineering portfolio system.

## 3. Project Goals

The project has five primary goals.

### 3.1 Deliver a Complete Financial Integration Scenario

Build one end-to-end workflow that includes authentication, company isolation, document upload, persistent processing, deterministic parsing, normalized output, structured failure handling, and later report-oriented use of validated data.

### 3.2 Demonstrate Production-Oriented Java Engineering

Show practical use of:

- Java 21 and Spring Boot;
- modular business boundaries;
- Spring Security and server-side sessions;
- PostgreSQL and Flyway;
- transactions, idempotency, structured errors, audit, and testing;
- large-file-aware processing design;
- integration adapters and canonical models;
- CI/CD, logging, monitoring, and cloud evolution.

### 3.3 Demonstrate a Genuine Agentic System

Build agents that:

- receive an operational goal;
- choose among narrow tools;
- perform multiple tool calls based on intermediate results;
- persist state;
- stop or request human input when required;
- preserve traces;
- operate within explicit permissions;
- are evaluated with formal scenarios.

The first agent investigates integration incidents. The second, separately permissioned agent prepares a repair in an isolated repository sandbox after human approval.

### 3.4 Learn Technologies Through Demonstrated Need

Docker, Python, MCP, Spring AI, Kafka, Redis, AWS, S3, observability, and Kubernetes remain part of the target learning direction, but each must enter the system at a milestone where it solves an actual problem.

### 3.5 Produce a Credible Portfolio Narrative

At every completed milestone, the developer should be able to explain:

- what user or operational problem was solved;
- what is implemented and what is not;
- why the architecture was selected;
- how company and data boundaries are protected;
- how failures are represented and investigated;
- why each infrastructure component exists;
- what trade-offs remain.

## 4. Core Deterministic Business Workflow

The normal financial workflow must not depend on an LLM.

1. A user registers and signs in.
2. The user selects or creates a company workspace.
3. The user uploads a financial integration file.
4. The file is stored through a document-storage abstraction.
5. A persistent processing job is created.
6. The backend determines the integration type and parser version.
7. A deterministic parser processes the file.
8. Parsed data is mapped to a canonical internal model.
9. Technical validation rules run.
10. The user receives a successful result or a structured error.
11. A qualifying failure creates an integration incident and structural file profile.
12. Validated normalized data may later participate in deterministic calculations and report generation.

The exact first integration format is synthetic and simplified. It exists to prove architecture and failure handling rather than to reproduce proprietary formats.

## 5. Agentic Operational Workflow

LLM agents are used for complex, variable, and poorly formalized operational work that would otherwise require support specialists and developers to manually investigate each integration failure.

```text
Integration processing failure
        |
        v
Support Investigation Agent
        |
        v
Evidence collection and classification
        |
        +--> User guidance
        +--> Known issue and approved workaround
        +--> Deterministically approved retry
        +--> Human escalation
        +--> Engineering ticket
                     |
                     v
               Human approval
                     |
                     v
           Integration Repair Agent
                     |
                     v
       Synthetic reproduction and isolated branch
                     |
                     v
       Patch, regression tests, and test execution
                     |
                     v
                 Draft PR
                     |
                     v
             Human code review
```

### 5.1 Support Investigation Agent

Planned for Version 0.2, the Support Investigation Agent:

- gathers incident metadata and redacted evidence;
- reads structural file profiles without row values;
- checks parser metadata, known issues, similar incidents, retries, and deployment context;
- classifies the failure;
- prepares a support recommendation;
- creates an engineering ticket when code change is likely;
- requests human help when evidence is insufficient.

It does not modify application code.

### 5.2 Integration Repair Agent

Planned for Version 0.3, the Integration Repair Agent:

- starts only from an approved engineering ticket and expected behavior;
- works with a synthetic reproduction in an isolated repository clone;
- reads only relevant repository content;
- produces a focused patch and regression tests;
- runs targeted, module, and full test suites;
- creates a draft pull request or local draft equivalent;
- records risks and test results.

It cannot write to `main`, merge, deploy, access production secrets, or use real customer financial data.

## 6. Guiding Principles

### 6.1 Deterministic Core, Bounded AI

LLMs do not perform financial calculations, define accounting or tax rules, approve reports, or become the business source of truth.

AI output is operational advice or a proposed engineering change that remains subject to deterministic validation and human review.

### 6.2 Vertical Milestones

Every milestone must end with a working, explainable scenario. Infrastructure-only progress is insufficient unless it directly enables the demonstrated flow.

### 6.3 Technology Must Solve a Demonstrated Problem

- Flyway manages schema history.
- Docker provides repeatable local infrastructure.
- Python provides agent orchestration where Java is not the selected agent runtime.
- Spring AI and MCP expose narrow Java capabilities without database sharing.
- Kafka appears when event-driven asynchronous boundaries exist.
- Redis appears for a concrete shared-session, cache, rate-limit, or lock requirement.
- S3 appears when durable object storage and large-file workflows are ready.
- Kubernetes remains optional until several services and operational needs justify it.

### 6.4 Java Remains the Security and Business Boundary

Java owns authentication, company authorization, document metadata, processing jobs, parser selection, incidents, known issues, retry eligibility, business rules, and audit-relevant platform state.

Python is not trusted as a security boundary and does not read Java-owned business tables directly.

### 6.5 Privacy by Data Minimization

The portfolio system uses synthetic financial data.

External LLMs receive only the minimum sanitized technical information required for a tool call or investigation. Raw customer documents, financial amounts, personal identifiers, bank accounts, real company names, credentials, and database dumps are outside the permitted model context.

### 6.6 Human Control over Material Actions

Human approval is required before a repair run. Human review is required before code merge. Deployment is performed by normal CI/CD or a human-approved process, not by an agent.

### 6.7 Persistent and Auditable Work

Processing jobs, incidents, agent runs, tool calls, approvals, and evaluation results must be persisted. A service restart must not silently erase an active investigation or repair history.

### 6.8 Small, Focused Engineering Changes

Implementation should follow issue-driven work, explicit acceptance criteria, small branches, tests, meaningful commits, and reviewable pull requests. Coding agents must follow [AGENTS.md](AGENTS.md).

### 6.9 Honest Documentation

Documentation must never present planned components as implemented. The words implemented, approved, planned, and optional must retain distinct meanings.

## 7. Core Data and Integration Concepts

The stable domain direction includes:

- users;
- companies;
- company memberships;
- document metadata and storage references;
- integration type and parser version;
- persistent processing jobs;
- canonical normalized records;
- technical validation results;
- structured errors;
- structural file profiles;
- integration incidents;
- known issues;
- retry eligibility decisions;
- internal engineering tickets;
- agent runs, steps, tool executions, approvals, and evaluations.

Detailed ownership and lifecycle are defined in [ARCHITECTURE.md](ARCHITECTURE.md).

## 8. Security Position

The following are mandatory project rules:

- every company-scoped operation rechecks membership in Java;
- browser or agent-provided company identifiers are never sufficient authorization;
- every MCP tool validates identity, permission, tenant scope, and input schema;
- Python has no direct access to Java business data storage;
- arbitrary SQL, arbitrary shell access, and unrestricted file reads are not agent tools;
- raw financial files are not sent to an external LLM;
- prompts, traces, logs, and Git must not contain secrets;
- uploaded document text is untrusted data and cannot override system instructions;
- state-changing tools require explicit permission;
- retry safety is determined by deterministic backend logic;
- repair requires human approval;
- merge and deployment remain outside agent authority;
- model output is validated and treated as a proposal.

See [docs/SECURITY_AND_DATA_BOUNDARIES.md](docs/SECURITY_AND_DATA_BOUNDARIES.md).

## 9. Non-Goals

The project does not require:

- a complete Rubixtax replacement;
- real tax forms or proprietary parser code;
- real customer financial data;
- production-grade reversible anonymization for arbitrary spreadsheets;
- autonomous accounting or tax decisions;
- autonomous report approval;
- dozens of integrations or agents;
- autonomous merge or deployment;
- a complete Jira or GitHub integration in the earliest milestone;
- Kafka before event-driven processing exists;
- Redis without a measured requirement;
- Kubernetes in early versions;
- a large custom UI design system;
- production-scale infrastructure before a local workflow is complete.

## 10. Approved Milestone Direction

- **Version 0.1:** Minimal platform and deterministic integration.
- **Version 0.2:** Support Investigation Agent.
- **Version 0.3:** Integration Repair Agent.
- **Version 0.4:** Asynchronous and event-driven processing.
- **Version 0.5:** Cloud document storage and deployment.
- **Version 0.6:** Reliability and operational maturity.
- **Version 0.7:** Additional integrations and variants.

The detailed order, dependencies, and completion criteria are maintained in [ROADMAP.md](ROADMAP.md).

## 11. Portfolio Goals

The project should eventually demonstrate:

- Java/Spring Boot backend engineering;
- React/TypeScript frontend development;
- PostgreSQL schema and transaction design;
- session authentication and multi-tenant authorization;
- deterministic document processing and canonical modeling;
- structured production troubleshooting;
- Python agent orchestration;
- multi-step tool calling through narrow MCP capabilities;
- human-in-the-loop repair workflows;
- agent persistence, tracing, and evaluation;
- asynchronous event processing and idempotency;
- cloud storage and deployment;
- CI/CD, testing, observability, and security controls.

A technology counts only after the relevant working scenario exists and can be explained.

## 12. Definition of Project Success

The project is portfolio-successful when the following demonstration works:

1. A user signs in and selects a company.
2. A normal synthetic integration file is uploaded and processed successfully.
3. A problematic synthetic variant creates a persistent job failure and integration incident.
4. The Support Investigation Agent independently calls multiple narrow tools, gathers evidence, classifies the problem, produces a safe support recommendation, and creates an engineering ticket when required.
5. The user interface exposes a concise reasoning summary without private chain-of-thought content.
6. A human approves the expected repair behavior.
7. The Repair Agent reproduces the problem with synthetic data, reads the relevant repository code, creates a focused patch and regression tests, runs the required tests, and produces a draft pull request.
8. A human reviews the change.
9. Agent traces and evaluation results are available for demonstration.
10. No raw financial data or production secret is sent to the model.

Only after this milestone exists may the project be described in a resume as an implemented agentic integration-support workflow.

## 13. Documentation Responsibilities

- [README.md](README.md) is the public entry point and honest current-status summary.
- [PROJECT.md](PROJECT.md) is the stable charter.
- [ROADMAP.md](ROADMAP.md) controls milestone scope and status.
- [ARCHITECTURE.md](ARCHITECTURE.md) defines current and future system structure.
- [DECISIONS.md](DECISIONS.md) records significant choices and supersession.
- [AGENTS.md](AGENTS.md) constrains AI-assisted development.
- [CONTRIBUTING.md](CONTRIBUTING.md) defines issue, branch, commit, review, and test practices.
- `docs/` contains the detailed incident, security, and evaluation designs.

After each milestone, these documents must be updated to reflect the running system rather than the earlier plan.
