# Tax Platform Architecture

## 1. Document Status

- **Architecture scope:** Approved plan for Version 0.1
- **Implementation status:** Not started
- **Current milestone:** Identity and Company Workspace
- **Last updated:** 2026-07-27

This document distinguishes between:

- **Version 0.1 architecture** — approved for the next implementation milestone;
- **future evolution** — possible later components that are not yet implemented.

Future components must not be described as part of the current running system until they exist.

## 2. Architectural Goals

The architecture should:

- support one complete business workflow at a time;
- remain understandable and demonstrable;
- establish clear business boundaries;
- protect company data from unauthorized access;
- allow local development without permanent cloud dependencies;
- support gradual extraction of specialized services;
- avoid infrastructure that has no current business purpose;
- make important trade-offs explicit and testable.

## 3. Version 0.1 System Context

### 3.1 Users

The initial system has one primary actor:

- a registered user who manages one or more company workspaces.

Later milestones may introduce accountants, invited team members, administrators, or external integrations.

### 3.2 System Context Diagram

```text
+--------------------+
| Browser User       |
+---------+----------+
          |
          | HTTP / HTTPS
          v
+--------------------+
| Tax Platform       |
|                    |
| React Frontend     |
| Java Backend       |
+---------+----------+
          |
          | SQL
          v
+--------------------+
| PostgreSQL         |
+--------------------+
```

No Python service, message broker, Redis instance, or AWS service is part of Version 0.1.

## 4. Version 0.1 Container View

```text
+---------------------------+
| React Frontend            |
|                           |
| - Registration UI         |
| - Login UI                |
| - Company list UI         |
| - Create company UI       |
+-------------+-------------+
              |
              | JSON over HTTP / REST
              | Browser session cookie
              v
+---------------------------+
| Java Spring Boot App      |
|                           |
| - Identity module         |
| - Security module         |
| - Company module          |
| - Common infrastructure   |
+-------------+-------------+
              |
              | JPA / JDBC
              v
+---------------------------+
| PostgreSQL                |
|                           |
| - users                   |
| - companies               |
| - company_members         |
+---------------------------+
```

## 5. Deployment Model for Local Development

The approved local model is:

```text
Developer machine
├── React development server
├── Java Spring Boot application
└── Docker Compose
    └── PostgreSQL
```

The frontend and backend may later be containerized, but Version 0.1 does not require containerization merely for appearance.

PostgreSQL runs in Docker so that:

- developers use a consistent database version;
- local setup is reproducible;
- database state is isolated from host installations;
- the application can later switch to a managed PostgreSQL instance through configuration.

## 6. Repository Architecture

The project starts as a monorepo:

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

A future Python service will be added as a separate top-level application when its business capability is implemented:

```text
tax-platform/
├── backend/
├── frontend/
├── document-processing-service/
└── ...
```

Sharing one repository does not require sharing one runtime process. Each deployable application remains independently buildable.

## 7. Java Application Structure

The Version 0.1 backend is one deployable Spring Boot application with explicit internal business modules.

Planned module boundaries:

```text
com.example.taxplatform
├── identity
├── company
├── security
├── common
└── configuration
```

The final base package will be selected when the project is generated.

### 7.1 Identity Module

Responsibilities:

- user registration;
- email normalization and uniqueness;
- password hashing;
- user status;
- retrieval of the authenticated user's profile.

The identity module must not contain company-specific authorization logic.

### 7.2 Security Module

Responsibilities:

- Spring Security configuration;
- login and logout behavior;
- session management;
- CSRF protection;
- conversion between authenticated security principals and domain user identifiers;
- common handling of unauthorized and forbidden requests.

### 7.3 Company Module

Responsibilities:

- company creation;
- company retrieval;
- company membership;
- owner assignment;
- company-scoped authorization;
- listing companies available to the authenticated user.

### 7.4 Common and Configuration

These packages may contain genuinely shared technical concerns such as:

- error-response conventions;
- time configuration;
- persistence configuration;
- request correlation identifiers;
- shared technical utilities.

Business logic must not be moved into `common` merely to avoid choosing the correct module.

## 8. Data Architecture

### 8.1 User

Planned fields:

```text
id
email
password_hash
display_name
status
created_at
updated_at
```

Rules:

- email is normalized before uniqueness checks;
- email is unique among users;
- plaintext passwords are never persisted or logged;
- timestamps are generated consistently;
- user status allows future account disabling without deleting identity records.

### 8.2 Company

Planned fields:

```text
id
name
registration_number
created_at
updated_at
```

The exact validation and uniqueness rules for `registration_number` are not yet defined because country and tax-jurisdiction requirements have not been selected.

### 8.3 CompanyMember

Planned fields:

```text
id
user_id
company_id
role
created_at
```

Rules:

- a membership connects one user to one company;
- `(user_id, company_id)` is unique;
- creating a company also creates an `OWNER` membership in the same transaction;
- company access requires a valid membership;
- initial roles are `OWNER` and `MEMBER`.

### 8.4 Relationship Model

```text
User 1 ---- * CompanyMember * ---- 1 Company
```

This model supports:

- one user belonging to multiple companies;
- one company having multiple users;
- future invitations;
- future company-specific roles;
- tenant-isolation checks.

## 9. Authentication Architecture

### 9.1 Chosen Model

Version 0.1 uses server-side session authentication.

```text
1. Browser submits email and password.
2. Backend verifies the password hash.
3. Backend creates an authenticated server-side session.
4. Browser receives a session identifier in an HTTP-only cookie.
5. Browser sends the cookie with later requests.
6. Backend resolves the session and authenticated user.
```

### 9.2 Cookie Requirements

The implementation must evaluate and configure:

- `HttpOnly`;
- `Secure` in HTTPS environments;
- appropriate `SameSite` behavior;
- a restricted cookie path where practical;
- expiration and idle timeout.

Development settings may differ from production settings, but insecure production defaults must not be copied from local development.

### 9.3 CSRF Protection

Because browser cookies are sent automatically, state-changing requests require CSRF protection.

The exact SPA integration mechanism will be implemented and documented during Version 0.1. Disabling CSRF globally is not an acceptable final design merely to simplify local development.

### 9.4 Password Storage

Passwords must be processed using an established adaptive password-hashing algorithm supported by Spring Security, such as BCrypt or Argon2.

The concrete algorithm and parameters will be recorded when selected during implementation.

### 9.5 Why JWT Is Not Used Initially

JWT is not required for a browser frontend communicating with one Java backend instance.

The session approach allows the project to learn and demonstrate:

- server-side authentication state;
- cookie security;
- CSRF protection;
- session expiration;
- logout invalidation;
- later distributed-session trade-offs.

JWT, Redis-backed sessions, or an external identity provider may be reconsidered when deployment topology or external clients create a real requirement.

## 10. Authorization and Tenant Isolation

Authentication answers:

> Who is the user?

Company authorization answers:

> May this user access this company?

Every company-scoped operation must verify membership on the server.

The backend must not rely on:

- the frontend hiding inaccessible companies;
- a company identifier supplied by the browser;
- ownership assumptions based only on who created the original record;
- client-side roles.

Expected authorization behavior:

```text
Unauthenticated request        -> 401 Unauthorized
Authenticated non-member       -> 403 Forbidden or intentionally hidden 404
Authenticated company member   -> operation continues
```

The final choice between `403` and security-oriented `404` behavior must be consistent and documented during implementation.

## 11. Transaction Boundaries

Important Version 0.1 transaction examples include:

### Company Creation

The following must succeed or fail as one transaction:

1. insert the company;
2. insert the creator's `OWNER` membership.

A company without an owner membership must not remain after a failed request.

### Registration

Registration must handle concurrent attempts to use the same normalized email address. Application-level checks improve error messages, but a database uniqueness constraint remains the final consistency guarantee.

## 12. Database Migrations

Flyway is the authoritative mechanism for schema changes.

Rules:

- schema creation must not depend on Hibernate auto-generation in normal environments;
- committed migrations are immutable after being applied to shared environments;
- new schema changes require new migration files;
- application startup should fail clearly when required migrations cannot be applied;
- test environments must exercise real migrations where practical.

## 13. API Architecture

Version 0.1 uses JSON-based HTTP endpoints.

Expected initial resources:

```text
/api/auth
/api/companies
```

API conventions to define during implementation include:

- validation error format;
- authentication and authorization error format;
- resource identifiers;
- timestamp representation;
- consistent HTTP status codes;
- correlation identifiers for troubleshooting.

OpenAPI documentation will describe the implemented API, but generated documentation does not replace integration tests.

## 14. Configuration and Secrets

Configuration must be externalized.

Expected database settings include equivalents of:

```text
DB_HOST
DB_PORT
DB_NAME
DB_USERNAME
DB_PASSWORD
```

Rules:

- secrets are not committed to Git;
- safe sample configuration may be committed in an `.env.example` or documented environment-variable table;
- production configuration must not be embedded in frontend code;
- local defaults must not silently become production defaults.

## 15. Testing Architecture

Version 0.1 should contain several test layers.

### Unit Tests

Used for isolated business rules such as:

- email normalization;
- company-creation rules;
- role decisions;
- validation helpers.

### Integration Tests

Used for behavior involving:

- PostgreSQL constraints;
- Flyway migrations;
- JPA mappings;
- transactions;
- Spring Security;
- session authentication;
- company isolation.

### API Tests

Used to verify complete request behavior including:

- registration;
- login and logout;
- unauthenticated access;
- authorized company access;
- forbidden cross-company access;
- validation and conflict responses.

### Frontend Verification

At minimum:

- TypeScript compilation;
- production build;
- manual verification of the complete Version 0.1 workflow.

Additional frontend automated tests may be introduced when the UI contains behavior that justifies them.

## 16. Observability in Version 0.1

Version 0.1 does not require a complete monitoring platform, but the application should establish sound logging foundations:

- no passwords or session identifiers in logs;
- structured and understandable application logs;
- clear startup and migration failures;
- useful error context without exposing secrets;
- optional request correlation identifiers.

Metrics, distributed tracing, and centralized log storage are later concerns.

## 17. Version 0.1 Known Limitations

The initial architecture intentionally accepts several limitations:

- one Java backend deployment;
- session state may initially be local to that backend instance;
- no independent backend-module scaling;
- no external identity provider;
- no password recovery or email verification;
- no document processing;
- no cloud deployment;
- minimal roles;
- no distributed tracing;
- no high-availability guarantees.

These are controlled scope decisions, not claims that the limitations are suitable for every production environment.

## 18. Future Evolution

A possible later architecture is:

```text
React Frontend
      |
      v
Java Core Application
      |\
      | +------> PostgreSQL
      | +------> Object Storage / S3
      | +------> Redis, when justified
      |
      +------> Queue or Broker, when justified
                    |
                    v
          Python Document Processing Service
```

Potential evolution includes:

- document binary storage in Amazon S3;
- a Python document-processing service;
- asynchronous processing and retry policies;
- Redis-backed distributed sessions or caching;
- managed PostgreSQL;
- richer RBAC;
- audit history;
- independently deployable Java services;
- centralized observability;
- Kubernetes only when operational complexity and scaling needs justify it.

None of these components should be introduced solely to make the architecture appear more complex.

## 19. Architecture Documentation Rule

After each milestone:

1. update this file to describe what was actually implemented;
2. move speculative components into the future-evolution section;
3. record important deviations and trade-offs in `DECISIONS.md`;
4. keep diagrams consistent with the running system.
