# Tax Platform Architecture Decision Log

## 1. Purpose

This document records significant architecture, security, operational, and engineering decisions for Tax Platform.

An ADR is required when several reasonable alternatives exist and the selected option affects system boundaries, security, data handling, delivery order, operations, or future development.

Routine implementation details, class renames, small refactors, and ordinary bug fixes do not require an ADR.

## 2. Document Status

- **Application implementation:** In Progress
- **Original ADR baseline:** 2026-07-27
- **Agentic architecture update:** 2026-08-05
- **Last updated:** 2026-08-06

The earlier roadmap assumption that Version 0.1 would contain only identity and company workspace work is superseded by the 2026-08-05 roadmap. That assumption was not itself a numbered ADR. The seven original ADRs remain applicable; ADR-001 is clarified by ADR-009 regarding the exact trigger for the first Python service.

## 3. Status Values

- **Proposed** — under discussion and not yet approved.
- **Accepted** — approved and currently applicable.
- **Superseded** — replaced by a later decision.
- **Deprecated** — retained for history but no longer recommended.
- **Rejected** — considered and intentionally not selected.

## 4. Decision Index

| ID | Decision | Status |
|---|---|---|
| ADR-001 | Begin with a Java core application and add specialized services incrementally | Accepted; clarified by ADR-009 |
| ADR-002 | Use a monorepo | Accepted |
| ADR-003 | Run PostgreSQL locally through Docker Compose | Accepted |
| ADR-004 | Use server-side session authentication in Version 0.1 | Accepted |
| ADR-005 | Defer AWS deployment until a complete local workflow exists | Accepted |
| ADR-006 | Model company access through explicit memberships | Accepted |
| ADR-007 | Use Flyway as the authoritative schema-migration mechanism | Accepted |
| ADR-008 | Build a deterministic financial core and use LLMs only for bounded operational assistance | Accepted |
| ADR-009 | Introduce the Python agent service immediately after the first structured integration failure exists | Accepted |
| ADR-010 | Separate Support Investigation Agent from Integration Repair Agent | Accepted |
| ADR-011 | Never send raw customer financial files to an external LLM | Accepted |
| ADR-012 | Use synthetic reproductions for AI-assisted integration repair | Accepted |
| ADR-013 | Keep authorization and business source of truth in Java | Accepted |
| ADR-014 | Expose narrow Java capabilities through Spring AI MCP tools | Accepted |
| ADR-015 | Require human approval before AI-assisted code repair | Accepted |
| ADR-016 | Allow the Repair Agent to create only draft pull requests | Accepted |
| ADR-017 | Keep Kafka, Redis, AWS, and Kubernetes milestone-driven | Accepted |
| ADR-018 | Persist and evaluate agent runs | Accepted |
| ADR-019 | Use specification-driven AI-assisted development | Accepted |

---

## ADR-001 — Begin with a Java Core Application and Add Specialized Services Incrementally

**Date:** 2026-07-27  
**Status:** Accepted; clarified by ADR-009

### Context

The platform is expected to use Java for the main business application and Python for specialized agent capabilities. Starting with many empty services would create build, networking, security, testing, and observability complexity before service boundaries are proven.

A single unstructured application would also make later separation difficult.

### Decision

Begin with one deployable Java core application containing explicit internal business modules. Add separately deployable services only when a concrete capability requires an independent runtime, permission model, deployment boundary, or scaling boundary.

The first such service is the Python agent service introduced under ADR-009.

### Alternatives Considered

1. Create several Java and Python microservices immediately.
2. Implement the entire target platform as one process and one language.
3. Start with one unstructured Java application and split it later.
4. Start with a modular Java core and add specialized services incrementally.

### Reasons

- the deterministic core is initially cohesive;
- local transactions and debugging remain simple;
- module boundaries can be tested before extraction;
- Python appears for a real agentic use case rather than as an empty service;
- the system can become distributed without pretending it already is.

### Consequences

Positive:

- lower initial operational complexity;
- faster first vertical workflow;
- clear ownership inside Java;
- later services have evidence-based boundaries.

Negative:

- Java modules cannot initially deploy or scale independently;
- internal boundaries require discipline;
- later extraction may require API and data-boundary refactoring.

### Revisit When

Reconsider a module when independent scaling, deployment, runtime, failure isolation, ownership, or release cadence provides concrete value.

---

## ADR-002 — Use a Monorepo

**Date:** 2026-07-27  
**Status:** Accepted

### Context

The project will contain a Java backend, React frontend, Python agent service, documentation, synthetic fixtures, and local infrastructure. It is initially developed by one person and benefits from coordinated changes.

### Decision

Store all applications, documentation, infrastructure definitions, and synthetic test assets in one Git repository. Keep each deployable application independently buildable.

### Alternatives Considered

1. Separate repository for every application.
2. One monorepo.
3. Separate code and documentation repositories.

### Reasons

- atomic cross-component changes;
- simpler local setup and portfolio navigation;
- documentation evolves with code;
- lower administrative overhead;
- easier shared CI during early development.

### Consequences

Positive:

- one review can include code, tests, fixtures, and documentation;
- project history is easy to follow;
- local bootstrap is simpler.

Negative:

- CI must become path-aware as the repository grows;
- monorepo location must not justify inappropriate runtime coupling;
- future ownership boundaries may require repository separation.

### Revisit When

Reconsider when applications have independent teams, access controls, release histories, or repository scale becomes harmful.

---

## ADR-003 — Run PostgreSQL Locally Through Docker Compose

**Date:** 2026-07-27  
**Status:** Accepted

### Context

The project requires realistic relational behavior without permanent cloud cost or host-specific database installation.

### Decision

Run PostgreSQL locally through Docker Compose and configure applications through external settings.

The Java and Python services may use separate databases or schemas and credentials within the same local PostgreSQL instance. Python must not query Java-owned tables directly.

### Alternatives Considered

1. Amazon RDS from the first day.
2. Host-installed PostgreSQL.
3. In-memory database for normal development.
4. Docker Compose PostgreSQL.

### Reasons

- reproducible version and configuration;
- real PostgreSQL behavior;
- simple reset and recreation;
- no permanent cloud dependency;
- supports later migration to managed PostgreSQL.

### Consequences

Positive:

- consistent local environment;
- Flyway and database constraints are exercised;
- service-specific schemas can be isolated locally.

Negative:

- Docker is required;
- port, volume, and resource management must be documented;
- local persistence is not a production availability solution.

### Revisit When

Add managed development databases only when collaboration, integration environments, or cloud-specific behavior justify them.

---

## ADR-004 — Use Server-Side Session Authentication in Version 0.1

**Date:** 2026-07-27  
**Status:** Accepted

### Context

The initial client is a browser-based React application communicating with one Java backend. JWT access and refresh tokens would add issuance, storage, refresh, rotation, revocation, and logout concerns that are not required by this topology.

### Decision

Use Spring Security server-side sessions, an HTTP-only session cookie, secure password hashing, and CSRF protection appropriate for the browser frontend.

### Alternatives Considered

1. JWT access and refresh tokens.
2. Server-side sessions.
3. External identity provider.
4. HTTP Basic authentication.

### Reasons

- suitable for the initial browser/backend topology;
- straightforward logout invalidation;
- session identifiers remain outside JavaScript-accessible storage;
- provides practical experience with cookie and CSRF security;
- avoids premature identity infrastructure.

### Consequences

Positive:

- simple browser authentication model;
- server controls invalidation;
- no refresh-token flow.

Negative:

- horizontal scaling later requires a shared session strategy or routing policy;
- CSRF and cross-origin cookie configuration require care;
- external clients may later need another authentication method.

### Revisit When

Reconsider when multiple backend instances, mobile or third-party clients, delegated identity, or an external provider create a concrete need.

---

## ADR-005 — Defer AWS Deployment Until a Complete Local Workflow Exists

**Date:** 2026-07-27  
**Status:** Accepted

### Context

Early AWS work would introduce IAM, networking, secrets, monitoring, and cost before a demonstrable product exists.

### Decision

Complete the local deterministic and agentic workflows before production-style AWS deployment. Applications must still use external configuration and storage abstractions that permit later cloud deployment.

### Alternatives Considered

1. Build cloud infrastructure and application functionality simultaneously from the first commit.
2. Deploy an empty application before the business workflow.
3. Complete stable local vertical flows, then deploy deliberately.

### Reasons

- preserves focus on product progress;
- cloud design can reflect actual components and workload;
- reduces simultaneous learning and failure sources;
- avoids unnecessary cost.

### Consequences

Positive:

- faster local milestones;
- more informed AWS design;
- no misleading empty cloud architecture.

Negative:

- cloud-specific problems are discovered later;
- local assumptions require deliberate review before deployment.

### Revisit When

Begin the cloud milestone after the local deterministic flow, Support Agent, and Repair Agent are stable enough to deploy and observe meaningfully.

---

## ADR-006 — Model Company Access Through Explicit Memberships

**Date:** 2026-07-27  
**Status:** Accepted

### Context

A user may work with multiple companies and a company may later have multiple users. A single company field on a user cannot model this cleanly.

### Decision

Represent user-company access with a `CompanyMember` entity. Initial roles are `OWNER` and `MEMBER`. Company creation and owner membership creation occur in one transaction.

### Alternatives Considered

1. One `company_id` on each user.
2. One owner field on the company and collaboration added later.
3. Explicit membership entity from the beginning.

### Reasons

- supports many-to-many access;
- creates a natural location for company roles;
- enables explicit tenant-isolation queries;
- avoids a predictable future migration.

### Consequences

Positive:

- realistic workspace model;
- extensible roles;
- clear authorization relationship.

Negative:

- additional joins and constraints;
- every company-scoped operation requires a membership check;
- role semantics must remain consistent.

### Revisit When

Extend memberships with invitations, status, permissions, or validity periods, while preserving the explicit relationship.

---

## ADR-007 — Use Flyway as the Authoritative Schema-Migration Mechanism

**Date:** 2026-07-27  
**Status:** Accepted

### Context

The project requires reproducible schema evolution across local, test, and future cloud environments. Hibernate auto-update hides changes and weakens reviewability.

### Decision

Use Flyway migrations as the authoritative schema-change mechanism. Hibernate schema generation is not used to create or update shared environments.

### Alternatives Considered

1. Hibernate `ddl-auto=update`.
2. Manual unversioned SQL.
3. Liquibase.
4. Flyway.

### Reasons

- explicit versioned SQL;
- strong PostgreSQL fit;
- easy Git review;
- familiar Spring Boot integration;
- deterministic environment recreation.

### Consequences

Positive:

- visible schema history;
- reviewed database changes;
- startup detects migration failures.

Negative:

- applied migrations must remain immutable;
- migrations require discipline and production change planning;
- tests must exercise real migrations where practical.

### Revisit When

Only reconsider if a concrete future requirement justifies another authoritative mechanism and migration ownership is explicitly transferred.

---

## ADR-008 — Build a Deterministic Financial Core and Use LLMs Only for Bounded Operational Assistance

**Date:** 2026-08-05  
**Status:** Accepted

### Context

LLMs can assist with ambiguous operational investigation, but financial calculations, accounting rules, tax logic, and report approval require deterministic behavior and accountable domain decisions.

Using an LLM in the ordinary processing path would make correctness, repeatability, audit, privacy, and testing harder without solving a necessary problem.

### Decision

The normal document workflow uses deterministic parsers, canonical models, and technical validation rules. LLM agents are limited to incident investigation and human-approved engineering assistance.

LLMs must not:

- calculate or approve financial values;
- define accounting or tax rules;
- persist unconfirmed financial data;
- become the source of truth for parser behavior;
- directly access production data stores.

### Alternatives Considered

1. Use LLM extraction as the primary parser.
2. Allow agents to choose financial rules dynamically.
3. Keep the financial core deterministic and use agents only for bounded operational work.

### Reasons

- repeatable financial behavior;
- clear testing and audit boundaries;
- reduced hallucination risk;
- easier tenant and data protection;
- agentic value remains real in troubleshooting and repair.

### Consequences

Positive:

- business correctness remains explainable;
- agents can be evaluated independently from financial logic;
- model failures cannot silently change financial results.

Negative:

- deterministic parsers and rules still require engineering effort;
- ambiguous business changes require human decisions;
- the system cannot claim fully autonomous document understanding.

### Revisit When

AI may assist with non-authoritative summarization for domain experts, but any change to financial source-of-truth rules requires a new ADR and stronger controls.

---

## ADR-009 — Introduce the Python Agent Service Immediately After the First Structured Integration Failure Exists

**Date:** 2026-08-05  
**Status:** Accepted

### Context

The original plan placed Python after a broader document milestone. The revised project goal is to demonstrate a genuine agentic system early, without delaying it until the platform is large or cloud-deployed.

An agent cannot be meaningful before there is a real incident, persistent job, structural profile, and narrow evidence tools.

### Decision

Version 0.1 must create one deterministic parser and one structured failure scenario. Version 0.2 immediately introduces a separate Python Support Investigation Agent service.

The project will not wait for Kafka, Redis, AWS, S3, multiple integrations, or production-scale files before adding the first agent.

### Alternatives Considered

1. Add Python before any deterministic failure exists.
2. Delay Python until after cloud and event-driven infrastructure.
3. Add Python immediately after the first structured failure boundary exists.

### Reasons

- avoids an empty or artificial agent service;
- creates early portfolio differentiation;
- keeps the first agent grounded in real tools and state;
- limits the amount of non-agent platform work before the central concept appears.

### Consequences

Positive:

- early end-to-end agentic demonstration;
- tool and security boundaries are designed while the platform is still small;
- evaluation infrastructure starts early.

Negative:

- Version 0.2 adds cross-language and LLM complexity sooner;
- the Version 0.1 incident model must be designed carefully for future tool use;
- local development becomes multi-service earlier.

### Revisit When

The milestone may be split only if Version 0.1 cannot produce a safe structured incident boundary. It must not be delayed merely to add unrelated infrastructure.

---

## ADR-010 — Separate Support Investigation Agent from Integration Repair Agent

**Date:** 2026-08-05  
**Status:** Accepted

### Context

Incident investigation and repository modification have different risks, tools, inputs, approvals, and failure modes. Combining them into one broad agent would create excessive permissions and make evaluation less clear.

### Decision

Implement two agents with separate roles and permissions:

- **Support Investigation Agent** — reads sanitized operational evidence, classifies incidents, recommends actions, creates tickets, and escalates.
- **Integration Repair Agent** — starts only after approval, works with synthetic reproduction in an isolated repository sandbox, prepares code and tests, and creates a draft pull request.

### Alternatives Considered

1. One general-purpose agent with all tools.
2. Many narrowly specialized agents from the beginning.
3. Two agents aligned with investigation and repair permission boundaries.

### Reasons

- least privilege;
- clearer human approval boundary;
- simpler evaluation suites;
- easier trace interpretation;
- avoids artificial agent proliferation.

### Consequences

Positive:

- support cannot accidentally modify code;
- repair cannot start from an unapproved ambiguous incident;
- tool sets and prompts remain focused.

Negative:

- handoff state must be modeled explicitly;
- two orchestration paths must be maintained;
- ticket and approval contracts must be stable.

### Revisit When

Add another agent only when a distinct goal and permission boundary cannot be represented cleanly by these two agents.

---

## ADR-011 — Never Send Raw Customer Financial Files to an External LLM

**Date:** 2026-08-05  
**Status:** Accepted

### Context

Financial files may contain amounts, personal identifiers, bank accounts, company identifiers, and sensitive operational details. Reliable anonymization of arbitrary spreadsheets is difficult and outside the portfolio scope.

### Decision

External LLMs receive no raw customer financial files and no financial row values. The first portfolio version uses synthetic financial data and sanitized structural profiles, metadata, redacted traces, documentation, and incident summaries.

### Alternatives Considered

1. Send complete files under contractual controls.
2. Build reversible anonymization for arbitrary spreadsheets.
3. Use only synthetic data and minimized structural evidence.

### Reasons

- minimizes privacy and confidentiality risk;
- avoids an unrealistic anonymization subsystem;
- supports meaningful incident classification without row values;
- simplifies safe public demonstration.

### Consequences

Positive:

- strong and explainable data boundary;
- lower prompt and trace leakage risk;
- portfolio demos can be shared safely.

Negative:

- some incidents may remain unknown and require human analysis;
- the agent cannot inspect exact problematic financial values;
- structural profiling must be sufficiently informative.

### Revisit When

Any future use of real customer data requires legal, security, provider, retention, and anonymization review plus a new ADR. The default remains prohibition.

---

## ADR-012 — Use Synthetic Reproductions for AI-Assisted Integration Repair

**Date:** 2026-08-05  
**Status:** Accepted

### Context

A repair agent needs a reproducible failing input, but production customer files must not enter the repair sandbox or model context.

### Decision

Repair runs use synthetic files that reproduce the relevant structural difference without real customer values. The expected behavior is approved before repair begins.

### Alternatives Considered

1. Copy the production file into the sandbox.
2. Use only textual incident descriptions without reproduction.
3. Build a synthetic reproduction from approved structural evidence.

### Reasons

- preserves privacy;
- enables deterministic regression tests;
- makes the issue portable and reviewable;
- separates business approval from implementation.

### Consequences

Positive:

- safe repeatable tests;
- clean incident-to-test traceability;
- repair artifacts can remain in the repository.

Negative:

- synthetic reproduction may fail to capture hidden production details;
- humans may need to refine the fixture;
- a false reproduction can lead to a misleading patch.

### Revisit When

If a case cannot be reproduced synthetically, the repair agent pauses and requests human input rather than receiving production data.

---

## ADR-013 — Keep Authorization and Business Source of Truth in Java

**Date:** 2026-08-05  
**Status:** Accepted

### Context

The Python service is optimized for agent orchestration, not for tenant authorization or financial state ownership. Direct shared-database access would bypass Java policies and couple schemas.

### Decision

Java owns authentication, company authorization, documents, processing jobs, parser metadata, incidents, known issues, retry eligibility, business rules, and platform audit state.

Python owns agent execution state but accesses Java-owned data only through authorized narrow tools or versioned events. Python does not query Java business tables directly.

### Alternatives Considered

1. Shared database access from both services.
2. Move incident and authorization ownership to Python.
3. Keep Java as source of truth and expose controlled capabilities.

### Reasons

- one clear authorization boundary;
- no duplicated tenant logic;
- reduced schema coupling;
- easier audit and consistency;
- safe model/tool separation.

### Consequences

Positive:

- service responsibilities are clear;
- Java policy cannot be bypassed by an agent;
- Python can evolve independently within its own state model.

Negative:

- more API or MCP contracts;
- cross-service failures must be handled;
- some local queries become network calls.

### Revisit When

Business ownership may move only through an explicit service-extraction ADR with data migration, authorization, and event ownership defined.

---

## ADR-014 — Expose Narrow Java Capabilities Through Spring AI MCP Tools

**Date:** 2026-08-05  
**Status:** Accepted

### Context

The agent needs operational evidence and limited actions. A general internal API, database credential, arbitrary SQL, or filesystem access would create excessive capability and weak auditability.

### Decision

Use Spring AI in the Java backend to expose narrow, typed, authorized MCP tools. The Python agent connects through MCP and receives minimum necessary sanitized responses.

### Alternatives Considered

1. Direct database access.
2. General-purpose REST endpoints and broad service credentials.
3. Arbitrary query or shell tools.
4. Narrow Spring AI MCP tools with schema validation and audit.

### Reasons

- meaningful use of Spring AI and MCP;
- explicit least-privilege contracts;
- Java reuses existing authorization and domain services;
- tool calls are observable and testable;
- the model cannot invent new capabilities.

### Consequences

Positive:

- clear agent boundary;
- strong schema and permission enforcement;
- easier forbidden-tool evaluation;
- no database sharing.

Negative:

- tool contracts require design and versioning;
- too many tiny tools could create complexity;
- MCP integration adds another protocol and test layer.

### Revisit When

A tool may move to another protocol if interoperability or reliability requires it, but the narrow, authorized, audited capability model remains mandatory.

---

## ADR-015 — Require Human Approval Before AI-Assisted Code Repair

**Date:** 2026-08-05  
**Status:** Accepted

### Context

An incident may reveal a technical defect, an unsupported variant, or an unresolved business decision. The model cannot infer authoritative expected behavior from operational evidence alone.

### Decision

A Repair Agent run requires:

- a structured engineering ticket;
- explicit approved expected behavior;
- an approved synthetic reproduction;
- an isolated sandbox.

If expected behavior is missing or ambiguous, the agent pauses and requests human input.

### Alternatives Considered

1. Start repair automatically after every parser failure.
2. Allow the agent to infer business behavior.
3. Require human approval before repair.

### Reasons

- preserves domain accountability;
- prevents speculative parser changes;
- creates a clear audit point;
- supports human-in-the-loop demonstration.

### Consequences

Positive:

- safer patches;
- clearer acceptance criteria;
- less unrelated or incorrect automation.

Negative:

- repair is not fully autonomous;
- approval can delay resolution;
- UI and persistence must support paused states.

### Revisit When

Only low-risk, pre-approved mechanical change classes could later use standing approval, and that would require a new ADR and strong evaluation evidence.

---

## ADR-016 — Allow the Repair Agent to Create Only Draft Pull Requests

**Date:** 2026-08-05  
**Status:** Accepted

### Context

Even a patch that compiles and passes tests may contain architectural, security, maintainability, or business problems. Giving an agent merge or deployment authority would collapse independent review.

### Decision

The Repair Agent may modify only an isolated branch and may create only a draft pull request or local draft equivalent. It cannot push to `main`, merge, approve its own change, or deploy.

### Alternatives Considered

1. Agent pushes directly to `main`.
2. Agent opens a normal ready-for-review pull request and auto-merges on green CI.
3. Agent creates a draft with tests and risk summary for human review.

### Reasons

- independent human review remains mandatory;
- AI-generated status is explicit;
- tests are evidence, not authority;
- supports reject, request-changes, tests-only, and manual-rewrite outcomes.

### Consequences

Positive:

- reduced autonomous-change risk;
- clear ownership of merge and deployment;
- portfolio demo includes professional review practice.

Negative:

- human work remains necessary;
- repair throughput is lower;
- pull-request provider integration must represent draft state.

### Revisit When

No autonomous merge is planned for the portfolio scope. Any future exception requires a new ADR with change-risk classification and organizational controls.

---

## ADR-017 — Keep Kafka, Redis, AWS, and Kubernetes Milestone-Driven

**Date:** 2026-08-05  
**Status:** Accepted

### Context

These technologies are valuable learning targets but can create artificial complexity when added without a working requirement.

### Decision

Retain them in the target architecture but introduce them only when a milestone demonstrates the corresponding need:

- Kafka for durable event-driven boundaries, outbox, retries, and idempotent consumers;
- Redis for shared sessions, measured caching, rate limiting, or short-lived locks;
- AWS and S3 after stable local workflows exist;
- Kubernetes only after multiple deployable services and a concrete orchestration goal exist.

### Alternatives Considered

1. Remove these technologies from the project entirely.
2. Add all of them at repository bootstrap.
3. Keep them as target technologies with explicit triggers.

### Reasons

- preserves learning goals;
- avoids resume-driven architecture;
- each technology can be explained through a real problem;
- keeps early milestones achievable.

### Consequences

Positive:

- lower initial complexity;
- stronger architecture narrative;
- reduced cost and maintenance.

Negative:

- some technologies appear later than a broad checklist might suggest;
- triggering criteria must be monitored honestly;
- later milestones may require substantial infrastructure work.

### Revisit When

Revisit each technology separately when its triggering problem is observed and measurable.

---

## ADR-018 — Persist and Evaluate Agent Runs

**Date:** 2026-08-05  
**Status:** Accepted

### Context

An agentic demonstration based only on transient chat output is difficult to resume, audit, debug, compare, or trust. Model behavior changes with prompts, tools, and provider versions.

### Decision

Persist agent runs, steps, tool executions, approvals, evaluation results, model and prompt versions, sanitized arguments, results, errors, timings, token usage, estimated cost, and final classifications.

Maintain formal Support and Repair evaluation suites with expected and forbidden behavior.

### Alternatives Considered

1. Keep only application logs.
2. Store only the final agent answer.
3. Persist structured execution state and formal evaluations.

### Reasons

- restart recovery;
- traceable tool use;
- prompt and model comparison;
- cost and latency visibility;
- regression detection;
- demonstrable engineering rigor.

### Consequences

Positive:

- inspectable and resumable runs;
- measurable agent quality;
- easier incident debugging;
- evidence for safe tool boundaries.

Negative:

- additional schema and retention work;
- traces require redaction;
- evaluations can be probabilistic and need tolerance rules.

### Revisit When

Retention, sampling, and storage may change with volume, but structured persistence and evaluation remain required.

---

## ADR-019 — Use Specification-Driven AI-Assisted Development

**Date:** 2026-08-05  
**Status:** Accepted

### Context

AI can accelerate implementation but may silently change architecture, broaden scope, add dependencies, weaken security, perform unrelated refactors, or claim success without tests.

### Decision

All coding agents and AI-assisted contributions follow repository specifications in `AGENTS.md` and `CONTRIBUTING.md`:

```text
Issue
-> acceptance criteria
-> repository inspection
-> short implementation plan
-> focused branch
-> code and tests
-> relevant test execution
-> changed-file and risk summary
-> developer review
-> meaningful commit
-> pull request
```

Agents cannot write to `main`, merge, deploy, weaken tenant isolation, move financial logic into LLMs, or use raw financial data.

### Alternatives Considered

1. Free-form AI coding without repository rules.
2. Prohibit AI-assisted development.
3. Use specification-driven AI assistance with human review and tests.

### Reasons

- preserves developer understanding and ownership;
- creates small reviewable diffs;
- aligns project development with the product's own repair-agent safety model;
- improves commit and documentation quality.

### Consequences

Positive:

- more consistent AI contributions;
- visible architecture and security constraints;
- better test and review discipline;
- meaningful project history.

Negative:

- additional planning and reporting overhead;
- agents may stop and request clarification more often;
- repository instructions require maintenance.

### Revisit When

Update the rules as build commands, modules, and CI gates become concrete. Core prohibitions require a new ADR to weaken.

---

## 5. ADR Template

```markdown
## ADR-XXX — Decision Title

**Date:** YYYY-MM-DD  
**Status:** Proposed | Accepted | Superseded | Deprecated | Rejected

### Context

What problem or constraint requires a decision?

### Decision

What was selected?

### Alternatives Considered

What reasonable alternatives were evaluated?

### Reasons

Why was this option selected?

### Consequences

Positive and negative consequences.

### Revisit When

What future condition should trigger reconsideration?
```
