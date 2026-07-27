# Tax Platform Architecture Decision Log

## 1. Purpose

This document records significant architectural and engineering decisions for the Tax Platform.

A decision belongs here when:

- several reasonable alternatives existed;
- the choice affects architecture, security, operations, or future development;
- the trade-off may need to be explained later;
- reversing the decision would require meaningful work.

Routine implementation details, class renames, minor refactoring, and ordinary bug fixes do not require an ADR.

## 2. Status Values

- **Proposed** — under discussion and not yet approved.
- **Accepted** — approved and currently applicable.
- **Superseded** — replaced by a later decision.
- **Deprecated** — retained for history but no longer recommended.
- **Rejected** — considered and intentionally not selected.

## 3. Decision Index

| ID | Decision | Status |
|---|---|---|
| ADR-001 | Begin with a Java core application and add specialized services incrementally | Accepted |
| ADR-002 | Use a monorepo | Accepted |
| ADR-003 | Run PostgreSQL locally through Docker Compose | Accepted |
| ADR-004 | Use server-side session authentication in Version 0.1 | Accepted |
| ADR-005 | Defer AWS deployment until a complete local workflow exists | Accepted |
| ADR-006 | Model company access through explicit memberships | Accepted |
| ADR-007 | Use Flyway as the authoritative schema-migration mechanism | Accepted |

---

## ADR-001 — Begin with a Java Core Application and Add Specialized Services Incrementally

**Date:** 2026-07-27  
**Status:** Accepted

### Context

The target platform is expected to use Java for the main business application and Python for specialized document-processing or AI-related capabilities.

Starting with many empty microservices would increase build, networking, deployment, security, testing, and observability complexity before service boundaries are proven by real business requirements.

Describing the entire future platform as a monolith would also be misleading because a separately deployable Python service is already part of the intended direction.

### Decision

The project will begin with one Java core application containing explicit internal business modules.

Separately deployable specialized services, including a Python document-processing service, will be introduced when the corresponding business capability is implemented.

No empty service will be created solely to demonstrate microservices.

### Alternatives Considered

1. Create several Java and Python microservices immediately.
2. Build the entire target platform as one process and one language.
3. Begin with one unstructured Java application and split it later.
4. Begin with a modular Java core and incrementally add specialized services.

### Reasons

- Version 0.1 has one small, cohesive business workflow.
- Cross-service boundaries are not yet supported by running requirements.
- A modular Java application allows fast delivery while preserving business separation.
- Python should appear when there is real document-processing work for it to perform.
- The approach allows the platform to become distributed without pretending it already is.

### Consequences

Positive:

- lower initial operational complexity;
- faster implementation of the first complete workflow;
- simpler local transactions;
- easier debugging and integration testing;
- service extraction remains possible later.

Negative:

- Java modules cannot initially scale or deploy independently;
- careless implementation could erode internal boundaries;
- future extraction may require refactoring and data-boundary changes.

### Revisit When

Reconsider a module boundary when at least one of the following becomes true:

- independent scaling is required;
- independent deployment is required;
- a different runtime or language provides concrete value;
- a failure domain must be isolated;
- ownership or release cadence becomes independent;
- synchronous in-process coupling prevents reliability goals.

---

## ADR-002 — Use a Monorepo

**Date:** 2026-07-27  
**Status:** Accepted

### Context

The project will contain a Java backend, React frontend, documentation, local infrastructure, and later a Python processing service.

The project is currently developed by one person and benefits from coordinated changes across applications.

### Decision

Store all Tax Platform applications and project-level documentation in one Git repository.

Planned top-level structure:

```text
backend/
frontend/
document-processing-service/  # added later
docs/
project documentation
local infrastructure
```

Each deployable application must remain independently buildable even though it shares the repository.

### Alternatives Considered

1. A separate repository for every application from the beginning.
2. One monorepo for the complete platform.
3. One repository for code and a separate repository for documentation.

### Reasons

- simpler project setup and navigation;
- atomic changes across backend, frontend, documentation, and local infrastructure;
- easier portfolio presentation;
- simpler CI coordination during early development;
- low organizational overhead for a single-developer project.

### Consequences

Positive:

- related changes can be reviewed in one commit or pull request;
- documentation can evolve with code;
- local development is easier to bootstrap;
- shared version history is visible.

Negative:

- CI must avoid rebuilding unrelated applications unnecessarily as the repository grows;
- repository-level conventions must not create inappropriate code sharing;
- future team ownership may require more granular boundaries.

### Revisit When

Reconsider repository boundaries if applications develop independent ownership, access controls, release histories, or repository size becomes operationally harmful.

---

## ADR-003 — Run PostgreSQL Locally Through Docker Compose

**Date:** 2026-07-27  
**Status:** Accepted

### Context

Version 0.1 requires a relational database. Connecting to a permanent AWS database for ordinary local development would add cost, network dependency, credentials, latency, and cloud configuration before the first business workflow exists.

A locally installed PostgreSQL instance could work but would make environment consistency more dependent on each developer's machine.

### Decision

Use PostgreSQL in Docker for local development and manage it through Docker Compose.

The application will receive database settings through external configuration such as environment variables.

### Alternatives Considered

1. Use Amazon RDS from the first day.
2. Require PostgreSQL to be installed directly on the host machine.
3. Use an in-memory database for normal development.
4. Run PostgreSQL in Docker Compose.

### Reasons

- reproducible database version and configuration;
- no permanent cloud dependency for daily work;
- realistic PostgreSQL behavior;
- simple reset and recreation of local state;
- easier later transition to managed PostgreSQL through configuration.

### Consequences

Positive:

- development works offline after dependencies are available;
- the local database is close to the intended production database type;
- setup is easier to document and reproduce;
- database lifecycle is explicit.

Negative:

- Docker is required for local infrastructure;
- volume management and port conflicts must be documented;
- Docker resource usage may be noticeable on weaker machines.

### Revisit When

The local development database may be supplemented with managed development environments when collaboration, integration testing, or cloud-specific behavior creates a concrete need.

---

## ADR-004 — Use Server-Side Session Authentication in Version 0.1

**Date:** 2026-07-27  
**Status:** Accepted

### Context

Version 0.1 has a browser-based React frontend and one Java Spring Boot backend.

JWT access and refresh tokens are familiar and popular, but they introduce token issuance, storage, rotation, refresh, logout, and revocation concerns that are not required by the initial topology.

The project should also provide practical experience with server-side sessions, secure cookies, and CSRF protection.

### Decision

Use Spring Security with server-side session authentication for Version 0.1.

The browser will receive a session identifier through an HTTP-only cookie. State-changing browser requests will use CSRF protection appropriate for the frontend integration.

Passwords will be stored only as secure adaptive hashes.

### Alternatives Considered

1. JWT access and refresh tokens.
2. Server-side sessions.
3. An external identity provider such as Amazon Cognito.
4. HTTP Basic authentication for the application UI.

### Reasons

- appropriate for one browser frontend and one backend;
- simpler logout invalidation;
- keeps credentials and session identifiers out of JavaScript-accessible storage;
- provides practical learning about cookie security and CSRF;
- avoids premature external identity infrastructure.

### Consequences

Positive:

- straightforward browser authentication model;
- server controls session invalidation;
- no refresh-token flow in the initial version;
- reduced exposure to token storage mistakes in the frontend.

Negative:

- horizontal scaling later requires sticky sessions or shared session storage;
- CSRF must be understood and implemented correctly;
- cookie behavior across origins requires careful local and production configuration;
- non-browser clients may later need a different authentication strategy.

### Revisit When

Reconsider the authentication architecture when:

- multiple backend instances are deployed;
- mobile or third-party API clients are introduced;
- services need delegated identity;
- an external identity provider provides clear operational value;
- session storage becomes a scaling or reliability limitation.

---

## ADR-005 — Defer AWS Deployment Until a Complete Local Workflow Exists

**Date:** 2026-07-27  
**Status:** Accepted

### Context

AWS is part of the target learning and deployment direction. However, deploying immediately would require infrastructure, IAM, networking, secrets, monitoring, and cost management before the application provides a demonstrable business workflow.

### Decision

Complete Version 0.1 locally before introducing production-style AWS deployment.

The application must still be designed for external configuration and future deployment, but cloud infrastructure is outside the first milestone.

### Alternatives Considered

1. Build infrastructure and application functionality simultaneously from the first commit.
2. Deploy an empty application to AWS before implementing the workflow.
3. Complete a local milestone first, then introduce AWS deliberately.

### Reasons

- preserves focus on business functionality;
- reduces the number of new systems being learned simultaneously;
- prevents infrastructure work from hiding lack of product progress;
- allows deployment choices to reflect a real application shape.

### Consequences

Positive:

- faster path to the first working version;
- fewer initial failure sources;
- AWS design can use real workload and component requirements;
- no unnecessary early cloud cost.

Negative:

- cloud-specific problems are discovered later;
- local assumptions must be reviewed carefully before deployment;
- deployment readiness is not demonstrated in Version 0.1.

### Revisit When

Begin the cloud milestone after Version 0.1 is complete and the project has verified startup, tests, configuration, and one complete user flow.

---

## ADR-006 — Model Company Access Through Explicit Memberships

**Date:** 2026-07-27  
**Status:** Accepted

### Context

A realistic financial platform must support users working with multiple companies and, later, multiple users working with one company.

Storing one `company_id` directly on the user record would prevent or complicate these workflows.

### Decision

Represent the relationship between users and companies with a `CompanyMember` entity.

Initial roles are `OWNER` and `MEMBER`.

Creating a company creates the corresponding owner membership in the same database transaction.

### Alternatives Considered

1. Store a single `company_id` on each user.
2. Store a single `owner_user_id` on the company and add collaboration later.
3. Use an explicit many-to-many membership entity from the beginning.

### Reasons

- supports multiple companies per user;
- supports multiple users per company;
- creates a natural location for company-specific roles;
- enables explicit tenant-isolation checks;
- avoids a known future data-model migration for collaboration.

### Consequences

Positive:

- realistic company-workspace model;
- extensible role assignments;
- clear authorization queries;
- supports invitations and accountants later.

Negative:

- more tables and joins than a single-company model;
- every company-scoped request requires membership checks;
- role semantics must remain company-specific and consistent.

### Revisit When

The membership model may evolve to include invitations, role permissions, membership status, validity periods, or organization hierarchies. The basic explicit relationship is expected to remain.

---

## ADR-007 — Use Flyway as the Authoritative Schema-Migration Mechanism

**Date:** 2026-07-27  
**Status:** Accepted

### Context

The project requires reproducible schema creation and controlled evolution across local, test, and future cloud environments.

Allowing Hibernate to create or update production schemas automatically hides changes and makes database state harder to review and reproduce.

### Decision

Use Flyway migrations as the authoritative source of database schema changes.

Hibernate schema generation will not be used as the normal mechanism for creating or updating shared environments.

### Alternatives Considered

1. Hibernate `ddl-auto=update`.
2. Manual SQL executed outside the application lifecycle.
3. Liquibase.
4. Flyway.

### Reasons

- explicit, versioned SQL migrations;
- easy review in Git;
- strong fit for PostgreSQL;
- familiar integration with Spring Boot;
- useful practical experience for production systems.

### Consequences

Positive:

- deterministic schema history;
- easier environment recreation;
- database changes are reviewed with application changes;
- migration failures are visible during startup or deployment.

Negative:

- migrations require discipline;
- applied migrations must not be casually edited;
- rollback strategy must be designed for each meaningful production change;
- test setup must run migrations rather than bypassing them.

### Revisit When

Reconsider only if future requirements demonstrate a concrete need for another migration mechanism. A change would require a documented migration of migration ownership, not silent coexistence.

---

## 4. ADR Template

Use the following template for future decisions:

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
