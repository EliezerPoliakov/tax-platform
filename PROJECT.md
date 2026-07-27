# Tax Platform Project

## 1. Document Purpose

This document is the stable project charter for the Tax Platform.

It defines why the project exists, the business direction, the guiding principles, the intended boundaries, and the definition of success.

The current implementation plan is maintained in [ROADMAP.md](ROADMAP.md). The current technical design is maintained in [ARCHITECTURE.md](ARCHITECTURE.md), and significant architectural choices are recorded in [DECISIONS.md](DECISIONS.md).

## 2. Project Vision

This project is not intended to be a tutorial CRUD application or a complete rewrite of the existing RubyTax system.

The goal is to build a small but realistic modern financial platform that demonstrates the engineering practices and architectural approaches used in production systems.

The project serves three purposes:

1. Build a complete, presentable product that can be demonstrated to potential employers.
2. Learn modern technologies through practical implementation rather than theory alone.
3. Practice making architectural decisions in the same way they are made in commercial software development.

## 3. Core Business Workflow

The project focuses on one complete financial workflow instead of many unrelated or unfinished use cases:

1. A user signs in.
2. The user selects a company.
3. The user uploads a financial document.
4. The document is stored.
5. Document processing is started.
6. The user receives the processing result.
7. The resulting data can be viewed and edited.
8. A final report is generated.

Additional workflows may be considered later, but they are not part of the initial scope.

## 4. Guiding Principles

### 4.1 Production First

Every completed version of the project must remain functional.

The project should not spend months on infrastructure without producing a working result. After every iteration, the system should be in a state that can be demonstrated during an interview.

### 4.2 Real Business Flow

Development is driven by a realistic end-to-end financial scenario rather than isolated technical exercises.

The business workflow is the foundation of the system. Technologies and architectural decisions are introduced in support of that workflow.

### 4.3 Technology Must Solve a Problem

A technology must not be added simply because it is popular.

Each technology should be introduced only when it addresses a real requirement. For example:

- **Flyway** — database schema migration management.
- **Redis** — faster access to frequently read data or shared session storage when required.
- **Kafka** — event exchange when asynchronous and event-driven communication is justified.
- **Amazon S3** — durable object storage for financial documents.
- **Docker** — consistent development and runtime environments.
- **Kubernetes** — service orchestration and scaling when simpler deployment models are insufficient.
- **AWS** — realistic cloud infrastructure when a stable application is ready to deploy.

### 4.4 Learn by Building

Technologies are learned while developing the system.

The goal is not merely to watch tutorials, but to implement working scenarios independently and understand the decisions behind them.

### 4.5 Small but Complete

One fully implemented workflow is more valuable than ten unfinished ones.

The project should look and behave like a small commercial product rather than a collection of disconnected experiments.

### 4.6 Current Reality Must Be Clear

Documentation must distinguish between:

- what is implemented now;
- what is approved for the current milestone;
- what is only a possible future direction.

Planned technologies must not be presented as already running parts of the system.

## 5. Non-Goals

The project does not attempt to:

- fully reproduce RubyTax;
- implement every tax form;
- support every reporting year;
- handle millions of users from the first version;
- contain dozens of microservices;
- introduce infrastructure solely to make the project appear complex.

Simplifications are acceptable where they do not prevent meaningful architectural learning.

## 6. Target System Direction

By the end of the project, the goal is to have a small modern platform that may include:

- a Java and Spring Boot core application;
- PostgreSQL;
- Flyway;
- Docker;
- AWS infrastructure;
- Amazon S3;
- Redis where caching or distributed sessions justify it;
- Kafka or another asynchronous mechanism where processing requirements justify it;
- Spring Security;
- role-based access control;
- React and TypeScript frontend;
- a separately deployable Python service for document processing or AI-related work;
- AI integration where it provides real value;
- CI/CD;
- monitoring and logging;
- automated tests.

These technologies will not be introduced all at once. Each one should be added only when the system has a concrete need for it.

The target system is expected to become a small distributed platform, but it will not begin as a collection of empty microservices. The Java core will start as one deployable application with clear internal modules, while specialized services will be introduced incrementally.

## 7. Development Approach

Development is organized into small iterations.

Each iteration must:

- have a clear user or engineering goal;
- define explicit scope and non-scope;
- finish with a working version;
- include automated verification appropriate to the change;
- update documentation to match the implemented system.

The authoritative milestone plan, statuses, implementation sequence, and completion criteria are maintained in [ROADMAP.md](ROADMAP.md).

## 8. Architectural Approach

Throughout development, the project should continuously address production-level questions:

- What happens when the load increases?
- How is the system secured?
- How are company boundaries enforced?
- How are documents stored?
- How is caching organized?
- How are database migrations managed?
- How is auditing implemented?
- How is idempotency guaranteed?
- How do queues and asynchronous processing work?
- How are services scaled?
- How is fault tolerance achieved?
- How are configuration and secrets managed?
- How is the system observed and diagnosed?

A capability does not need to be implemented immediately, but its architectural implications should be understood when it becomes relevant.

The current architecture is documented in [ARCHITECTURE.md](ARCHITECTURE.md). Significant choices and trade-offs are recorded in [DECISIONS.md](DECISIONS.md).

## 9. Use of AI

AI is a development tool, not a replacement for the developer.

The developer remains responsible for the main architectural decisions and must understand the resulting implementation.

AI may be used for:

- architecture discussions;
- code review;
- accelerating routine code implementation;
- test generation;
- assistance with unfamiliar technologies;
- documentation support;
- selected features inside the platform itself.

AI-generated code or advice must be reviewed, tested, and understood before it becomes part of the project.

## 10. Documentation Responsibilities

The project uses the following documentation model:

- [README.md](README.md) — public entry point, current status, verified setup, and links.
- [PROJECT.md](PROJECT.md) — stable vision, principles, boundaries, and success criteria.
- [ROADMAP.md](ROADMAP.md) — current work, milestone scope, statuses, and completion criteria.
- [ARCHITECTURE.md](ARCHITECTURE.md) — the approved architecture plan and, as development progresses, the architecture actually implemented.
- [DECISIONS.md](DECISIONS.md) — significant architectural decisions and trade-offs.

Documentation must be updated after meaningful architectural changes and milestone completion. It does not need to change after every small code edit.

## 11. Definition of Success

The success of the project is not measured by the number of technologies it contains.

The primary goal is to become a significantly stronger software engineer while producing a credible, demonstrable system.

At any stage of development, the project should make it possible to explain clearly to a potential employer:

- what problems the system solves;
- what is implemented today;
- why the architecture was chosen;
- why each technology is used;
- what trade-offs were made;
- how security and data boundaries are enforced;
- how failures are handled;
- how the system can evolve further.
