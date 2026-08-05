# Tax Platform Architecture

## 1. Document Status

- **Architecture baseline:** Approved before application development
- **Current approved milestone:** Version 0.1 — Minimal Platform and Deterministic Integration
- **Application implementation:** In Progress
- **Last updated:** 2026-08-05

This document explicitly separates:

- **implemented now** — verified running architecture;
- **approved for Version 0.1** — committed milestone architecture not yet fully implemented;
- **planned for later milestones** — approved evolution that is not yet running;
- **optional future evolution** — possible additions requiring future justification.

The current running subset includes a React registration UI, Java Spring Boot identity and security components, PostgreSQL through Docker Compose, Flyway migrations, and browser-to-database registration. Login, authenticated sessions, companies, documents, processing, incidents, agents, and cloud infrastructure remain unimplemented.

## 2. Architectural Goals

The architecture must:

- deliver a demonstrable vertical scenario after each milestone;
- keep the financial processing core deterministic;
- enforce company isolation in Java;
- represent processing and agent work as persistent state machines;
- allow agents to operate only through narrow, authorized capabilities;
- prevent raw financial data from entering LLM prompts or traces;
- support synthetic reproduction and human-approved repair;
- evolve toward asynchronous processing and cloud deployment without premature infrastructure;
- remain understandable enough to explain during a technical interview.

## 3. System Context

### 3.1 Primary Actors

- **Company user** — registers, signs in, selects a company, uploads a file, and views results.
- **Support specialist** — reviews incidents and agent recommendations; may start or resume an investigation.
- **Developer or team lead** — confirms expected repair behavior and reviews draft pull requests.
- **Domain expert** — provides accounting or tax decisions when a format or rule change has business meaning.
- **External integration provider** — produces files or API responses consumed by deterministic adapters.

### 3.2 Target System Context

```text
+----------------------+             +------------------------+
| Company User         |             | Support / Developer    |
+----------+-----------+             +-----------+------------+
           |                                         |
           | HTTPS                                   | HTTPS
           v                                         v
+----------------------------------------------------------------+
|                         Tax Platform                           |
|                                                                |
|  React UI  <-->  Java Core  <-->  Python Agent Service         |
|                    |                 |                          |
|                    +--> PostgreSQL   +--> Agent State Store     |
|                    +--> Object Store +--> Repair Sandbox        |
+----------------------------------------------------------------+
           |
           | Files / APIs
           v
+-----------------------------+
| External Integration Source |
+-----------------------------+
```

Only a subset of this target exists in each milestone.

## 4. Current and Planned Container Views

### 4.1 Implemented Now

```text
+---------------------------+
| React Frontend            |
|                           |
| - registration form       |
| - validation feedback     |
+-------------+-------------+
              |
              | JSON over HTTP through Vite /api proxy
              | CSRF token and cookie
              v
+---------------------------+
| Java Spring Boot App      |
|                           |
| - registration API        |
| - request validation      |
| - email normalization     |
| - password hashing        |
| - duplicate handling      |
| - CSRF endpoint/security  |
+-------------+-------------+
              |
              | JPA / JDBC
              v
+---------------------------+
| PostgreSQL 17             |
| Docker Compose            |
|                           |
| - flyway_schema_history   |
| - users                   |
+---------------------------+
```

Implemented endpoints:

```text
GET  /api/csrf
POST /api/auth/register
```

Implemented persistence:

- Flyway `V1__baseline.sql`;
- Flyway `V2__create_users.sql`;
- `users` table with unique normalized email, password hash, display name, status, and timestamps;
- JPA `User` entity and `UserRepository`.

Implemented security behavior:

- plaintext passwords are not persisted;
- new passwords use Spring Security's delegating encoder with `{bcrypt}` hashes;
- registration requires a valid CSRF token;
- duplicate email is protected by both an application check and a database uniqueness constraint.

Not implemented in the current runtime:

- login, logout, current-user retrieval, or an authenticated server-side session;
- company membership or tenant authorization;
- document storage, processing jobs, parsers, results, incidents, or structural profiles;
- Python, MCP, Kafka, Redis, S3, AWS, or agent runtimes.

### 4.2 Approved Version 0.1 Container View

```text
+---------------------------+
| React Frontend            |
|                           |
| - authentication UI       |
| - company workspace       |
| - document upload         |
| - job/result/incident UI  |
+-------------+-------------+
              |
              | JSON over HTTP
              | session cookie + CSRF
              v
+---------------------------+
| Java Spring Boot App      |
|                           |
| - identity and security   |
| - company authorization   |
| - document metadata       |
| - processing jobs         |
| - integration parser      |
| - canonical model         |
| - incidents and profiles  |
+----------+----------------+
           |                     \
           | JPA/JDBC             \ storage abstraction
           v                       v
+----------------------+     +--------------------------+
| PostgreSQL           |     | Local File Storage       |
| Docker Compose       |     | Synthetic files only     |
+----------------------+     +--------------------------+
```

Version 0.1 does not include an LLM, Python service, MCP, Kafka, Redis, S3, or AWS deployment.

### 4.3 Planned Version 0.2 Container View

```text
+---------------------------+
| React Frontend            |
+-------------+-------------+
              |
              v
+---------------------------+       MCP / authorized HTTP       +---------------------------+
| Java Spring Boot App      | <-------------------------------> | Python Agent Service      |
|                           |                                    |                           |
| - business source of truth|                                    | - Support Agent           |
| - tenant authorization    |                                    | - agent loop              |
| - Spring AI MCP tools     |                                    | - tool selection          |
| - incidents/known issues  |                                    | - traces and evaluations  |
+-------------+-------------+                                    +-------------+-------------+
              |                                                                |
              v                                                                v
+---------------------------+                                    +---------------------------+
| Java-owned PostgreSQL     |                                    | Agent-owned PostgreSQL    |
| schemas/tables            |                                    | schemas/tables            |
+---------------------------+                                    +---------------------------+
```

For local development, both logical data stores may run in one PostgreSQL container, but they use separate schemas or databases and separate credentials. Python must not query Java-owned business tables directly.

### 4.4 Planned Version 0.3 Repair Environment

```text
+--------------------------+
| Approved Repair Request  |
+------------+-------------+
             |
             v
+--------------------------+       restricted repository tools
| Python Repair Agent      | -------------------------------------+
+------------+-------------+                                      |
             |                                                    v
             | creates                              +----------------------------+
             v                                      | Ephemeral Repair Sandbox   |
+--------------------------+                        |                            |
| Agent Run State          |                        | - repository clone         |
| Approval and Trace       |                        | - isolated branch          |
+--------------------------+                        | - synthetic sample         |
                                                    | - test commands            |
                                                    | - no production secrets    |
                                                    +-------------+--------------+
                                                                  |
                                                                  v
                                                    +----------------------------+
                                                    | Draft Pull Request Provider|
                                                    +----------------------------+
```

## 5. Repository Architecture

The current monorepo contains:

```text
tax-platform/
├── backend/                         # implemented Java/Spring Boot application
├── frontend/                        # implemented React/TypeScript application
├── docker-compose.yml               # implemented PostgreSQL infrastructure
├── .env.example                     # safe local configuration example
├── AGENTS.md
├── CONTRIBUTING.md
├── README.md
├── PROJECT.md
├── ROADMAP.md
├── ARCHITECTURE.md
└── DECISIONS.md
```

Planned additions appear only when their milestone capability is implemented:

```text
tax-platform/
├── agent-service/                   # Version 0.2
├── integration-samples/             # deterministic parser fixtures
└── docs/                             # detailed security, incident, and evaluation documents
```

Monorepo rules:

- each deployable application remains independently buildable;
- service boundaries are enforced through APIs or MCP, not shared repositories or direct table access;
- shared documentation and synthetic fixtures may live at repository level;
- domain libraries are shared only when ownership and versioning are explicit;
- CI should become path-aware as the repository grows.

## 6. Java Core Application

The Java backend is the business source of truth and security boundary.

### 6.1 Current and Planned Module Boundaries

Current base package:

```text
com.poliakov.taxplatform
├── identity             # partially implemented
└── security             # partially implemented
```

Approved Version 0.1 evolution:

```text
com.poliakov.taxplatform
├── identity
├── security
├── company
├── document
├── processing
├── integration
├── incident
├── audit
├── common
└── configuration
```

Planned later additions:

```text
com.poliakov.taxplatform
├── knownissue
└── agentgateway         # introduced with Version 0.2 MCP tools
```

The package structure may be refined, but responsibilities must remain explicit and planned modules must not be described as running before implementation.

### 6.2 Identity Module

Current implementation:

- `User` JPA entity;
- `UserStatus` enum;
- `UserRepository`;
- user registration;
- email trimming, lower-case normalization, and uniqueness;
- secure password hashes;
- user status and timestamps;
- registration request validation;
- duplicate-email conflict response.

Remaining Version 0.1 responsibilities:

- authenticated user profile;
- integration with login and current-user retrieval.

The identity module does not own company authorization.

### 6.3 Security Module

Current implementation:

- Spring Security filter-chain configuration;
- public registration endpoint;
- public CSRF-token endpoint;
- CSRF enforcement for state-changing browser requests;
- delegating password encoder with bcrypt for new passwords;
- disabled form-login page and HTTP Basic authentication.

Remaining Version 0.1 responsibilities:

- login and logout behavior;
- authenticated server-side session creation and invalidation;
- authenticated principal conversion;
- current-user endpoint support;
- HTTP-only authenticated-session cookie settings;
- consistent unauthorized and forbidden error responses.

Service-to-service authentication is planned only when the Python service is introduced.

### 6.4 Company Module

Responsibilities:

- company creation and retrieval;
- explicit company membership;
- owner and member roles;
- tenant-scope authorization;
- reusable authorization policies for documents, incidents, and tools.

Every company-scoped use case starts from an authenticated identity and server-side membership check.

### 6.5 Document Module

Responsibilities:

- company-scoped document metadata;
- original filename and media-type metadata;
- storage key generation;
- checksum and duplicate-detection metadata;
- storage abstraction;
- retention and deletion state;
- prevention of arbitrary filesystem-path access.

The binary is not stored directly in agent prompts or traces.

### 6.6 Processing Module

Responsibilities:

- persistent processing-job lifecycle;
- status transitions;
- timestamps and attempt count;
- correlation and idempotency metadata;
- cancellation and retry metadata;
- result and failure references;
- orchestration of deterministic parsers;
- later event publication through an outbox.

Example early job states:

```text
CREATED
STORED
PROCESSING
SUCCEEDED
FAILED
CANCELLED
RETRY_PENDING
```

### 6.7 Integration Module

Responsibilities:

- integration type detection or explicit selection;
- parser registry;
- parser versioning;
- deterministic file parsing;
- canonical normalized model;
- technical validation rules;
- integration-specific error mapping;
- synthetic fixtures and regression tests.

A parser may inspect raw file bytes inside the deterministic Java processing boundary. This does not grant the LLM access to the file.

### 6.8 Incident Module

Responsibilities:

- structured integration incident creation;
- normalized error codes;
- processing stage;
- parser and deployment metadata references;
- structural file profile;
- retry history;
- incident status;
- links to known issues, support recommendations, tickets, and agent runs;
- safe user-visible and support-visible summaries.

### 6.9 Known Issue Module

Responsibilities:

- known issue summaries;
- affected integration and parser versions;
- approved workaround;
- fixed version;
- status and validity period;
- support-safe text exposed to the agent.

### 6.10 Audit Module

Responsibilities:

- security-relevant and state-changing audit events;
- actor and service identity;
- company scope;
- correlation identifiers;
- approval and retry decisions;
- agent action references;
- redaction policy.

### 6.11 Agent Gateway Module

Introduced in Version 0.2.

Responsibilities:

- Spring AI MCP server configuration;
- narrow tool definitions;
- service authentication and authorization;
- tenant and incident scope validation;
- sanitized response DTOs;
- tool audit records;
- idempotency for state-changing tools;
- prevention of direct database or arbitrary file access.

## 7. Core Data Model Direction

### 7.1 Identity and Tenant Data

Implemented now:

```text
User
- id
- email
- password_hash
- display_name
- status
- created_at
- updated_at
```

Current constraints:

- normalized `email` is unique;
- `password_hash` is required and plaintext passwords are never stored;
- `status` is restricted to approved enum values;
- timestamps are required.

Approved for later in Version 0.1:

```text
Company
- id
- name
- registration_number (synthetic or optional in demo)
- created_at
- updated_at

CompanyMember
- id
- user_id
- company_id
- role
- created_at
```

`(user_id, company_id)` will be unique. Company and membership tables are not implemented yet.

### 7.2 Document and Processing Data

```text
Document
- id
- company_id
- storage_key
- original_filename
- content_type
- size_bytes
- checksum
- integration_type
- upload_status
- created_by
- created_at

ProcessingJob
- id
- company_id
- document_id
- status
- parser_type
- parser_version
- attempt_count
- idempotency_key
- correlation_id
- started_at
- completed_at
- failure_code
- created_at
- updated_at

ProcessingResult
- id
- job_id
- canonical_schema_version
- normalized_summary
- warning_codes
- created_at
```

The exact normalized record schema is defined with the first synthetic integration.

### 7.3 Incident and Structural Profile Data

```text
IntegrationIncident
- id
- company_id
- job_id
- status
- normalized_error_code
- processing_stage
- classification
- known_issue_id
- retry_eligibility
- created_at
- updated_at

StructuralFileProfile
- id
- incident_id
- format
- encoding
- delimiter
- sheet_count
- visible_sheet_count
- hidden_sheet_count
- row_count_summary
- column_count_summary
- header_names_or_hashes
- detected_data_types
- reporting_year_count
- requested_year_present
- warning_codes
- profile_version
- created_at
```

A structural profile contains schema-level and aggregate metadata, not financial row values.

### 7.4 Agent Service Data

Owned by the Python service:

```text
AgentRun
- id
- agent_type
- external_incident_id
- status
- model
- prompt_version
- started_at
- completed_at
- token_usage
- estimated_cost
- final_classification
- error_summary

AgentStep
- id
- agent_run_id
- sequence_number
- step_type
- sanitized_summary
- started_at
- completed_at

ToolExecution
- id
- agent_step_id
- tool_name
- sanitized_arguments
- sanitized_result_summary
- status
- duration_ms
- error_code

AgentApproval
- id
- agent_run_id
- approval_type
- requested_from
- status
- decision_summary
- decided_at

AgentEvaluation
- id
- agent_type
- scenario_id
- model
- prompt_version
- result
- score
- metrics
- trace_reference
- created_at
```

The Python service may store opaque Java incident identifiers but not copy unrestricted business records.

## 8. Deterministic Processing Flow

### 8.1 Approved Version 0.1 Flow

```text
1. Browser uploads a company-scoped synthetic file.
2. Java authenticates the user and verifies company membership.
3. Java validates metadata and stores the binary through DocumentStorage.
4. Java creates a persistent ProcessingJob.
5. Java selects integration type and parser version.
6. The deterministic parser reads the file using bounded-memory techniques appropriate to the format.
7. The parser maps records to the canonical model.
8. Technical validations run.
9a. On success, Java persists a ProcessingResult and marks the job SUCCEEDED.
9b. On failure, Java maps the error, creates a StructuralFileProfile and IntegrationIncident, and marks the job FAILED.
10. The UI polls or requests status and displays the result.
```

### 8.2 Large-File Evolution

Version 0.1 may use small files, but the design must avoid assuming that all uploads remain in memory.

Planned evolution includes:

- streamed or direct-to-object-storage uploads;
- file size and checksum validation;
- streaming parsers where the format supports them;
- batch persistence;
- progress checkpoints;
- persistent cancellation and retry state;
- duplicate upload prevention;
- partial-failure semantics;
- bounded worker concurrency;
- asynchronous processing;
- correlation and audit across retries.

These are design constraints, not claims of Version 0.1 implementation.

## 9. Support Investigation Agent

### 9.1 Purpose

The Support Investigation Agent is the first agent and is planned for Version 0.2.

It investigates a structured incident, gathers evidence, classifies the failure, and proposes a safe next action. It does not modify code.

### 9.2 Approved Classifications

```text
USER_INPUT_ERROR
USER_MODIFIED_FILE
WRONG_REPORTING_PERIOD
UNSUPPORTED_FORMAT
KNOWN_ISSUE
KNOWN_PRODUCT_DEFECT
TRANSIENT_INFRASTRUCTURE_FAILURE
PARSER_REGRESSION
NEW_INTEGRATION_VARIANT
EXTERNAL_API_CONTRACT_CHANGE
UNKNOWN_REQUIRES_HUMAN
```

### 9.3 Allowed Evidence

- incident metadata;
- integration type and parser version;
- processing stage;
- normalized error code;
- redacted stack trace;
- processing-job status and retry history;
- structural file profile;
- schema and header information without row values;
- sheet, row, and column counts;
- detected types and reporting-year counts;
- warning codes;
- recent deployment metadata;
- known issue and similar incident summaries;
- integration documentation;
- synthetic reproduction metadata.

### 9.4 Forbidden Evidence

- raw customer financial files;
- financial amounts;
- bank accounts or personal identifiers;
- real company names or registration numbers;
- complete production logs;
- credentials or secrets;
- database dumps;
- unrestricted internal documents.

### 9.5 Narrow Tools

Initial tool contracts include:

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
request_repair_approval
escalate_to_human
```

There is no arbitrary SQL, shell, production-file, deployment, merge, or financial-data mutation tool.

### 9.6 Agent Loop

```text
Load run state
    |
    v
Read incident summary
    |
    v
Choose next allowed tool based on current evidence
    |
    v
Validate tool request in Java
    |
    v
Persist tool result and update run state
    |
    +--> enough evidence: classify and propose action
    +--> missing business decision: request human input
    +--> forbidden or unsafe request: stop and record policy failure
    +--> recoverable tool error: retry within bounded policy
```

The stored reasoning is a concise evidence and decision summary, not private chain-of-thought content.

## 10. Integration Repair Agent

### 10.1 Preconditions

A repair run starts only when:

1. Support Agent or a human creates a structured engineering ticket.
2. The expected behavior is explicitly approved.
3. A synthetic reproduction exists.
4. A sandbox and isolated branch are available.

### 10.2 Inputs

- approved ticket;
- approved expected behavior;
- structural profile;
- synthetic reproduction;
- integration and parser documentation;
- repository clone;
- existing tests;
- build commands;
- coding conventions and architecture restrictions.

### 10.3 Restricted Tools

```text
list_repository_files
search_code
read_file
read_test_file
inspect_build_configuration
apply_patch
create_file
run_parser_reproduction
run_targeted_tests
run_module_tests
run_full_test_suite
get_git_diff
revert_changes
commit_changes
create_draft_pull_request
request_human_input
```

The sandbox exposes only approved commands and paths. `run_*` tools are typed wrappers, not unrestricted shell access.

### 10.4 Repair Flow

```text
Approved ticket
    |
    v
Create sandbox and branch
    |
    v
Run baseline tests and synthetic reproduction
    |
    v
Inspect relevant code and tests
    |
    v
Produce implementation plan
    |
    +--> behavior unclear: request human input and pause
    |
    v
Apply focused patch and regression tests
    |
    v
Run targeted tests -> module tests -> full suite
    |
    +--> failure: revise or revert within bounded attempts
    |
    v
Inspect diff and architecture restrictions
    |
    v
Create risk summary and draft pull request
    |
    v
Human review
```

The agent never merges or deploys.

## 11. MCP and Spring AI Boundary

### 11.1 Purpose

Spring AI is used meaningfully in the Java backend to expose narrow operational and business capabilities as MCP tools. Python uses those tools rather than receiving database credentials or a general-purpose internal API.

### 11.2 Tool Security Requirements

Every tool invocation must:

- authenticate the calling service and run identity;
- validate the incident and company scope;
- verify the caller has the required tool permission;
- validate arguments against a schema;
- enforce rate and size limits;
- retrieve only the minimum data required;
- redact sensitive values;
- record an audit event;
- apply idempotency to state-changing operations;
- return a typed error rather than an unrestricted exception dump.

### 11.3 Trust Model

- Java tools are trusted to enforce policy.
- Python orchestration is not trusted to bypass Java checks.
- The LLM is untrusted and may propose invalid or malicious tool arguments.
- Document-derived text is untrusted data and cannot change tool permissions or system policy.

## 12. Persistent Agent State and Status Model

The planned cross-system lifecycle includes:

```text
DETECTED
INVESTIGATING
USER_ACTION_REQUIRED
KNOWN_ISSUE
RETRY_AVAILABLE
ESCALATED_TO_SUPPORT
ENGINEERING_CHANGE_REQUIRED
TICKET_CREATED
WAITING_FOR_REPAIR_APPROVAL
REPAIR_RUNNING
HUMAN_INPUT_REQUIRED
REPAIR_FAILED
DRAFT_PR_CREATED
PR_REVIEWED
FIX_DEPLOYED
INCIDENT_RESOLVED
```

Java owns incident business status. Python owns detailed agent execution state. Updates cross the service boundary through authorized APIs/MCP and, later, versioned events.

Restart behavior:

- a run resumes from persisted state or moves to an explicit recoverable-failure status;
- duplicate tool execution is prevented or made idempotent;
- a human can inspect the last completed step;
- no active run silently disappears.

## 13. Event-Driven Future

Kafka is planned for Version 0.4, not Version 0.1 or 0.2.

### 13.1 Candidate Events

```text
DocumentUploaded
ProcessingStarted
ProcessingFailed
IntegrationIncidentCreated
InvestigationRequested
InvestigationCompleted
RepairApprovalRequested
RepairApproved
DraftPullRequestCreated
FixDeployed
ReprocessingRequested
IncidentResolved
```

### 13.2 Planned Delivery Pattern

```text
Java transaction
    |
    +--> business state
    +--> outbox row
             |
             v
        outbox publisher
             |
             v
           Kafka
             |
             v
    idempotent consumer
```

Required characteristics:

- event versioning;
- correlation and causation IDs;
- at-least-once delivery assumptions;
- idempotent consumers;
- bounded concurrency;
- retry and dead-letter policy;
- observable lag;
- safe replay procedures.

## 14. Storage Evolution

### 14.1 Version 0.1

- local filesystem implementation behind `DocumentStorage`;
- synthetic files only;
- generated storage keys;
- metadata and authorization in PostgreSQL.

### 14.2 Version 0.5

- Amazon S3 implementation;
- private objects;
- tenant-aware object key policy;
- pre-signed upload when large-file requirements justify it;
- retention and cleanup rules;
- no raw file transfer to the LLM.

The storage abstraction must prevent the rest of the domain from depending on local filesystem paths or S3-specific details.

## 15. Authentication and Authorization

### 15.1 Browser Authentication

Current implemented registration flow:

```text
Browser requests CSRF token
        |
        v
Browser submits registration data and CSRF header
        |
        v
Java validates and normalizes input
        |
        v
PasswordEncoder creates a {bcrypt} hash
        |
        v
User is persisted in PostgreSQL
```

Implemented security properties:

- registration is public but CSRF-protected;
- passwords are verified only through a password encoder and are never returned;
- duplicate email is rejected without storing another account;
- the React frontend uses the Vite `/api` proxy during local development.

Approved next authentication flow:

```text
Credentials -> password verification -> server session -> HTTP-only cookie
```

Login, logout, current-user retrieval, authenticated session creation, session invalidation, and production cookie settings are not implemented yet.

State-changing requests continue to require CSRF protection. Production cookie configuration must evaluate `Secure`, `SameSite`, path, idle timeout, and expiration.

### 15.2 Company Authorization

Authentication answers who the user is. Company authorization determines whether the user may access a tenant-scoped resource.

Every document, job, incident, known issue link, support recommendation, and approval operation resolves company scope server-side and checks membership.

### 15.3 Service Authorization

The Python service receives a dedicated service identity and scoped tool permissions. A service credential does not automatically grant access to every incident or company. Tool requests must carry or resolve an authorized run and incident scope.

## 16. Retry, Idempotency, and Duplicate Handling

LLMs do not decide retry safety.

The Java backend owns deterministic retry eligibility based on:

- operation idempotency;
- prior side effects;
- partial result cleanup;
- attempt limits;
- current job state;
- integration-specific constraints.

The agent may call `check_retry_eligibility` and present the returned decision. A retry tool, when introduced, must reject unsafe or stale requests.

Duplicate uploads and repeated requests should use checksums, idempotency keys, and explicit business rules rather than silent duplicate processing.

## 17. Error Architecture

Errors are structured and stable enough for UI, support, and agent use.

A planned error response includes:

```text
error_code
message_code
category
correlation_id
field_errors
incident_id (when created)
retryable (deterministic value)
```

Internal traces are redacted before agent exposure. User messages are localizable and must not leak stack traces, storage paths, SQL, or secrets.

## 18. Testing Architecture

### 18.1 Java

Implemented tests currently include:

- Spring Boot context startup;
- PostgreSQL-backed `UserRepository` persistence and lookup;
- database enforcement of unique email;
- successful registration API behavior;
- password hash persistence and password matching;
- duplicate registration conflict response;
- rejection of registration without CSRF;
- transactional rollback of registration-test data.

Approved Version 0.1 test growth includes:

- login, logout, current-user, session, and unauthorized-access tests;
- transaction and uniqueness tests;
- company-isolation tests;
- document-storage contract tests;
- processing-job lifecycle tests;
- parser fixture and regression tests;
- incident and structural-profile tests;
- MCP tool authorization and redaction tests when introduced.

### 18.2 Frontend

Implemented verification:

- ESLint;
- TypeScript production compilation through the Vite build;
- production build;
- manual browser verification of registration, duplicate feedback, CSRF flow, and persisted user data.

Focused automated component or end-to-end tests may be added when frontend behavior justifies them. The remaining Version 0.1 UI must cover login, session state, company selection, upload, processing status, success, and failure.

### 18.3 Python Agents

- unit tests for orchestration and tool adapters;
- persistence and resume tests;
- mocked and controlled LLM tests;
- formal Support and Repair evaluation suites;
- forbidden-tool and sensitive-data assertions;
- deterministic replay where possible.

See [docs/EVALUATION_STRATEGY.md](docs/EVALUATION_STRATEGY.md).

## 19. Observability

### 19.1 Early Foundation

Version 0.1 should establish:

- structured logs;
- correlation identifiers;
- no passwords, session IDs, file contents, or secrets in logs;
- clear migration and startup failures;
- job duration and status transitions.

### 19.2 Agent Observability

Versions 0.2 and 0.3 add:

- run and step timing;
- tool success and failure counts;
- model and prompt versions;
- token usage and estimated cost;
- final classification;
- approval wait time;
- test execution results;
- sanitized trace summaries.

### 19.3 Later Operations

Centralized logs, metrics, dashboards, alerts, Kafka lag, S3 events, and service health are introduced with cloud and reliability milestones.

## 20. Deployment Evolution

### 20.1 Version 0.1 Local Model

Current implemented local runtime:

```text
Developer machine
├── React Vite development server
│   └── /api proxy to localhost:8080
├── Java Spring Boot application
└── Docker Compose
    └── PostgreSQL 17 with named volume
```

Current startup commands are documented in `README.md`.

Approved later Version 0.1 additions:

- local filesystem document storage behind an application abstraction;
- document-processing and incident components inside the Java application.

### 20.2 Version 0.2 Local Distributed Model

Docker Compose may run:

- PostgreSQL;
- Java backend;
- Python agent service;
- optional local MCP networking configuration;
- optional local object storage only if it simplifies testing.

### 20.3 Version 0.4 Event-Driven Model

Kafka and related local infrastructure are added only when the event flow is implemented.

### 20.4 Version 0.5 AWS Model

Possible components include:

- independent Java and Python compute;
- managed PostgreSQL;
- Amazon S3;
- secrets management;
- centralized logs and metrics;
- CI/CD deployment.

The exact services are selected during cloud milestone planning.

### 20.5 Optional Kubernetes Evolution

Kubernetes is not required for early milestones. It may be added when there are multiple independently deployable services and a concrete orchestration, scaling, or deployment-learning objective.

## 21. Security Boundaries Summary

```text
Browser
  | untrusted input
  v
Java authorization boundary
  | sanitized, scoped MCP tools
  v
Python agent orchestration
  | restricted prompts and tools
  v
External LLM

Repair Agent
  | approved ticket only
  v
Ephemeral repository sandbox
  | draft changes only
  v
Human code review
```

Key rules:

- raw user files stay outside the model boundary;
- Java rechecks all tenant and tool permissions;
- Python does not share Java database credentials;
- repository sandboxes contain no production secrets;
- agents cannot merge or deploy;
- prompt injection from documents is treated as untrusted content;
- model output is validated and never directly becomes financial truth.

See [docs/SECURITY_AND_DATA_BOUNDARIES.md](docs/SECURITY_AND_DATA_BOUNDARIES.md).

## 22. Architecture Documentation Rule

After each milestone:

1. change planned sections to implemented only after verification;
2. remove or clearly mark abandoned design assumptions;
3. update all diagrams and repository trees;
4. record significant changes in `DECISIONS.md`;
5. keep milestone names identical to `ROADMAP.md` and `README.md`;
6. preserve a clear distinction between current and future components.
