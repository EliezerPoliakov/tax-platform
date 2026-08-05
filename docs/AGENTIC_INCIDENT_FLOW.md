# Agentic Incident and Repair Flow

## 1. Document Status

- **Application implementation:** Not started
- **Support Agent milestone:** Version 0.2 — Planned
- **Repair Agent milestone:** Version 0.3 — Planned
- **Last updated:** 2026-08-05

This document defines the approved end-to-end incident investigation and repair lifecycle. It does not claim that the agents or tools are currently implemented.

## 2. Purpose

Financial integrations fail for many reasons that are difficult to encode as a complete static rule set:

- the wrong file is uploaded;
- the requested reporting period is absent;
- headers or sheets are modified;
- encoding, delimiter, date, decimal, or enum formats change;
- an integration provider adds or removes fields;
- an API contract changes;
- a parser regresses after deployment;
- storage or workers fail transiently;
- several users encounter a new variant;
- a known workaround exists but support does not recognize it.

The deterministic parser remains responsible for financial data processing. Agents investigate and coordinate operational resolution after a structured failure exists.

## 3. High-Level Flow

```text
Deterministic processing failure
        |
        v
Integration incident created
        |
        v
Support Investigation Agent
        |
        +--> user guidance
        +--> known issue and workaround
        +--> deterministic retry recommendation
        +--> human escalation
        +--> engineering ticket
                     |
                     v
              human approval
                     |
                     v
             synthetic reproduction
                     |
                     v
           Integration Repair Agent
                     |
                     v
       focused patch and regression tests
                     |
                     v
               draft pull request
                     |
                     v
                human review
```

## 4. Preconditions from the Deterministic Platform

Version 0.1 must provide:

- an authenticated company-scoped upload;
- document metadata and storage reference;
- persistent processing job;
- integration type and parser version;
- normalized error code;
- processing stage;
- redacted error trace or technical summary;
- structural file profile without financial row values;
- integration incident identifier;
- retry history fields;
- at least one synthetic failure fixture.

The agent flow must not be built around a free-form user message alone.

## 5. Incident Inputs

### 5.1 Allowed Inputs

The Support Agent may receive or retrieve:

- incident ID and status;
- company-scoped opaque identifiers;
- integration type;
- parser type and version;
- processing stage;
- normalized error code;
- redacted stack trace;
- processing-job status;
- attempt and retry history;
- structural file profile;
- schema and header information without row values;
- sheet count and hidden-sheet count;
- row and column counts;
- detected data types;
- number of distinct reporting years;
- whether the requested year is present;
- encoding and delimiter;
- warning codes;
- recent deployment metadata;
- known issue summaries;
- similar incident summaries;
- integration documentation;
- synthetic reproduction metadata.

### 5.2 Forbidden Inputs

The agent must not receive:

- raw customer files;
- financial row values or amounts;
- real company names or registration numbers;
- bank accounts;
- personal identifiers;
- complete production logs;
- credentials, tokens, or secrets;
- database dumps;
- arbitrary internal documents;
- unrestricted repository or production filesystem access.

## 6. Support Investigation Agent

### 6.1 Goal

The Support Investigation Agent gathers evidence, classifies the incident, and selects a safe next action.

It must not change code, financial data, production configuration, or deployment state.

### 6.2 Classifications

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

The classification must be supported by cited tool evidence in the stored decision summary.

### 6.3 Tool Set

Initial read tools:

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
```

Initial action tools:

```text
create_support_recommendation
create_engineering_ticket
request_repair_approval
escalate_to_human
```

Action tools are typed, authorized, idempotent where necessary, and audited.

### 6.4 Investigation Loop

```text
Start or resume run
    |
    v
Read incident summary
    |
    v
Select the next tool needed to reduce uncertainty
    |
    v
Java validates service identity, run, incident, company scope, and arguments
    |
    v
Persist sanitized tool outcome
    |
    +--> sufficient evidence: classify
    +--> missing evidence: call another allowed tool
    +--> business behavior unclear: escalate or request approval input
    +--> unsafe or forbidden request: stop with policy failure
    +--> temporary tool failure: bounded retry or explicit pause
```

The agent has a maximum turn and tool-call budget. Exhaustion produces `UNKNOWN_REQUIRES_HUMAN` or an explicit failed state rather than an infinite loop.

### 6.5 Evidence Summary

The final result contains:

- classification;
- confidence or evidence-quality indicator;
- concise evidence list;
- contradictions or missing evidence;
- proposed next action;
- user- or support-facing message code;
- ticket or known issue link when applicable;
- retry eligibility copied from deterministic backend logic;
- reason for escalation when unresolved.

It does not expose private chain-of-thought. It provides a concise, reviewable explanation based on tool outputs.

## 7. Support Outcomes

### 7.1 User Input Error

Examples:

- wrong file type;
- required sheet absent;
- header starts on an unexpected row;
- file was manually modified;
- requested reporting year is absent.

Outcome:

- support recommendation;
- localizable message code;
- evidence summary;
- no engineering ticket when product behavior is correct.

### 7.2 Known Issue

Outcome:

- link incident to known issue;
- display approved workaround;
- show affected and fixed parser versions;
- avoid duplicate engineering ticket creation.

### 7.3 Infrastructure Failure

The agent calls `check_retry_eligibility`.

A retry is recommended only when Java confirms:

- the operation is idempotent;
- no unsafe partial result remains;
- attempt limits allow another execution;
- the current job state permits retry.

The LLM does not infer retry safety.

### 7.4 New Integration Variant or Parser Regression

Outcome:

- collect structural difference evidence;
- identify affected parser and version;
- check similar incidents and deployments;
- create a structured engineering ticket;
- identify unresolved business questions;
- request human approval before repair.

### 7.5 Unknown Case

Outcome:

- preserve all gathered evidence;
- explain what remains unknown;
- escalate to a human;
- avoid speculative ticket content or repair.

## 8. Engineering Ticket Contract

The initial provider abstraction is:

```text
TicketProvider
├── InternalTicketProvider       # first implementation
└── JiraTicketProvider           # later adapter
```

A ticket includes:

- incident identifier;
- integration and parser version;
- observed behavior;
- expected behavior, if already approved;
- normalized error and processing stage;
- structural evidence;
- similar incidents and known issue checks;
- reproduction status;
- business questions;
- security classification;
- confirmation that raw financial data is not attached;
- acceptance criteria for repair.

Ticket creation must be idempotent for the same incident and repair reason.

## 9. Repair Approval

Repair approval is a persistent state transition, not a chat confirmation.

Required approval data:

- approving human identity;
- approved expected behavior;
- scope and non-scope;
- approved synthetic reproduction;
- affected integration and parser;
- architecture constraints;
- required test levels;
- decision timestamp;
- optional expiration or revocation.

Possible states:

```text
NOT_REQUESTED
PENDING
APPROVED
CHANGES_REQUESTED
REJECTED
REVOKED
```

A rejected or revoked approval prevents repair execution.

## 10. Synthetic Reproduction

A synthetic reproduction must:

- contain no copied customer values;
- reproduce the structural cause;
- be small enough for deterministic tests;
- document how it differs from the supported format;
- include the requested reporting period or other relevant conditions;
- define the expected parser result or error;
- be approved before the Repair Agent uses it.

Example multi-year scenario:

```text
Supported assumption: 1-2 reporting years
Observed structure: 10 reporting years
Approved behavior:
- process requested year and previous year;
- ignore unrelated years;
- emit explicit warning;
- fail if requested year is missing.
```

## 11. Integration Repair Agent

### 11.1 Goal

Prepare a focused, tested code change for an approved integration defect or variant.

### 11.2 Inputs

- approved ticket;
- approved expected behavior;
- structural profile;
- synthetic reproduction;
- parser documentation;
- repository clone;
- existing tests;
- build commands;
- coding conventions;
- architecture restrictions.

### 11.3 Sandbox Lifecycle

1. Allocate an ephemeral container or sandbox.
2. Clone the repository at the approved base revision.
3. Create a branch such as `ai-fix/INC-1042-priority-multi-year`.
4. Inject only approved synthetic fixtures.
5. provide no production credentials or customer data.
6. Run baseline tests and reproduction.
7. Restrict filesystem paths, commands, network, and runtime duration.
8. Destroy or clean the sandbox according to retention policy after the run.

### 11.4 Repair Tools

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

`run_*` tools invoke approved command templates. They are not arbitrary shell access.

### 11.5 Repair Sequence

1. Verify approval and sandbox.
2. Run the existing reproduction and baseline tests.
3. Locate the parser, exception mapping, and existing tests.
4. Read only relevant files.
5. Produce a concise implementation plan.
6. Request human input if expected behavior is incomplete.
7. Apply a focused patch.
8. Add regression tests.
9. Run the reproduction and targeted tests.
10. Run parser or module tests.
11. Run the full backend test suite.
12. Inspect the diff for unrelated changes and architecture violations.
13. Revert or revise within bounded attempts when checks fail.
14. Produce a risk summary.
15. Create a draft pull request or local draft equivalent.

### 11.6 Stop Conditions

The Repair Agent stops and requests human input when:

- expected behavior is missing;
- the synthetic reproduction does not match the incident evidence;
- the issue requires accounting or tax interpretation;
- the patch would weaken tenant isolation;
- a new dependency lacks justification;
- the change requires unrelated refactoring;
- tests reveal a larger architecture problem;
- required tests cannot be run;
- production data or credentials would be needed.

## 12. Draft Pull Request Contract

The provider abstraction is:

```text
PullRequestProvider
├── LocalDraftProvider
└── GitHubPullRequestProvider     # later
```

The draft contains:

- incident and ticket references;
- root cause;
- approved expected behavior;
- changed files;
- implementation summary;
- regression coverage;
- baseline and final test results;
- known risks;
- architecture and security impact;
- confirmation that no production data or secrets were used;
- explicit AI-generated status;
- human-review requirement.

The agent cannot change draft status to approved, merge, or deploy.

## 13. Human Review Outcomes

```text
APPROVED
CHANGES_REQUESTED
REJECTED
TESTS_ACCEPTED_IMPLEMENTATION_REWRITTEN
IMPLEMENTATION_ACCEPTED_TESTS_REVISED
SUPERSEDED_BY_MANUAL_FIX
```

The selected outcome is persisted and linked to the incident and agent run.

## 14. Incident and Agent States

Platform-facing states:

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

Agent-service execution states may be more detailed, but they must map to these stable business states.

## 15. Failure and Recovery

### 15.1 Java or MCP Tool Failure

- persist the failed tool execution;
- use bounded retries only for retryable technical failures;
- never retry a state-changing tool without idempotency;
- pause or escalate after budget exhaustion.

### 15.2 Python Service Restart

- reload active runs from persistent state;
- resume from the last confirmed step;
- prevent duplicate state-changing calls;
- mark unrecoverable runs explicitly.

### 15.3 LLM Provider Failure

- record provider and error category;
- apply bounded retry and timeout policy;
- support manual resume or alternate approved model;
- do not lose incident evidence or approvals.

### 15.4 Sandbox Failure

- persist logs and sanitized failure summary;
- do not create a draft PR when required tests did not run;
- clean up the branch or preserve it according to policy;
- allow a human to retry from an approved checkpoint.

## 16. Example: Ten-Year Integration Variant

```text
1. User uploads a synthetic Priority-like export.
2. Java stores the document and creates a job.
3. Parser detects ten reporting years instead of the supported one or two.
4. Java records a normalized error, structural profile, parser version, stage, and incident.
5. Support Agent reads incident, profile, parser metadata, known issues, and similar incidents.
6. It classifies NEW_INTEGRATION_VARIANT.
7. It creates a ticket noting that requested and previous years are present and that unrelated years exist.
8. A human approves: process requested and previous year, ignore the rest, emit warning.
9. A synthetic ten-year file is approved.
10. Repair Agent reproduces the failure in an isolated branch.
11. It changes the parser and adds tests for one year, two years, ten years, and missing requested year.
12. It runs targeted, module, and full backend tests.
13. It creates a draft pull request with risks and test evidence.
14. A developer reviews and merges manually.
15. Normal CI/CD deploys the change.
16. Reprocessing is offered only when Java confirms idempotency.
```

## 17. Audit Requirements

Persist:

- who started or approved a run;
- agent type, model, and prompt version;
- incident and ticket references;
- tool calls and sanitized inputs/outputs;
- errors, timing, tokens, and cost;
- classification and proposed action;
- approval decisions;
- test execution results;
- draft PR reference;
- human review outcome;
- resolution and reprocessing decision.

Do not persist secrets, raw financial files, or private chain-of-thought.
